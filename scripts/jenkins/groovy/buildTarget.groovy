def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  if (config.archiveFiles == null) {
    config.archiveFiles = true
  }
  if (config.filesToArchive == null) {
    config.filesToArchive = '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
  }
  if (config.hasJUnit == null) {
    config.hasJUnit = true
  }

  try {
    execMake(config.target)
  } finally {
    if (config.hasJUnit) {
      junit testResults: '**/test-results/*.xml', keepLongStdio: true, allowEmptyResults: true
    }
    if (config.archiveFiles) {
      archiveArtifacts artifacts: config.filesToArchive, allowEmptyArchive: true
    }
  }
}

def execMake(target) {
  sh """
    export JAVA_HOME=/usr/lib/jvm/java-8-oracle
    export USER=jenkins

    echo "Activating Python ${env.PYTHON_VERSION}"
    . /envs/h2o_env_python${env.PYTHON_VERSION}/bin/activate

    echo "Activating R ${env.R_VERSION}"
    activate_R_${env.R_VERSION}

    cd h2o-3
    echo "Linking small and bigdata"
    rm -f smalldata
    ln -s -f /home/0xdiag/smalldata
    rm -f bigdata
    ln -s -f /home/0xdiag/bigdata

    unset CHANGE_AUTHOR_DISPLAY_NAME
    unset CHANGE_TITLE
    echo "Running Make"
    make -f docker/Makefile.jenkins ${target}
  """
}
