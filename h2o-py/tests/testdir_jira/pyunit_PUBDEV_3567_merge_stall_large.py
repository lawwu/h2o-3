import sys
sys.path.insert(1,"../../")
import h2o
from tests import pyunit_utils

def pubdev_3567():

    mattTest1()
    # train = h2o.import_file(pyunit_utils.locate("bigdata/laptop/higgs_train_imbalance_100k.csv"))
    # test = h2o.import_file(pyunit_utils.locate("bigdata/laptop/higgs_test_imbalance_100k.csv"))
    # merged = train.merge(test,by_x=["response"],by_y=["response"])#, method="radix")
    # merged[0,0]
    # col = 10000* [0, 0, 1, 1, 2, 3, 0]
    # fr = h2o.H2OFrame(list(zip(*[col])))
    # fr.set_names(['rank'])
    #
    # mapping = h2o.H2OFrame(list(zip(*[[0,1,2,3],[6,7,8,9]])))
    # mapping.set_names(['rank', 'outcome'])
    #
    # merged = fr.merge(mapping,all_x=True,all_y=False)
    #
    # rows, cols = merged.dim
    # assert rows == 70000 and cols == 2, "Expected 70000 rows and 2 cols, but got {0} rows and {1} cols".format(rows, cols)
    #
    # threes = merged[merged['rank'] == 3].nrow
    # assert threes == 10000, "Expected 10000 3's, but got {0}".format(threes)


def mattTest1():
    d1_list = [['A', 'x'], ['a', 1], ['b', 2], ['c', 3], ['d', 4], ['e', 5], ['f', 6], ['g', 7], ['h', 8], ['i', 9],
               ['j', 10]]
    d2_list = [['A', 'y'], ['e', 1], ['f', 2], ['g', 3], ['h', 4], ['i', 5], ['j', 6], ['k', 7], ['l', 8], ['m', 9],
               ['n', 10]]
    merged1Ans_list = [['A', 'x', 'y'], ['e', 5, 1], ['f', 6, 2], ['g', 7, 3], ['h', 8, 4], ['i', 9, 5], ['j', 10, 6]]
    merged2Ans_list = [['A', 'x', 'y'], ['a',1,'NA'], ['b', 2, 'NA'], ['c', 3, 'NA'], ['d', 4, 'NA'], ['e', 5, 1],
                  ['f', 6, 2], ['g', 7, 3], ['h', 8, 4], ['i', 9, 5], ['j', 10, 6], ['k','NA',7], ['l','NA',8],
                  ['m', 'NA', 9], ['n', 'NA', 10]]

    merged2Ans_listL = [['A', 'x', 'y'], ['a',1,'NA'], ['b', 2, 'NA'], ['c', 3, 'NA'], ['d', 4, 'NA'], ['e', 5, 1],
                       ['f', 6, 2], ['g', 7, 3], ['h', 8, 4], ['i', 9, 5], ['j', 10, 6]]

    merged2Ans_listR = [['A', 'x', 'y'],['e', 5, 1], ['f', 6, 2], ['g', 7, 3], ['h', 8, 4], ['i', 9, 5], ['j', 10, 6],
                        ['k','NA',7], ['l','NA',8], ['m', 'NA', 9], ['n', 'NA', 10]]
    d1 = h2o.H2OFrame(python_obj=d1_list, header=1, column_types=['enum','int'], na_strings=['NA'])
    d2 = h2o.H2OFrame(python_obj=d2_list, header=1, column_types=['enum','int'], na_strings=['NA'])
    merged1Ans = h2o.H2OFrame(python_obj=merged1Ans_list, header=1, column_types=['enum','int', 'int'], na_strings=['NA'])
   # merged2Ans = h2o.H2OFrame(python_obj=merged2Ans_list, header=1, column_types=['enum','int', 'int'])
    merged2AnsL = h2o.H2OFrame(python_obj=merged2Ans_listL, header=1, column_types=['enum','int', 'int'], na_strings=['NA'])
    merged2AnsR = h2o.H2OFrame(python_obj=merged2Ans_listR, header=1, column_types=['enum','int', 'int'], na_strings=['NA'])
    temp = h2o.H2OFrame(python_obj=merged2Ans_listR, header=1, column_types=['enum','int', 'int'], na_strings=['NA'])
    merged2AnsRB = temp[0].cbind(temp[2]).cbind(temp[1])

    merged2 = d2.merge(d1) # works
    answer2 = merged1Ans[0].cbind(merged1Ans[2]).cbind(merged1Ans[1])
    pyunit_utils.compare_frames(merged2, answer2, numElements=merged2.nrow)
    merged1 = d1.merge(d2) # works
    pyunit_utils.compare_frames(merged1, merged1Ans, numElements=merged1.nrow)
    merged2RB = d2.merge(d1, all_x=True)    # not working
    pyunit_utils.compare_frames(merged2RB, merged2AnsRB, numElements=merged2RB.nrow)
    merged2L = d1.merge(d2, all_x=True)
    pyunit_utils.compare_frames(merged2L, merged2AnsL, numElements=merged2L.nrow)
    merged1 = d1.merge(d2)
    pyunit_utils.compare_frames(merged1, merged1Ans, numElements=merged1.nrow)

    # merged2R = d1.merge(d2, all_y=True) # columns are switched, not working yet
    # pyunit_utils.compare_frames(merged2R, merged2AnsR, numElements=merged2R.nrow)




if __name__ == "__main__":
    pyunit_utils.standalone_test(pubdev_3567)
else:
    pubdev_3567()
