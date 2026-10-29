#!/bin/sh
# Minimal launcher for the official, version-pinned Gradle wrapper.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P) || exit 1
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi
# Intentional splitting supports the JVM options exported by the Nix dev shell.
exec "$JAVA" ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
