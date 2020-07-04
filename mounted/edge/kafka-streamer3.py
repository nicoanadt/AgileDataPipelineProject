from kafka import KafkaProducer
import json
import pandas as pd
import time
import simplejson
import sys
import yaml

if len(sys.argv) > 1:
    CFG_FILENAME = "/data/edge/" + sys.argv[1]
else:
    print("No arguments supplied")
    exit()

# CONFIG LOAD
with open(CFG_FILENAME, "r") as ymlfile:
    cfg = yaml.load(ymlfile)
    
FILENAME = cfg['FILENAME']
DICT_FILENAME = cfg['DICT_FILENAME']
TOPIC = cfg['TOPIC']
BOOTSTRAP_SERVER = cfg['BOOTSTRAP_SERVER']
OFFSET_FILENAME_PREF = cfg['OFFSET_FILENAME_PREF']
OFFSET_FILENAME_SUFF = cfg['OFFSET_FILENAME_SUFF']


# KAFKA SETUP
producer = KafkaProducer(bootstrap_servers=BOOTSTRAP_SERVER)
stream_offset_start = 1
stream_no_of_recs = 1
stream_interval = 1


    
OFFSET_FILENAME = OFFSET_FILENAME_PREF + TOPIC + OFFSET_FILENAME_SUFF

# OPEN FILE
df = pd.read_csv (FILENAME)

# OPEN SCHEMA MAP
map = json.load(open(DICT_FILENAME))

# REPLACE COLUMN NAME FROM MAP
df.rename(columns=map, inplace=True)      

# OPEN OFFSET FILE
with open(OFFSET_FILENAME) as offset_file:
    try:
        stream_offset_start = int(offset_file.read())
    except:
        stream_offset_start = 0
    offset_file.close()

# VARS DECLARATION
loop_stream_offset_start = stream_offset_start
loop_stream_offset_end = stream_offset_start + stream_no_of_recs
    

with open(OFFSET_FILENAME,"w") as offset_file:

    # THE LOOP TO INITIATE SCHEDULER
    while True:
        print("Pushing data to", TOPIC)   

        
        for row in df.iloc[loop_stream_offset_start:loop_stream_offset_end].to_dict(orient='records'):
            producer.send(TOPIC, simplejson.dumps(row, ignore_nan=True).encode('utf-8')) 
        
        
        
        # INCREMENT OFFSETS
        loop_stream_offset_start = loop_stream_offset_start + stream_no_of_recs
        loop_stream_offset_end = loop_stream_offset_end + stream_no_of_recs
        
        # TRUNCATE FILE AND WRITE OUTPUT
        offset_file.seek(0)
        offset_file.truncate()
        offset_file.write(str(loop_stream_offset_start))
        
        # SLEEP FOR x SECS
        time.sleep(stream_interval)
    

    
    







 




