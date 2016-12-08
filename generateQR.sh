#!/bin/bash


export SRC_HOME=`pwd`;

# Log file name
filename=`eval date '+qrgenerator-stdout.%d%m%Y-%H%M%S.log'`

export CLASSPATH=$SRC_HOME/QRCode:$CLASSPATH

for i in `/bin/ls $SRC_HOME/lib/*.jar`
do
	export CLASSPATH=$CLASSPATH:$i
done

CONFIG_FILE=$SRC_HOME/config/qrgeneration.properties
export CLASSPATH=$CLASSPATH:$CONFIG_FILE
export CLASSPATH=$CLASSPATH:./config/qrgeneration.properties

echo $CLASSPATH;


#java QRCode $CONFIG_FILE  1>>$SRC_HOME/logs/$filename 2>>$SRC_HOME/logs/$filename 

java QRCode ./config/qrgeneration.properties 1>>$SRC_HOME/logs/$filename 2>>$SRC_HOME/logs/$filename 
