def call (String lang, String jobName, String buildID) {
    echo "Pulling artifacts"
    step ([$class: 'CopyArtifact',
      projectName: jobName,
      filter: "h2o-3/test-package-${lang}.zip, h2o-3/build/h2o.jar",
      selector: [$class: 'SpecificBuildSelector', buildNumber: buildID]
    ]);
    sh "cd h2o-3 && unzip test-package-${lang}.zip && rm test-package-${lang}.zip"
}
