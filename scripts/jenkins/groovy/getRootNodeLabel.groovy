import ai.h2o3.ci.Globals

// Returns label of node, which should execute the "root" build. "Root" build
// is the stage which waits for test stages to complete.
def call() {
  def result = null
  if (env.BUILD_URL.contains(Globals.PR_MACHINE)) {
    // If we are running on machine used for PRs, then run the "root" build
    // on master, because master has multiple executors
    echo "Running under ${Globals.PR_MACHINE} machine, use ${Globals.MASTER_NODE_LABEL} as root node."
    result = Globals.MASTER_NODE_LABEL
  } else {
    // If we are running on machine used for master branch builds, then run the "root" build
    // on the node with default label, because the master has only one executor
    echo "Running under ${Globals.MASTER_MACHINE} machine, use ${Globals.DEFAULT_NODE_LABEL} as root node."
    result = Globals.DEFAULT_NODE_LABEL
  }
  return result
}
