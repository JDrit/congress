#!/bin/bash

java -server \
     -Xmx5G \
     -XX:AutoBoxCacheMax=20000 \
     -XX:+TieredCompilation \
     -XX:+UseNUMA \
     -XX:+AggressiveOpts \
     -XX:+UseParallelGC \
     -XX:+UseParallelOldGC \
     -jar target/scala-2.11/congress.jar
