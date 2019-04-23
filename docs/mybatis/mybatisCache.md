# 介绍
缓存的主要作用就是减小DB压力，增加并发能力，增加可用性。随处可见的缓存也可以证明他的重要性。当然mybaits也是支持缓存的，这里就介绍下mybatis缓存，它分为一级缓存和二级缓存，默认的实现都是内存缓存。

# 缓存开启
所有继承BaseExecutor没有改写query方法的都是开启了一级缓存，一级缓存的实现是PerpetualCache，内部就是一个hashMap。
二级缓存开关cacheEnabled，默认开启，所以如果需要配置二级缓存只需要在mapper中配置&lt;cache /&gt;

# 查询取值逻辑
一二级缓存取值逻辑在：org.apache.ibatis.executor.CachingExecutor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)，总的来说，就是先取二级缓存，取不到则取一级缓存，取不到则取数据库。

# 生命周期
DefaultSqlSession.executor.tcm=TransactionalCacheManager,所以cache对象的生命周期同sqlSession，但是缓存的内容将会在commit和rollback时清除，故一级缓存内容的生命周期其实是事务。

二级缓存之所以是全局的，因为Cache是在MappedStatement中

# 二级缓存介绍

在xml中加&lt;chache /&gt;就可以开启该mapper的二级缓存，里面各个properties分别会指定如下的几种缓存，[详见文档](http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html#cache)。
它们的包名是org.apache.ibatis.cache.decorators，mybatis把各种功能抽象成装饰器，需要一个功能就增加一种装饰。

- BlockingCache 利用concurrentHashMap存key的锁，如果获取到直接获取，没有获取到，阻塞，put解除阻塞
- FifoCache 非线程安全，内部linkedList存key，数量超过则去头
- LoggingCache 非线程安全，内部两个变量，请求数量（requests）和缓存命中数量（hits），打印命中率
- LruCache 非线程安全，内部持有自定义的linkedHashMap, 最近最少使用缓存组件
- ScheduledCache 非线程安全，核心操作都进行判断是否当前时间距离上次清除时间大于定时时间，如果大于则delegate.clear();
- SerializedCache 非线程安全，只是做序列化，map中实际存储的是序列化之后的内容
- SynchronizedCache 线程安全，每个方法都是sync， - =
- SoftCache 和 WeakCache 非线程安全，利用软弱引用的特性，实现在内存不够时自动回收的功能。
- TransactionalCache 所有缓存的入口，被TransactionalCacheManager调用，控制提交或者回滚，只有事务提交之后，二级缓存才会存入对应得缓存中，回滚当然是不会保存本次相关的缓存。

# 一个配置
如果在 mapper中 只配置了eviction="LRU"，则默认的委派链：TransactionalCache.delegate(=SynchronizedCache).delegate(=SerializedCache).delegate(=LruCache).delegate(=PerpetualCache,这是最底层的缓存实现，内部是map)，
cache的构建是在MapperBuilderAssistant#useNewCache中根据配置build的。

# 自定义二级缓存
完成下面步骤，就算是自定义实现了一个mybatis二级缓存了，可以根据自己得需要来填充逻辑。
1. 自定义一个org.apache.ibatis.cache.Cache实现类，要求有一个String作为参数得构造器，偷懒可以直接Copy一级缓存实现类（PerpetualCache）改下名字,这里叫MyCache
2. 在mybatis配置文件中配置
```xml
<typeAliases>
    <typeAlias alias="customerCache" type="org.apache.ibatis.personal.param.MyCache" />
</typeAliases>
```
3. 在mapper中配置缓存
```xml
<cache type="customerCache"></cache>
```
自定义二级缓存的委派链是：TransactionalCache.delegate = LoggingCache, LoggingCache.delegate = customerCache;也是在MapperBuilderAssistant#useNewCache中根据配置build的。


# 小总结
现在的服务都在分布式环境下，mybatis自带的二级缓存应该是没啥用了，需要使用就必须要自定义二级缓存。


# 讨论
一级缓存有毛用呢？在同一个sqlSession中，一般都是直接传递下去了吧。那岂不是99%都没有走一级缓存，虽然加了一级缓存的成本也比较低。

# 扩展&引用
- [装饰模式](http://www.runoob.com/design-pattern/decorator-pattern.html).
- [mybatis一级缓存](https://blog.csdn.net/luanlouis/article/details/41280959)
- [mybatis 官方文档](http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html#cache)