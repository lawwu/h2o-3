def call() {
  return currentBuild.rawBuild.getAction(CauseAction.class).findCause(hudson.triggers.TimerTrigger.TimerTriggerCause.class) != null
}
