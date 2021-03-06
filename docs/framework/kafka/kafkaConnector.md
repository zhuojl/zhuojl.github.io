### kafka管道 小记录
#### 构建数据管道需要考虑的问题
- 及时性，kafka基于流的数据平台，提供了可靠可伸缩的数据存储，kafka可以扮演缓冲区，降低了生产者和消费者的时间敏感度
- 可靠性，Connect API 集成外部系统提供的处理偏移量的API，实现仅有一次（本来支持的是仅有一次）的端到端的数据管道。
- 高吞吐量和动态吞吐量，kafka解藕生产者，消费者，可挤压
- 数据格式，kafka 和 connect API数据格式无关，生产者和消费者可以使用各种序列化器来表示任意格式的数据
- 数据转换，数据管道的构建分为两个阵营，ETL（提取-转换-加载，Extract-Transform-Load）和ELT（Extract-Load-Transform），数据转换和数据持有处各不相同
- 安全性，kafka支持加密和认证
- 故障处理能力，保存数据，可以重做
- 耦合性和灵活性


#### 如何在Connect API 和 普通生产者和消费者之间选择？
如果没有业务逻辑，可以直接与存储系统交互，可以考虑Connect，它提供了一些开箱即用的特性：配置管理，偏移量管理，并行处理，错误处理。

### 其他
书上说 完整的Connect可以写一本书。。。。

### 疑问
- kafka挂了会咋样。本身难挂，和很多业务系统一样？
- 顺序性如何保证？只有一个partition？
- kafka默认ack=1，本身就可能丢消息，作为通道丢消息了咋办。。ack=all？？

