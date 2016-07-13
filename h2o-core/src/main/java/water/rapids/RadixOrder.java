package water.rapids;

import water.*;
import water.fvec.Chunk;
import water.fvec.Frame;
import water.fvec.Vec;
import water.util.ArrayUtils;
import water.util.Log;
import water.util.Pair;
import water.util.PrettyPrint;

import java.util.Arrays;
import java.util.Hashtable;


public class RadixOrder extends H2O.H2OCountedCompleter<RadixOrder> {  // counted completer so that left and right index can run at the same time
  int _shift[];
  int _bytesUsed[];
  long _base[];
  //long[][][] _o;
  //byte[][][] _x;
  Frame _DF;
  boolean _isLeft;
  int _whichCols[], _id_maps[][];

  RadixOrder(Frame DF, boolean isLeft, int whichCols[], int id_maps[][]) {
    _DF = DF;
    _isLeft = isLeft;
    _whichCols = whichCols;
    _id_maps = id_maps;
  }

  @Override
  public void compute2() {

    //System.out.println("Calling RadixCount ...");
    long t0 = System.nanoTime();
    _shift = new int[_whichCols.length];   // currently only _shift[0] is used
    _bytesUsed = new int[_whichCols.length];
    _base = new long[_whichCols.length];
    for (int i=0; i<_whichCols.length; i++) {
      Vec col = _DF.vec(_whichCols[i]);
      // TODO: strings that aren't already categoricals and fixed precision double.
      long max;
      if (col.isCategorical()) {
        _base[i] = 0;  // simpler and more robust for now for all categorical bases to be 0, even though some subsets may be far above 0; i.e. forgo uncommon efficiency savings for now
        if (_isLeft) {
          assert _id_maps[i] != null;
          // the left's levels have been matched to the right's levels and we store the mapped values so it's that mapped range we need here (or the col.max() of the corresponding right table would be fine too, but mapped range might be less so use that for possible efficiency)
          //_colMin[i] = ArrayUtils.minValue(_id_maps[i]);  // TODO: what is in _id_maps for no matches (-1?) and exclude those i.e. find the minimum >=0. Then treat -1 in _id_map as an NA when writing key
          max = ArrayUtils.maxValue(_id_maps[i]); // if we join to a small subset of levels starting at 0, we'll benefit from the smaller range here, though
        } else {
          max = (long)col.max();
        }
      } else {
        _base[i] = (long)col.min();
        max = (long)col.max();
      }
      long range = max - _base[i] + 2;   // +1 for when min==max to include the bound, +1 for the leading NA spot
      int biggestBit = 1 + (int) Math.floor(Math.log(range) / Math.log(2));  // number of bits starting from 1 easier to think about (for me)
      if (biggestBit < 8) Log.warn("biggest bit should be >= 8 otherwise need to dip into next column (TODO)");  // TODO: feed back to R warnings()
      assert biggestBit >= 1;
      _shift[i] = Math.max(8, biggestBit)-8;
      long MSBwidth = 1<<_shift[i];
      if (_base[i] % MSBwidth != 0) {
        // choose base lower than minimum so as to align boundaries (unless minimum already on a boundary by chance)
        _base[i] = MSBwidth * (_base[i]/MSBwidth + (_base[i]<0 ? -1 : 0));
        assert _base[i] % MSBwidth == 0;
      }
      _bytesUsed[i] = (_shift[i]+15) / 8;
      assert (biggestBit-1)/8 + 1 == _bytesUsed[i];
      int chk = (int)(max - _base[i] + 1) >> _shift[i];  // relied on in RadixCount.map
      assert chk <= 255;
      assert chk >= 0;
    }
    int keySize = ArrayUtils.sum(_bytesUsed);   // The MSB is stored (seemingly wastefully on first glance) because we need it when aligning two keys in Merge()
    int batchSize = 256*1024*1024 / Math.max(keySize, 8) / 2 ;   // 256MB is the DKV limit.  / 2 because we fit o and x together in one OXBatch.
    // The Math.max ensures that batches of o and x are aligned, even for wide keys. To save % and / in deep iteration; e.g. in insert().
    System.out.println("Time to use rollup stats to determine biggestBit: " + (System.nanoTime() - t0) / 1e9);

    t0 = System.nanoTime();
    new RadixCount(_isLeft, _base[0], _shift[0], _whichCols[0], _isLeft ? _id_maps : null ).doAll(_DF.vec(_whichCols[0]));
    System.out.println("Time of MSB count MRTask left local on each node (no reduce): " + (System.nanoTime() - t0) / 1e9);

    // NOT TO DO:  we do need the full allocation of x[] and o[].  We need o[] anyway.  x[] will be compressed and dense.
    // o is the full ordering vector of the right size
    // x is the byte key aligned with o
    // o AND x are what bmerge() needs. Pushing x to each node as well as o avoids inter-node comms.


    // System.out.println("Starting MSB hist reduce across nodes and SplitByMSBLocal MRTask ...");
    // Workaround for incorrectly blocking closeLocal() in MRTask is to do a double MRTask and pass a key between them to pass output
    // from first on that node to second on that node.  // TODO: fix closeLocal() blocking issue and revert to simpler usage of closeLocal()
    t0 = System.nanoTime();
    Key linkTwoMRTask = Key.make();
    SplitByMSBLocal tmp = new SplitByMSBLocal(_isLeft, _base, _shift[0], keySize, batchSize, _bytesUsed, _whichCols, linkTwoMRTask, _id_maps).doAll(_DF.vecs(_whichCols));   // postLocal needs DKV.put()
    System.out.println("SplitByMSBLocal MRTask (all local per node, no network) took : " + (System.nanoTime() - t0) / 1e9);
    System.out.println(tmp.profString());

    t0 = System.nanoTime();
    new SendSplitMSB(linkTwoMRTask).doAllNodes();
    System.out.println("SendSplitMSB across all nodes took : " + (System.nanoTime() - t0) / 1e9);

    //long nGroup[] = new long[257];   // one extra for later to make undo of cumulate easier when finding groups.  TO DO: let grouper do that and simplify here to 256

    // dispatch in parallel
    RPC[] radixOrders = new RPC[256];
    System.out.print("Sending SingleThreadRadixOrder async RPC calls ... ");
    t0 = System.nanoTime();
    for (int i = 0; i < 256; i++) {
      //System.out.print(i+" ");
      radixOrders[i] = new RPC<>(SplitByMSBLocal.ownerOfMSB(i), new SingleThreadRadixOrder(_DF, _isLeft, batchSize, keySize, /*nGroup,*/ i)).call();
    }
    System.out.println("took : " + (System.nanoTime() - t0) / 1e9);

    System.out.print("Waiting for RPC SingleThreadRadixOrder to finish ... ");
    t0 = System.nanoTime();
    int i=0;
    for (RPC rpc : radixOrders) { //TODO: Use a queue to make this fully async
      // System.out.print(i+" ");
      rpc.get();
      //SingleThreadRadixOrder radixOrder = (SingleThreadRadixOrder)rpc.get();   // TODO: make sure all transient here
      i++;
    }
    System.out.println("took " + (System.nanoTime() - t0) / 1e9);

    tryComplete();

    // serial, do one at a time
//    for (int i = 0; i < 256; i++) {
//      H2ONode node = MoveByFirstByte.ownerOfMSB(i);
//      SingleThreadRadixOrder radixOrder = new RPC<>(node, new SingleThreadRadixOrder(DF, batchSize, keySize, nGroup, i)).call().get();
//      _o[i] = radixOrder._o;
//      _x[i] = radixOrder._x;
//    }

    // If sum(nGroup) == nrow then the index is unique.
    // 1) useful to know if an index is unique or not (when joining to it we know multiples can't be returned so can allocate more efficiently)
    // 2) If all groups are size 1 there's no need to actually allocate an all-1 group size vector (perhaps user was checking for uniqueness by counting group sizes)
    // 3) some nodes may have unique input and others may contain dups; e.g., in the case of looking for rare dups.  So only a few threads may have found dups.
    // 4) can sweep again in parallel and cache-efficient finding the groups, and allocate known size up front to hold the group sizes.
    // 5) can return to Flow early with the group count. User may now realise they selected wrong columns and cancel early.

  }

  private static class SendSplitMSB extends MRTask<SendSplitMSB> {
    final Key _linkTwoMRTask;
    SendSplitMSB(Key linkTwoMRTask) { _linkTwoMRTask = linkTwoMRTask; }
    @Override public void setupLocal() {
      SplitByMSBLocal.MOVESHASH.get(_linkTwoMRTask).SendSplitMSB();
      SplitByMSBLocal.MOVESHASH.remove(_linkTwoMRTask);
    }
  }
}

