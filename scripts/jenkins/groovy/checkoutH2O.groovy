def call() {
  dir ('h2o-3') {
    retry(3) {
      timeout(time: 1, unit: 'MINUTES') {
        echo "Checkout H2O-3"
        checkout scm
        if (params.customMakefileURL != null && params.customMakefileURL != '') {
          echo "Using custom Makefile.jenkins from ${params.customMakefileURL}"
          sh "wget -O h2o-3/docker/Makefile.jenkins ${params.customMakefileURL}"
        } else {
          echo "Using docker/Makefile.jenkins"
        }
        echo "Makefile.jenkins content:"
        sh "cat h2o-3/docker/Makefile.jenkins"
      }
    }
  }
}
