def call() {
  sh """
    echo "Activating Python ${env.PYTHON_VERSION}"
    . /envs/h2o_env_python${env.PYTHON_VERSION}/bin/activate
    pip install h2o-3/h2o-py/dist/*.whl
  """
}

return this
