#Assets of Agile Data Pipeline Framework
This page contains the assets required to support the Framework execution. The details of each content is explained below.


##Sample Messages `sample-messages`

Three type of sample messages are available for each use case.

1. `UC1`:
Raw data of traffic record, where each event is represented as a single row
2. `UC2`:
Aggregated traffic record data, where records are accumulated in 5 minutes period
3. `UC3`:
Precalculated journey time data, where records are accumulated in 15 minutes period.

##Dataset for Lookup `dataset-lookup`

Two types of lookup dataset for 'static join' purpose:
1. `Sensor Lookup`:
Used for UC2, contains the sensor location name and its coordinates
2. `Path Lookup`:
Used for UC3, contains the coordinate of route path

##Framework

###Start Script `framework-start-script`

The data pipeline framework is executed using this starting script 

###WF Config `framework-wf-config`

The following configurations are used to run the use case samples
1. `WFUC1.json`
2. `WFUC2_1157.json`, `WFUC2_4073.json`, and `WFUC2_MERGED.json`
3. `WFUC3.json`

##Druid Ingestion `druid-ingest-json`

TBD

##Superset Dashboard `superset-dashboard`

Dashboard configuration for Superset use cases are described here

###Use Case 1
####UC1 - Today Avg Speed and UC1 Today Traffic Count

```
select 
__time,
class_name
direction_name ,
site_id ,
avg_speed,
count_vol
from druid.TargetUC1
```
####UC1 - Historical Avg Speed

```
select dt, TIME_FORMAT(dt,'yyyy-MM-dd E') dt_str, a.avg_speed, a.cnt 
from
(select 
DATE_TRUNC('day',__time) dt,
time_extract(DATE_TRUNC('day',__time), 'DOW') dow,
avg(speed) avg_speed,
count(*) cnt
from druid.TargetUC1
group by 
DATE_TRUNC('day',__time), time_extract(DATE_TRUNC('day',__time), 'DOW')
) a
join
(
select MAX(DATE_TRUNC('day',__time)) max_dt
from druid.TargetUC1
) b
on a.dow = time_extract(b.max_dt, 'DOW')
```
####UC1 - Historical Traffic Count
```
select dt, TIME_FORMAT(dt,'yyyy-MM-dd E') dt_str, a.avg_speed, a.cnt 
from
(select 
DATE_TRUNC('day',__time) dt,
time_extract(DATE_TRUNC('day',__time), 'DOW') dow,
avg(speed) avg_speed,
count(*) cnt
from druid.TargetUC1
group by 
DATE_TRUNC('day',__time), time_extract(DATE_TRUNC('day',__time), 'DOW')
) a
join
(
select MAX(DATE_TRUNC('day',__time)) max_dt
from druid.TargetUC1
) b
on a.dow = time_extract(b.max_dt, 'DOW')
```

###Use Case 2

####UC2 - Today Traffic Volume

Type:
```
deck.gl 3D Hexagon
```
Query:

```
select __time,a.locname,a.lon,a.lat,a.volume from 
druid.TargetUC2 a
join
(
select max(DATE_TRUNC('day',__time)) dt_max, locname
from druid.TargetUC2 
group by locname 
) b
on b.dt_max=DATE_TRUNC('day',a.__time) and a.locname =b.locname
```

Extra data for JS:
```
locname
```

Javascript tooltip generator:
```
function updateTooltip(object) {
  return ("Location: " + object.object.points[0].extraProps.locname + ", Volume: " + object.object.elevationValue);
}
```

####UC 2 - Historical Traffic Volume

Type:
```
Bar Chart
```

Query:
```
select dt, TIME_FORMAT(dt,'yyyy-MM-dd E') dt_str, a.sum_vol, a.locname 
from
(select 
DATE_TRUNC('day',__time) dt,
time_extract(DATE_TRUNC('day',__time), 'DOW') dow,
sum(volume) sum_vol,
locname
from druid.TargetUC2 where locname!='Chester Rd'
group by 
DATE_TRUNC('day',__time), time_extract(DATE_TRUNC('day',__time), 'DOW'), locname
) a
join
(
select MAX(DATE_TRUNC('day',__time)) max_dt, locname
from druid.TargetUC2 group by locname
) b
on a.dow = time_extract(b.max_dt, 'DOW') and a.locname=b.locname
```

###Use Case 3

#### UC3 - Traffic Map

Type:
```
deck.gl Path
```

Query:
```
select
a.route,
b.jt_secs,
case when b.jt_secs < p1 then '#42f542' --'green'
when b.jt_secs < p2 then '#42f542' --'green'
when b.jt_secs < p3 then '#42f542' --'green'
when b.jt_secs < p4 then '#fcb43f' --'orange'
else '#ff1f1f' --'red' 
end color,
coord
from
( select route, max(__time) max_time, avg(jt_secs) avg_jt_secs, 
APPROX_QUANTILE_DS(jt_secs,0.20,8) p1,
APPROX_QUANTILE_DS(jt_secs,0.40,8) p2,
APPROX_QUANTILE_DS(jt_secs,0.60,8) p3,
APPROX_QUANTILE_DS(jt_secs,0.80,8) p4
from TargetUC3 where jt_secs>0 group by route ) a
join 
TargetUC3 b 
on a.max_time=b.__time and a.route=b.route
join Route_Coord c
on a.route=c.routename
```

Lines encoding: `JSON`
Lines column: `coord`

Extra data for JS:
```
color, route, jt_secs
```

Javascript data interceptor:
```
data => data.map(d => ({
    ...d,
    color: colors.hexToRGB(d.extraProps.color)
}));
```

Javascript tooltip generator:
```
function updateTooltip(object) {
  return ("Route: "  + object.object.extraProps.route + ", " + object.object.extraProps.jt_secs + " secs");
}
```

#### UC3 - Historical Journey Time

Type
```
Bar Chart
```

Query
```
select dt, TIME_FORMAT(dt,'yyyy-MM-dd E') dt_str, a.avg_jt_secs, a.route 
from
(select 
DATE_TRUNC('day',__time) dt,
time_extract(DATE_TRUNC('day',__time), 'DOW') dow,
avg(jt_secs) avg_jt_secs,
route
from druid.TargetUC3 where jt_secs>0
group by 
DATE_TRUNC('day',__time), time_extract(DATE_TRUNC('day',__time), 'DOW'), route
) a
join
(
select MAX(DATE_TRUNC('day',__time)) max_dt, route
from druid.TargetUC3 group by route
) b
on a.dow = time_extract(b.max_dt, 'DOW') and a.route=b.route

```

Metrics: `sum(avg_jt_secs)`

Series: `routes`

Breakdowns: `dt_str`

