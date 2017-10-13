def call() {
  def MODE_PR = 'pr'
  def MODE_MASTER = 'master'
  def MODE_NIGHTLY = 'nightly'

  // Job will execute PR_STAGES only if these are green.
  def SMOKE_STAGES = [
    [
      stageName: 'Py2.7 Smoke', target: 'test-py-smoke', pythonVersion: '2.7',
      timeoutValue: 8, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'R3.4 Smoke', target: 'test-r-smoke', rVersion: '3.4.1',
      timeoutValue: 8, lang: 'r',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'PhantomJS Smoke', target: 'test-phantom-js-smoke',
      timeoutValue: 10, lang: 'js',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ]
  ]

  // Stages executed after each push to PR.
  def PR_STAGES = [
    [
      stageName: 'Py2.7 Booklets', target: 'test-py-booklets', pythonVersion: '2.7',
      timeoutValue: 40, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'Py2.7 Demos', target: 'test-py-demos', pythonVersion: '2.7',
      timeoutValue: 15, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'Py2.7 Init', target: 'test-py-init', pythonVersion: '2.7',
      timeoutValue: 5, hasJUnit: false, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'Py2.7 Small', target: 'test-pyunit-small', pythonVersion: '2.7',
      timeoutValue: 45, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'Py3.5 Small', target: 'test-pyunit-small', pythonVersion: '3.5',
      timeoutValue: 45, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'Py3.6 Small', target: 'test-pyunit-small', pythonVersion: '3.6',
      timeoutValue: 45, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'R3.4 Init', target: 'test-r-init', rVersion: '3.4.1',
      timeoutValue: 5, hasJUnit: false, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'R3.4 Small', target: 'test-r-small', rVersion: '3.4.1',
      timeoutValue: 90, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'R3.4 Small Client Mode', target: 'test-r-small-client-mode', rVersion: '3.4.1',
      timeoutValue: 120, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'R3.4 Datatable', target: 'test-r-datatable', rVersion: '3.4.1',
      timeoutValue: 20, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'R3.4 CMD Check', target: 'test-r-cmd-check', rVersion: '3.4.1',
      timeoutValue: 15, hasJUnit: false, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'R3.4 CMD Check as CRAN', target: 'test-r-cmd-check-as-cran', rVersion: '3.4.1',
      timeoutValue: 10, hasJUnit: false, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'R3.4 Booklets', target: 'test-r-booklets', rVersion: '3.4.1',
      timeoutValue: 50, lang: 'r',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt, **/*.out.txt, **/*.code, **/package_version_check_out.txt'
    ],
    [
      stageName: 'R3.4 Demos Small', target: 'test-r-demos-small', rVersion: '3.4.1',
      timeoutValue: 15, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt **/*.code'
    ],
    [
      stageName: 'PhantomJS Small', target: 'test-phantom-js-small',
      timeoutValue: 45, lang: 'js',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt'
    ],
    [
      stageName: 'PhantomJS', target: 'test-phantom-js',
      timeoutValue: 45, lang: 'js',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/tests.txt, **/*lib_h2o-flow_build_js_headless-test.js.out.txt'
    ],
    [
      stageName: 'Py2.7 Medium-large', target: 'test-pyunit-medium-large', pythonVersion: '2.7',
      timeoutValue: 90, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt, h2o-py/tests/testdir_dynamic_tests/testdir_algos/glm/Rsandbox*/*.csv'
    ],
    [
      stageName: 'Py3.5 Medium-large', target: 'test-pyunit-medium-large', pythonVersion: '3.5',
      timeoutValue: 90, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt, h2o-py/tests/testdir_dynamic_tests/testdir_algos/glm/Rsandbox*/*.csv'
    ],
    [
      stageName: 'R3.4 Medium-large', target: 'test-r-medium-large', rVersion: '3.4.1',
      timeoutValue: 70, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt  **/*.code'
    ],
    [
      stageName: 'R3.4 Demos Medium-large', target: 'test-r-demos-medium-large', rVersion: '3.4.1',
      timeoutValue: 120, lang: 'r',
      filesToArchive: '**/results/*, **/*tmp_model*, **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt'
    ],
    [
      stageName: 'PhantomJS Medium', target: 'test-phantom-js-medium',
      timeoutValue: 45, lang: 'js',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/tests.txt, **/*lib_h2o-flow_build_js_headless-test.js.out.txt'
    ]
  ]

  // Stages executed in addition to PR_STAGES after merge to master.
  def MASTER_STAGES = [

  ]

  // Stages executed in addition to MASTER_STAGES, used for nightly builds.
  def NIGHTLY_STAGES = [
    [
      stageName: 'Py3.6 Medium-large', target: 'test-pyunit-medium-large', pythonVersion: '3.6',
      timeoutValue: 90, lang: 'py',
      filesToArchive: '**/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/*ipynb.out.txt, h2o-py/tests/testdir_dynamic_tests/testdir_algos/glm/Rsandbox*/*.csv'
    ]
  ]

  properties(
    [
        parameters(
            [
              choice(name: 'rVersion', description: 'R version used to compile H2O-3', choices: '3.4.1\n3.3.3\n3.2.5\n3.1.3\n3.0.3'),
              choice(name: 'pythonVersion', description: 'Python version used to compile H2O-3', choices: "3.5\n3.6\n3.7"),
              string(name: 'customMakefileURL', defaultValue: '', description: 'Makefile used to build and test H2O-3. Leave empty to use docker/Makefile.jenkins from master'),
              choice(name: 'testsSize', description:'Choose small for smoke and small tests only. Medium-large runs medium-large test as well.', choices: "${SIZE_SMALL}\n${SIZE_MEDIUM_LARGE}")
            ]
        ),
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '25'))
    ]
  )

  def customEnv = [
    "JAVA_VERSION=8",
    "BUILD_HADOOP=false",
    "GRADLE_USER_HOME=../gradle-user-home",
    "GRADLE_OPTS=-Dorg.gradle.daemon=false"
  ]

  if (env.CHANGE_BRANCH != null && env.CHANGE_BRANCH != '') {
    cancelPreviousBuilds()
  }

  node (getRootNodeLabel()) {
    // node (Globals.DEFAULT_NODE_LABEL) {
    //   withDockerEnvironment(customEnv, 4, 'HOURS') {
    //
    //     stage ('Checkout Sources') {
    //       currentBuild.displayName = "${params.testsSize} #${currentBuild.id}"
    //       checkoutH2O()
    //       setJobDescription()
    //     }
    //
    //     stage ('Build H2O-3') {
    //       withEnv(["PYTHON_VERSION=${params.pythonVersion}", "R_VERSION=${params.rVersion}"]) {
    //         try {
    //           buildTarget {
    //             target = 'build-h2o-3'
    //             hasJUnit = false
    //             archiveFiles = false
    //           }
    //           buildTarget {
    //             target = 'test-package-py'
    //             hasJUnit = false
    //             archiveFiles = false
    //           }
    //           buildTarget {
    //             target = 'test-package-r'
    //             hasJUnit = false
    //             archiveFiles = false
    //           }
    //           buildTarget {
    //             target = 'test-package-js'
    //             hasJUnit = false
    //             archiveFiles = false
    //           }
    //         } finally {
    //           archiveArtifacts """
    //             h2o-3/docker/Makefile.jenkins,
    //             h2o-3/h2o-py/dist/*.whl,
    //             h2o-3/build/h2o.jar,
    //             h2o-3/h2o-3/src/contrib/h2o_*.tar.gz,
    //             h2o-3/h2o-assemblies/genmodel/build/libs/genmodel.jar,
    //             h2o-3/test-package-*.zip,
    //             **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/tests.txt, **/status.*
    //           """
    //         }
    //       }
    //     }
    //   }
    // }
    // executeInParallel(SMOKE_STAGES, customEnv, params.customMakefileURL)
    //
    // def jobs = SMALL_JOBS
    // if (params.testsSize.toLowerCase() == SIZE_MEDIUM_LARGE.toLowerCase()) {
    //   jobs += MEDIUM_LARGE_JOBS
    // }
    // executeInParallel(jobs, customEnv, params.customMakefileURL)
  }
}

