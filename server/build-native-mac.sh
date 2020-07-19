#!/usr/bin/env bash

native-image \
  --no-server \
  --no-fallback \
  -H:IncludeResources='org/sqlite/native/Mac/.*' \
  -H:+ReportExceptionStackTraces \
  -jar build/libs/server.jar
