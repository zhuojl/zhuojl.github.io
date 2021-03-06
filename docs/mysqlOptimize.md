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

问题&优化：大字段单独存储，

问题&优化：被关联的表的关联字段才需要建立索引，不是两个表都建立

问题&优化：错误：exits比in好;用关联查询走索引效果或许更好，重要的是通过explain查看计划

问题&优化：order by 是走索引的，所以看到select count(*) from table 比 select * from table order by id 慢是正常的。

问题：存在大量空的数据，（比如奖励金为0），不重要，但又不能没有；或者说大量系统处理数据，又比如推荐关系，大量未推荐数据，当需要查询推荐关系时，效率很差
优化：个人理解这些数据可以单独存储，避免多数场景下查询效率，需要和产品沟通，在一些例如分页的场景做妥协

问题&优化：规则表使用json字段存储



