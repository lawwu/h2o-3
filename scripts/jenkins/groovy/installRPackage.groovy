def call() {
  sh """
    echo "Activating R ${env.R_VERSION}"
    activate_R_${env.R_VERSION}
    R CMD INSTALL h2o-3/h2o-r/R/src/contrib/h2o*.tar.gz
  """
}

return this
