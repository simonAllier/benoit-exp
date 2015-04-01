#!/bin/sh

cd /root/diversify-statements
git pull
mvn clean -Dmaven.test.skip=true package

rm -rf results

java -Dhttp.proxyHost=proxy.rennes.grid5000.fr -Dhttps.proxyPort=3128 -Dhttps.proxyHost=proxy.rennes.grid5000.fr -Dhttp.proxyPort=3128 -cp target/benoit-exp-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.benoit.Main init


cpu=$(cat nbProcess)
for i in `seq 1 100000`
do
    for j in `seq 1 $cpu`
    do
        java  -Dhttp.proxyHost=proxy.rennes.grid5000.fr  -Dhttps.proxyPort=3128 -Dhttps.proxyHost=proxy.rennes.grid5000.fr  -Dhttp.proxyPort=3128  -cp target/benoit-exp-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.benoit.Main &
    done
    wait
    killall java
done

