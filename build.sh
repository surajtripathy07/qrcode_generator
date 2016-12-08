#!/bin/bash

export SRC_HOME=`pwd`
export CLASSPATH=$SRC_HOME:$CLASSPATH

for i in `/bin/ls $SRC_HOME/lib/*.jar`
do
        export CLASSPATH=$CLASSPATH:$i
done

javac $SRC_HOME/QRCode.java
