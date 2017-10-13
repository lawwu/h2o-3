def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def cancelPreviousBuilds = load('jenkins-scripts/scripts/jenkins/groovy/cancelPreviousBuilds.groovy')
  def getRootNodeLabel = load('jenkins-scripts/scripts/jenkins/groovy/getRootNodeLabel.groovy')
  def withDockerEnvironment = load('jenkins-scripts/scripts/jenkins/groovy/withDockerEnvironment.groovy')
  def buildTarget = load('jenkins-scripts/scripts/jenkins/groovy/buildTarget.groovy')
  def defaultTestPipeline = load('jenkins-scripts/scripts/jenkins/groovy/defaultTestPipeline.groovy')

  def MODE_PR_CODE = 0
  def MODE_MASTER_CODE = 1
  def MODE_NIGHTLY_CODE = 2
  def MODES = [
    [name: 'MODE_PR', code: MODE_PR_CODE],
    [name: 'MODE_MASTER', code: MODE_MASTER_CODE],
    [name: 'MODE_NIGHTLY', code: MODE_NIGHTLY_CODE]
  ]

  def DEFAULT_NODE_LABEL = 'docker && !mr-0xc8 && !mr-0xg2'

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
    echo "###### Cancelling all previous builds ######"
    cancelPreviousBuilds()
  }

  node (getRootNodeLabel()) {
    node (DEFAULT_NODE_LABEL) {
      // node change, need to get scripts
      echo "Pulling scripts"
      step ([$class: 'CopyArtifact',
        projectName: env.JOB_NAME,
        filter: "jenkins-scripts/scripts/jenkins/groovy/*",
        selector: [$class: 'SpecificBuildSelector', buildNumber: env.BUILD_ID]
      ]);

      withDockerEnvironment(customEnv, 4, 'HOURS') {
        stage ('Checkout') {
          dir ('h2o-3') {
            deleteDir()
            checkout scm
          }
        }
        stage ('Build H2O-3') {
          withEnv(["PYTHON_VERSION=${params.pythonVersion}", "R_VERSION=${params.rVersion}"]) {
            try {
              buildTarget {
                target = 'build-h2o-3'
                hasJUnit = false
                archiveFiles = false
              }
              buildTarget {
                target = 'test-package-py'
                hasJUnit = false
                archiveFiles = false
              }
              buildTarget {
                target = 'test-package-r'
                hasJUnit = false
                archiveFiles = false
              }
              buildTarget {
                target = 'test-package-js'
                hasJUnit = false
                archiveFiles = false
              }
            } finally {
              archiveArtifacts """
                h2o-3/docker/Makefile.jenkins,
                h2o-3/h2o-py/dist/*.whl,
                h2o-3/build/h2o.jar,
                h2o-3/h2o-3/src/contrib/h2o_*.tar.gz,
                h2o-3/h2o-assemblies/genmodel/build/libs/genmodel.jar,
                h2o-3/test-package-*.zip,
                **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/tests.txt, **/status.*
              """
            }
          }
        }
      }
    }

    executeInParallel(SMOKE_STAGES, customEnv, params.customMakefileURL, defaultTestPipeline)

    def mode = MODES.find{it['name'] == config.mode}['code']
    def jobs = PR_STAGES
    if (mode >= MODE_MASTER_CODE) {
      jobs += MASTER_STAGES
    }
    if (mode >= MODE_NIGHTLY_CODE) {
      jobs += MASTER_STAGES
    }
    executeInParallel(jobs, customEnv, params.customMakefileURL, defaultTestPipeline)
  }

}

def executeInParallel(jobs, customEnv, customMakefileURL, defaultTestPipeline) {

  parallel(jobs.collectEntries { c ->
    [
      c['stageName'], {
        withEnv(customEnv) {
          defaultTestPipeline {
            stageName = c['stageName']
            target = c['target']
            pythonVersion = c['pythonVersion']
            rVersion = c['rVersion']
            timeoutValue = c['timeoutValue']
            hasJUnit = c['hasJUnit']
            filesToArchive = c['filesToArchive']
            lang = c['lang']
          }
        }
      }
    ]
  })
}

return this
