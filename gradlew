#!/bin/sh
export JAVA_HOME="${JAVA_HOME:-/opt/tools/jdk21}"
export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-/opt/android-sdk}"
export PATH="$JAVA_HOME/bin:$PATH"
exec /opt/tools/gradle-9.3.1/bin/gradle "$@"
