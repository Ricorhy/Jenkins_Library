#!/usr/bin/groovy
def call() {
    def build = currentBuild.rawBuild
    def cause = build.getCause(hudson.model.Cause.UserIdCause.class)
    return cause.getUserName()
}