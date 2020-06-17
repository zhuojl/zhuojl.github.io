### spring kafka 源码阅读记录
最近公司老是kafka出问题，大部分还是由于客户端消费超过session time out，消费者频繁join group，一直re-balance。
对kafka是有些了解的，但是客户端是如何处理的，还不是很清楚，打算了解了解。首先看的就是spring-kafka(1.3.7 公司用)。

#### 消费者 注册
不同于spring-mybatis的处理，kafka消费者不需要暴露给其他bean，所以不需要注册到容器中。
完整 kafka消费者 的处理流程分为3个阶段：
1. KafkaListenerAnnotationBeanPostProcessor(BeanPostProcessor) 处理kafka SmartInitializingSingleton解析，注册KafkaListenerEndpoint到KafkaListenerEndpointRegistrar
2. SmartInitializingSingleton 在所单例bean初始化之后，设置一些属性，触发KafkaListenerEndpointRegistrar.registerAllEndpoints
3. 在finishRefresh()中调用SmartLifecycle.start() 启动kafka消费者

疑惑：KafkaListenerEndpointRegistrar 和 KafkaListenerEndpointRegistry 傻傻分不清，1、2为什么要分两步？

可以明显看出来的是，在第二步中通过beanFactory.getBean拿了KafkaListenerEndpointRegistry 和 KafkaListenerConfigurer，直接放在bean依赖中不行？

#### consumer 启动
上面启动kafka消费者，其实调用的是： KafkaMessageListenerContainer.doStart()..
拿messageListener；没有指定consumerExecutor就初始化一个``SimpleAsyncTaskExecutor``；初始化 ListenerConsumer，
交由AsyncListenableTaskExecutor执行任务。这里看下初始化ListenerConsumer部分。

```
// 因为实际代码太多了，占用篇幅太多，所以删除了，看这个文章时， 至少还是有源码的吧。。
ListenerConsumer(GenericMessageListener<?> listener, GenericAcknowledgingMessageListener<?> ackListener) {
    
    // 创建 Consumer
    final Consumer<K, V> consumer = KafkaMessageListenerContainer.this.consumerFactory.createConsumer(
            this.consumerGroupId, KafkaMessageListenerContainer.this.clientIdSuffix);
    this.consumer = consumer;

    // 创建 ConsumerRebalanceListener，监听 join group，Revoking（撤销）
    ConsumerRebalanceListener rebalanceListener = createRebalanceListener(consumer);

    // 订阅topic
    
    // 确认listener类型等
    
    // 设置taskScheduler，在containerProperties.getScheduler()没有设置时，初始化一个ThreadPoolTaskScheduler
   
    // 定时监控
    this.monitorTask = this.taskScheduler.scheduleAtFixedRate(
            new Runnable() {

                @Override
                public void run() {
                    // poll时间间隔是否超过阈值（containerProperties.getNoPollThreshold()），
                    // 超过则发送 NonResponsiveConsumerEvent
                    // 从目前的代码来看，似乎没有消费者。。
                    checkConsumer();
                }

            },
            this.containerProperties.getMonitorInterval() * 1000);
}

```

#### consumer 消费
消费见：KafkaMessageListenerContainer.ListenerConsumer#run()，完= =!
循环执行：处理position(offset)；poll()；处理数据；

当消费者要加入群组时，它会向担任群组协调器的broker发送一个JoinGroup请求。第一个加入群组的消费者将成为“群主”。
群主从协调器那里获得群组的成员列表（列表中包含了所有最近发送过心跳的消费者，它们被认为是活跃的），并负责给每一个消费者分配分区。
群主使用一个实现了PartitionAssignor接口的分类来决定哪些分区应该被分配给哪些消费者。

Kafka内置了两种分配分区的策略：
Rang：把主题的若干个连续分区分配给消费者。当分区数量无法被消费者数量整除时，先加入群组的消费者会分配到更多的分区。
RoundRobin：把主题的所有分区逐个分配给消费者。RoundRobin策略会给所有消费者分配相同数量的分区（或者最大就差一个分区）。
可以通过partion.assignment.strategy来配置，默认使用实现了Range策略的org.apach.kafka.clients.consumer.RangeAssignor,
也可以指定org.apach.kafka.clients.consumer.RoundRobinAssignor，还可以使用自定义策略。
分配完毕后，群主会把分配情况列表发给群主协调器，协调器在把这些信息发给所有消费者。每个消费者只能看到自己的分配消息，
只有群主知道群组里所有消费者的分配信息。这个过程会在每次再均衡时重复发生。



#### 额外收获

- 设置 AbstractKafkaListenerContainerFactory#batchListener 实现批量消息接受
- 在不指定任何taskExecutor的情况下，一个消费者至少3个线程：消费者线程、监控线程、协调器线程
- 一些bean启动或者加载需要消耗很多资源或者影响很多东西，可以通过SmartLifecycle 后置处理，
避免先启动后其他bean处理失败而处理失败流程


======== 2020-06-17 更新 ==========
我是谁，我在哪，这是我写的吗。。。。。。。


