# mysql 优化记录
需求：一个 跑马灯播报最新中将的前20条记录 功能，通过直接查询数据来返回结果
问题：这种全范围查询，无法走索引
优化：通过redis list来优化，当新纪录产生时，lpush，当数据量超过指定值（100或者更大），ltrim（0，20）
获取数据时，获取lrange。

问题&优化：子查询不走索引，修改为关联查询

问题&优化：数据插入较慢，因为有一些不常用的索引，删除索引

问题&优化：多次根据不同的条件查询同一张表（数据量不大），修改为单次查询，在程序中再根据条件过滤

问题：
```sql
-- 查询耗时50s左右
select * from 
(
    SELECT buyer_domain_id, buyer_id, buyer_mobile 
    FROM table_order 
    WHERE someCondition 
    GROUP BY buyer_id, buyer_domain_id HAVING COUNT(*) >= 1 AND COUNT(*) <= 100 
) t LIMIT 0,1000
```
```sql
-- 查询耗时13秒左右
select * from 
(
    SELECT buyer_domain_id, buyer_id, buyer_mobile 
    FROM table_order 
    WHERE someCondition 
    GROUP BY buyer_id HAVING COUNT(*) >= 1 AND COUNT(*) <= 100 
) t LIMIT 0,1000
```
```sql
-- 查询耗时2秒左右
SELECT buyer_domain_id, buyer_id, buyer_mobile 
FROM table_order 
WHERE someCondition 
GROUP BY buyer_id HAVING COUNT(*) >= 1 AND COUNT(*) <= 100 
LIMIT 0,1000
```
第一条没走索引，还加了一层select * 全表，第二条增加了走索引，第三条减少了一层select *

问题&优化：索引列上做计算不走索引，修改为计算好的值

问题&优化：数据类型不一致，不走索引

问题&优化：在凌晨统计昨日数据，或者在23：59统计当日数据，可以加一个年月日字段，并创建索引

问题&优化：order by create_time 可以优化为order by id desc

