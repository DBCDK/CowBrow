#!/bin/bash
if [ "$SSL" = "yes" ]
then
  JAVA_SSL="--autoBindSsl"
fi

java -jar ${PWD}/CowBrow-0.1.0-payaramicro.jar $JAVA_SSL &

status=$?

if [ $status -ne 0 ]; then
  echo "Failed to start java process: $status"
  exit $status
fi

python3 -u ${PWD}/cowbrow_web.py &

status=$?

if [ $status -ne 0 ]; then
  echo "Failed to start python process: $status"
  exit $status
fi

while /bin/true; do
  PROCESS_1_STATUS=$(ps -ef |grep java |grep -v grep)foo
  PROCESS_2_STATUS=$(ps -ef |grep python3 | grep -v grep)foo

  # If the greps above find anything, they will exit with 0 status
  # If they are not both 0, then something is wrong
  #echo $PROCESS_1_STATUS
  #echo $PROCESS_2_STATUS
  if [ "$PROCESS_1_STATUS" = "foo" -o "$PROCESS_2_STATUS" = "foo" ]; then
    echo "One of the processes has already exited."
    exit -1
  fi
  sleep 10
done