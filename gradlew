#!/bin/sh
MYSELF=`which "$0" 2>/dev/null`
if [ "$MYSELF" = "" ]; then MYSELF="./$0"; fi
java=java
if [ -n "$JAVA_HOME" ]; then
  java="$JAVA_HOME/bin/java"
fi
exec "$java" -jar $MYSELF "$@"
exit 1
