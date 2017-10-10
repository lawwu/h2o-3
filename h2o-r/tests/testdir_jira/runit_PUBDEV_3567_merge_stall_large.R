setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source("../../scripts/h2o-r-test-setup.R")

test <- function() {
  data <- h2o.uploadFile(locate("bigdata/laptop/higgs_train_imbalance_100k.csv"))
  data2 <- h2o.uploadFile(locate("bigdata/laptop/higgs_test_imbalance_100k.csv"))
  f <- h2o.merge(data, data2, by.x = c("response"), by.y=c("response"))
  f
  
}

doTest("PUBDEV-784", test)
