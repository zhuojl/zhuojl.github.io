## 消息快于事务问题

### 背景

```
class Demo {
    @Transaction
    public void test() {
        kafkaTemplate.send(key, data);
    }
}
```
在现在的系统中，随处可见这样的代码结构，这样可能会出现消息快于事务的问题，导致消息已经被消费，但是事务却还没有被提交，
特别是如果当前方法逻辑复杂，几乎是必现。
在之前与外部同事联调时，遇到了这个问题，消费kafka消息之后，查询不到数据，他最后以工期太赶，没有做该问题修复。

### 解决办法
1. 将消息放置在事务之外。消息提交成功之后再发送消息。
2. 利用spring事务处理，通过以下方式实现。
```
TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override
    public void afterCommit() {
        
    }
});
```
3. 在方法二的基础之上，springKafka 封装了一个实现KafkaResourceSynchronization，可以通过简单配置实现
```
@Bean
public ProducerFactory<Integer, String> producerFactory() {
    DefaultKafkaProducerFactory factory = new DefaultKafkaProducerFactory<>(senderProps());
    factory.transactionCapable();
    factory.setTransactionIdPrefix("tran-");
    return factory;
}
```

源码见：
```
org.springframework.kafka.core.KafkaTemplate#getTheProducer
org.springframework.kafka.core.ProducerFactoryUtils#getTransactionalResourceHolder
org.springframework.kafka.core.ProducerFactoryUtils#bindResourceToTransaction

```