def executeInParallel(jobs, customEnv, customMakefileURL) {
  parallel(jobs.collectEntries { c ->
    [
      c['stageName'], {
  //       withEnv(customEnv) {
  //         defaultTestPipeline {
  //           stageName = c['stageName']
  //           target = c['target']
  //           pythonVersion = c['pythonVersion']
  //           rVersion = c['rVersion']
  //           timeoutValue = c['timeoutValue']
  //           hasJUnit = c['hasJUnit']
  //           filesToArchive = c['filesToArchive']
  //           lang = c['lang']
  //         }
  //       }
      }
    ]
  })
}

def setJobDescription() {
  // def MAX_MESSAGE_LENGTH = 30
  // def gitSHA = sh(returnStdout: true, script: 'cd h2o-3 && git rev-parse HEAD').trim()
  // def gitMessage = sh(returnStdout: true, script: 'cd h2o-3 && git log -1 --pretty=%B').trim()
  // if (gitMessage.length() >= MAX_MESSAGE_LENGTH) {
  //   gitMessage = gitMessage.substring(0, MAX_MESSAGE_LENGTH) + '...'
  // }
  // def gitAuthor = sh(returnStdout: true, script: 'cd h2o-3 && git log -1 --format=\'%an <%ae>\'').trim()
  //
  // currentBuild.description = "MSG: ${gitMessage}\nAUT: ${gitAuthor}\nSHA: ${gitSHA}"
}
