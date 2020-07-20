#!/usr/bin/env bash

"${GRAALVM_HOME}/bin/native-image" \
  --no-server \
  --no-fallback \
  -H:IncludeResources='org/sqlite/native/Mac/.*' \
  -H:+ReportExceptionStackTraces \
  -jar build/libs/server.min.jar
