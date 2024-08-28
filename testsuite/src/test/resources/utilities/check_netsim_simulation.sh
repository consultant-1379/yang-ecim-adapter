#!/bin/bash

SLEEP_DURATION=5
SLEEP_LOOP=18
NETSIM_UP="INFO: \[press Ctrl+C to exit\] or run"

sleep 3    # give docker a moment to start up
iter=1
while [ $iter -le $SLEEP_LOOP ]
do
  netsim_log=$(docker logs --tail=1 netsim)
  echo "$netsim_log" | grep -q "$NETSIM_UP"
  [ $? -eq 0 ] && iter=0 && break
  sleep $SLEEP_DURATION
  echo -n .
  iter=$(( $iter + 1 ))
done
echo
exit $iter
