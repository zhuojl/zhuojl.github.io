### 小知识

#### org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
多线程执行任务的日志断层，不方便关联，通过配置taskDecorator在日志上下文中记录父类线程的名称，可以实现。
<property name="taskDecorator">
    <bean class="org.springframework.core.task.TaskDecorator"/>
</property>

log4j2的日志上下文处理：org.apache.logging.log4j.ThreadContext.put
logback的日志上下文处理：org.slf4j.MDC#put

日志格式中，在线程附近增加配置 [%X{theParentThread}

#### 系统jvm参数设置

-Xms,  一般是2-4g
-Xmx,  一般同Xms一样，避免申请内存时间的影响，
-XX:MetaspaceSize, 一般256m、512m，  
-XX:MaxMetaspaceSize，一般同 MetaspaceSize一样，以前因为引入一个guava的一个bug，导致一致oom却无法回收的问题
-Xss256k,  
-XX:MaxTenuringThreshold,  晋升年龄，默认15，
-XX:ParallelGCThreads， gc线程数，最近因为jdk问题，导致线程数量过多，通过修改此参数=4可以解决。
-Xnoclassgc,  
-XX:+UseParNewGC,  年轻代回收器，serialNew？
-XX:+UseConcMarkSweepGC,   老年代回收器，为延缓响应时间，一般设置CMS
-XX:CMSInitiatingOccupancyFraction=75,  老年代内存到达多少时触发fgc
-XX:+CMSParallelRemarkEnabled,  
-XX:+UseCMSCompactAtFullCollection,  在FULL GC的时候， 对年老代的压缩
-XX:CMSFullGCsBeforeCompaction=0, 多少次fgc触发一次compact操作，因为 cms 使用标记清除，所以会有碎片，通过compact清理碎片
-XX:+PrintGCDateStamps,  打印gc时间戳
-XX:+PrintGCDetails,  打印gc详情
-XX:+PrintGCTimeStamps,  打印gc时间戳
-XX:+PrintHeapAtGC,  gc时打印
-Xloggc:/JVMGC.log,  
-XX:+CMSClassUnloadingEnabled,  
-Djava.awt.headless=true,  
-Duser.timezone=GMT+08,  
-XX:+UnlockExperimentalVMOptions,  
-XX:+UseCGroupMemoryLimitForHeap

引用：
- http://www.51gjie.com/java/551.html

