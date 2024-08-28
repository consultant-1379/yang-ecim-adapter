#! /bin/bash

PARAMETERS="PORT NODE ADAPTER_USERNAME PASSWORD LISTENER NODE_PORT IDLE_TIMEOUT DISCONNECT_TIMEOUT LISTENER ADAPTER_NORTHBOUND_IP_ADDRESS ADAPTER_SOUTHBOUND_IP_ADDRESS"

DEFAULT_PORT="2222"
DEFAULT_NODE="192.168.100.1"
DEFAULT_ADAPTER_USERNAME="netsim"
DEFAULT_PASSWORD="netsim"
DEFAULT_LISTENER="com.ericsson.ranexplorer.yangecimadapter.netconf.rpc.handler.config.listener.AdapterListener"
DEFAULT_NODE_PORT="2222"
DEFAULT_IDLE_TIMEOUT="600000"
DEFAULT_DISCONNECT_TIMEOUT="10000"
INSTALL_DIR="/opt/ericsson/adapter"
DEFAULT_ADAPTER_NORTHBOUND_IP_ADDRESS="0.0.0.0"
DEFAULT_ADAPTER_SOUTHBOUND_IP_ADDRESS="0.0.0.0"

[ -z "$PORT" ] && PORT=$DEFAULT_PORT
[ -z "$NODE" ] && NODE=$DEFAULT_NODE
[ -z "$ADAPTER_USERNAME" ] && ADAPTER_USERNAME=$DEFAULT_ADAPTER_USERNAME
[ -z "$PASSWORD" ] && PASSWORD=$DEFAULT_PASSWORD
[ -z "$LISTENER" ] && LISTENER=$DEFAULT_LISTENER
[ -z "$NODE_PORT" ] && NODE_PORT=$DEFAULT_NODE_PORT
[ -z "$IDLE_TIMEOUT" ] && IDLE_TIMEOUT=$DEFAULT_IDLE_TIMEOUT
[ -z "$DISCONNECT_TIMEOUT" ] && DISCONNECT_TIMEOUT=$DEFAULT_DISCONNECT_TIMEOUT
[ -z "$ADAPTER_NORTHBOUND_IP_ADDRESS" ] && ADAPTER_NORTHBOUND_IP_ADDRESS=$DEFAULT_ADAPTER_NORTHBOUND_IP_ADDRESS
[ -z "$ADAPTER_SOUTHBOUND_IP_ADDRESS" ] && ADAPTER_SOUTHBOUND_IP_ADDRESS=$DEFAULT_ADAPTER_SOUTHBOUND_IP_ADDRESS

for PARAM in $PARAMETERS; do
  PARAM_VALUE=${!PARAM}
  echo "$PARAM value is ${PARAM_VALUE}"
done

START_COMMAND="java -Dlogback.configurationFile=$INSTALL_DIR/conf/logback.xml -Dorg.apache.sshd.maxDHGexKeySize=2048 -jar $INSTALL_DIR/lib/adapter-server-${project.version}.jar ssh --port $PORT -n $NODE --nodeport $NODE_PORT -u $ADAPTER_USERNAME -p $PASSWORD  -a $ADAPTER_NORTHBOUND_IP_ADDRESS -b $ADAPTER_SOUTHBOUND_IP_ADDRESS -i $IDLE_TIMEOUT -d $DISCONNECT_TIMEOUT -c $LISTENER"
echo $START_COMMAND
eval $START_COMMAND


