// Returns label of node, which should execute the "root" build. "Root" build
// is the stage which waits for test stages to complete.
def call() {
  def DEFAULT_NODE_LABEL = 'docker && !mr-0xc8 && !mr-0xg2'
  def PR_MACHINE = 'mr-0xb4'
  def MASTER_MACHINE = 'mr-0xc1'
  def MASTER_NODE_LABEL = 'master'

  def result = null
  if (env.BUILD_URL.contains(PR_MACHINE)) {
    // If we are running on machine used for PRs, then run the "root" build
    // on master, because master has multiple executors
    echo "Running under ${PR_MACHINE} machine, use ${MASTER_NODE_LABEL} as root node."
    result = MASTER_NODE_LABEL
  } else {
    // If we are running on machine used for master branch builds, then run the "root" build
    // on the node with default label, because the master has only one executor
    echo "Running under ${MASTER_MACHINE} machine, use ${DEFAULT_NODE_LABEL} as root node."
    result = DEFAULT_NODE_LABEL
  }
  return result
}

return this
