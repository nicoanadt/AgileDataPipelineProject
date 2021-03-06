from kafka import KafkaProducer
import json
import pandas as pd
import time
import simplejson

# KAFKA SETUP
producer = KafkaProducer(bootstrap_servers='kafka:9090')
topic_name = 'trafficTopicNow'
stream_offset_start = 40
stream_no_of_recs = 1
stream_interval = 1



# OPEN FILE
df = pd.read_csv (r'/data/spark/data/rawpvr_2019-07-01_31d_ATC_1157.csv')

# OPEN SCHEMA MAP
map = json.load(open('/data/edge/rawpvr_dict.json'))

# REPLACE COLUMN NAME FROM MAP
df.rename(columns=map, inplace=True)      

#pd.to_datetime('now').strftime("%Y-%m-%d %H:%M:%S")
df['date'] = pd.to_datetime('now').strftime("%Y-%m-%d %H:%M:%S")

# OPEN OFFSET FILE
with open("/data/edge/offset_now.txt") as offset_file: 
    stream_offset_start = int(offset_file.read())
    offset_file.close()

# VARS DECLARATION
loop_stream_offset_start = stream_offset_start
loop_stream_offset_end = stream_offset_start + stream_no_of_recs
    

with open("/data/edge/offset/offset_now.txt","w") as offset_file:

    # THE LOOP TO INITIATE SCHEDULER
    while True:
        print("Pushing data to", topic_name)   

        
        for row in df.iloc[loop_stream_offset_start:loop_stream_offset_end].to_dict(orient='records'):
            #producer.send(topic_name, json.dumps(row).encode('utf-8')) 
            producer.send(topic_name, simplejson.dumps(row, ignore_nan=True).encode('utf-8')) 
            
        
        # EXTRACT RECS
        #json_str = df.iloc[loop_stream_offset_start:loop_stream_offset_end].to_json(orient='records')
        
        # SEND DATA
        #producer.send(topic_name, json_str.encode('utf-8'))
        #print(json_str)
        
        # INCREMENT OFFSETS
        loop_stream_offset_start = loop_stream_offset_start + stream_no_of_recs
        loop_stream_offset_end = loop_stream_offset_end + stream_no_of_recs
        
        # TRUNCATE FILE AND WRITE OUTPUT
        offset_file.seek(0)
        offset_file.truncate()
        offset_file.write(str(loop_stream_offset_start))
        
        # SLEEP FOR x SECS
        time.sleep(stream_interval)
    

    
    







 




