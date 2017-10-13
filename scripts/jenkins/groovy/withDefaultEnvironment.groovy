def call(customEnv = [], timeoutValue = 1, timeoutUnit = 'HOURS', block) {
  ansiColor('xterm') {
    timestamps {
      withEnv(customEnv) {
        sh "printenv"
        timeout(time: timeoutValue, unit: timeoutUnit) {
          block()
        }
      }
    }
  }
}

return this
