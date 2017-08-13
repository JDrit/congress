#!/bin/bash

java -server \
     -Xmx4G \
     -XX:+TieredCompilation \
     -XX:AutoBoxCacheMax=20000 \
     -XX:+UseNUMA \
     -XX:+UseParallelGC \
     -XX:+UseParallelOldGC \
     -XX:+AggressiveOpts \
     -jar target/scala-2.11/congress.jar
