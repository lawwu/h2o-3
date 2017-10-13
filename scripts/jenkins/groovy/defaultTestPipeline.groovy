import ai.h2o3.ci.Globals

def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  if (config.pythonVersion == null) {
    config.pythonVersion = '3.5'
  }
  if (config.rVersion == null) {
    config.rVersion = '3.4.1'
  }
  if (config.timeoutValue == null) {
    config.timeoutValue = 60
  }
  if (config.hasJUnit == null) {
    config.hasJUnit = true
  }

  def customEnv = [
    "PYTHON_VERSION=${config.pythonVersion}",
    "R_VERSION=${config.rVersion}",
    "JAVA_VERSION=8",
    "BUILD_HADOOP=false",
    "BUILD_HADOOP=false",
    "GRADLE_USER_HOME=../gradle-user-home",
    "GRADLE_OPTS=-Dorg.gradle.daemon=false"
  ]

  node(Globals.DEFAULT_NODE_LABEL) {
    withDockerEnvironment(customEnv, config.timeoutValue, 'MINUTES') {
      stage(config.stageName) {
        dir ('h2o-3') {
          deleteDir()
        }
        unpackTestPackage(config.lang, env.JOB_NAME, env.BUILD_ID)
        if (config.lang == 'py') {
          installPythonPackage()
        }
        if (config.lang == 'r') {
          installRPackage()
        }
        buildTarget {
          target = config.target
          filesToArchive = config.filesToArchive
          hasJUnit = config.hasJUnit
        }
      }
    }
  }
}
