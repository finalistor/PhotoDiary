#!/bin/sh
APP_HOME=$(dirname "$(readlink -f "$0" 2>/dev/null || echo "$0")")
APP_HOME="$(cd "$APP_HOME" && pwd -P)"
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
