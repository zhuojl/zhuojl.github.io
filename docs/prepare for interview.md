### 面试别人问题汇总
刚到公司的时候被叫着一起面试新人，当时没啥准备很匆忙，所以准备了这个文件，结果后面就没有面试了。。。哈哈哈哈，
不过雁过拔毛（？？）总得留点记录。。也可以留给自己以后用用。。

#### java基础
- hashMap
    - 数据结构
    - put过程
    - loadFactor的作用
    - putIfAbsent、merge 等函数的运用
    - 
    
- ConcurrentHashMap
    - 并发的支撑是什么
        - cas 
        - synchronized
    - 并发性能好的原因是什么
        - helpTransfer

- PriorityQueue
    - 二叉堆，查询第k个大的数
    - 适用场景
    - 数据结构
    
- 位图算法，排序   
    

#### 多线程相关

- 线程的状态

- wait、sleep、join的差别，对应什么状态

- ThreadLocal的用法，注意事项，底层实现

- 线程池参数说明，常用线程池，ArrayListQueue 和 LinkedListQueue区别，execute 和 submit的区别，底层实现

- 定时器的实现方式，选用什么定时器？ timer 和 scheduled

- sync 和 lock 的区别

- AQS，CountDownLatch，semaphore，CyclicBarrier某个源码的底层实现

- 线程调优？
    

#### 虚拟机相关
- jvm内存结构、JVM内存模型

- 常用回收算法、常用垃圾回收器

- 对象回收条件、逻辑

- 类加载机制，tomcat的类加载机制有了解吗，如何自定义类加载器

- 回收对象标记方法，能成为GcRoot的对象有哪些

- jvm常用配置参数，不用说英文名

- 有没有分析过线上服务内存或者cpu异常，如何处理的？如果发生了该怎么处理

- java自带命令有哪些，分别有什么作用

- jvm 调优


#### mysql

- 引擎有哪些，有什么特点，怎么选择

- sql查询流程

- b树索引和hash索引的区别（数据查询的方式），b树底层存储数据的数据结构，b+树的结构特点

- 创建索引时考虑些什么？假如有a，b，c三个查询字段，当有a，b组合，b，c组合，c单独查询时，索引如何创建

- 覆盖索引了解吗？sort ，group by 走索引吗

- 日常做sql性能优化吗？如何做

- explain主要看哪些属性？

- 事务，隔离级别，mysql默认隔离级别，底层实现


#### java框架
- spring
    - spring 启动
        - beanFactoryPostProcessor 和 beanPostProcessor
        - 举例常见的2个
        
    - spring bean 生命周期
    - spring boot 与 springMvc 差别
    - spring boot 如何自定义插件
    - 使用过程中遇到过什么问题
    - spring事务的传播性，aop的实现方式，有什么问题？为什么？怎么处理？

- mybatis
    - 加载
    - 请求流程
    - 插件扩展
    
    
#### 微服务
- 注册中心，调用中心，熔断降级限流

#### 分布式
- 分布式事务处理方式
    两阶段提交，3阶段提交，tcc（try confirm cancel），XA
- cap、base
- zookeeper 
    - 协议：zab，paxos
    - 角色
    
- redis 
    - 数据类型
    - 底层数据结构
    - 数据过期的处理方式
    - redis高可用方案

- 分布式锁
- 分布式主键生成器
- 缓存
    - 使用场景
    - 基于什么实现
    - 穿透/击穿避免 
    
- MQ 
    了解不够深入，就不问了

#### 设计模式
- 设计注意事项/原则
- 在项目中用过设计模式吗？都用过哪些
- 单例/命令模式/观察者/责任链等等 等随便一个解释下。

#### 其他

- 说一个项目中做的最好的项目，说下业务架构和技术架构，有没有改进的地方，如何改进 ===》自我表达时间

- 最近有看过什么书吗 ===》学习习惯

- 比较熟悉的框架/源码 ===》框架理解深度

- 有没有印象比较深的问题处理流程 ===》线上问题处理
