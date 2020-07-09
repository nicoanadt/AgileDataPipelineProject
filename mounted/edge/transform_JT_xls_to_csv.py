import pandas as pd
from datetime import datetime
import numpy as np

# HOW TO RUN: python /data/edge/transform_JT_xls_to_csv.py

FILENAME = '/data/spark/data/journeytime/r1_trafford/Route_1_TraffordRoad_A_15minIntervals_JT_May2019'
IN_FILENAME = FILENAME + '.xlsx'
OUT_FILENAME = FILENAME + '.csv'
FILETYPE = 'Route_1_TraffordRoad'
 
df = pd.read_excel(IN_FILENAME, skiprows=8, nrows=97) 
df1 = df.replace(np.nan, '', regex=True)

# Create new column names
newcolarray = []
for x in df1.iloc[0]:
	try:
		timecol = datetime.strptime(x, '%a %d/%m/%y')
		newcolarray.append(timecol.strftime('%Y-%m-%d'))
	except ValueError:
		newcolarray.append('time')
		
df1.columns = newcolarray

df_out = pd.melt(df1,id_vars=['time'])
df_out = df_out[df_out.time!='']
df_out = df_out.replace('-','00:00:00')
df_out['Route'] = FILETYPE


df_out.to_csv(OUT_FILENAME, index=False)
