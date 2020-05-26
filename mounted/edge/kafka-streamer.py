from kafka import KafkaProducer
import json
import pandas as pd
import time

# KAFKA SETUP
producer = KafkaProducer(bootstrap_servers='kafka:9090')
topic_name = 'sample'
stream_offset_start = 0
stream_no_of_recs = 1
stream_interval = 20

# VARS DECLARATION
loop_stream_offset_start = stream_offset_start
loop_stream_offset_end = stream_no_of_recs

# OPEN FILE
df = pd.read_csv (r'/data/spark/data/rawpvr_2019-07-01_31d_ATC_1157.csv')


# THE LOOP TO INITIATE SCHEDULER
while True:
    print("Start new loop -------------")
    
    # INCREMENT OFFSETS
    loop_stream_offset_start = loop_stream_offset_start + stream_no_of_recs
    loop_stream_offset_end = loop_stream_offset_end + stream_no_of_recs
    
    # EXTRACT RECS
    json_str = df.iloc[loop_stream_offset_start:loop_stream_offset_end].to_json(orient='records')
    
    # SEND DATA
    producer.send(topic_name, json_str.encode('utf-8'))
    #print(json_str)
    
    # SLEEP FOR x SECS
    time.sleep(stream_interval)
    
    







 




