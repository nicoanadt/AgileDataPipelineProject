#!/bin/bash

./data/edge/parallel_commands "python /data/edge/kafka-streamer3.py cfg/cfg_jt1.yml" "python /data/edge/kafka-streamer3.py cfg/cfg_jt2.yml" "python /data/edge/kafka-streamer3.py cfg/cfg_jt3.yml" "python /data/edge/kafka-streamer3.py cfg/cfg_jt4.yml"