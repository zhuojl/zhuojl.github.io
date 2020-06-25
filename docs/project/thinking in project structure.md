### 项目结构的思考
### 背景

一般来说我们的项目都是web服务，但是有时候有不止web服务，比如为避免web应用频繁更新影响定时任务，
或者一些核心的定时任务需要单独的程序处理，需要单起服务专门做定时任务。在现有的结构中，都是在web层做一套配置，
如果新开定时服务，还需要再做一套配置，copy做多份配置总是让人难以接受的

#### 现状

项目划分为两块，一块web，一块其他业务逻辑，配置放置在web层
    
    - web（web服务模块，引用biz模块）
        - controller
        - ...
    - biz（业务模块）
        - rpc
        - MQ-consumer
        - MQ-producer
        - mysql
        - mongo
        — redis
        - ...
        - service
            - service1
            - service2
            - ... 
        - schedule（）
            - schedule1
            - schedule2
        - ... 

#### 期待

首先需要将项目做个划分，一个项目无外乎有这几个部分组成，
1. 对外部提供服务的入口，包括controller，MQ消费者等，作为入口接受外部指令或者事件
2. 请求外部数据，包括RPC请求，MQ生产者，Mysql等
3. 内部业务处理
4. 定时任务（大任务，或者统一管理任务），这类差不多也可以算作是入口，但是定时任务到后期往往需要单独成服务，
避免对常规业务处理造成性能影响
        
并且各自的配置，都配置在相应模块，在新入口（如定时任务）接入时，就不用copy配置，单元测试也可以分模块做，而且项目结构清晰！
不过分层也会有引入一个新问题，层与层之间通信问题，继而转变为实体转换、应该存在于什么位置的问题？？
全部放在service层中？这样就和上面的原始结构一致，不能分层；再往前一步，所有能往下沉入的都放置在下层，
依次是 depend -》service -》entrance/schedule.

实体分类（引用自阿里巴巴编码规范）
- DO（Data Object）：与数据库表结构一一对应，通过 DAO 层向上传输数据源对象。
- DTO（Data Transfer Object）：数据传输对象，Service 或 Manager 向外传输的对象。
- BO（Business Object）：业务对象。由 Service 层输出的封装业务逻辑的对象。
- AO（Application Object）：应用对象。在 Web 层与 Service 层之间抽象的复用对象模型，极为贴近展示层，复用度不高。
- VO（View Object）：显示层对象，通常是 Web 向模板渲染引擎层传输的对象。
- Query：数据查询对象，各层接收上层的查询请求。注意超过 2 个参数的查询封装，禁止使用 Map 类来传输。

在此基础上，调整结果（名词有待商榷）

    - entrance
        - controller
            - AO（应用对象。不过一般不太会有这个，可直接使用service层的DTO）
            - VO（显示层对象，转换DTO、BO信息为显示层信息）
        - MQ-consumer
            - AO？或者DTO，用于接受对象
        - ...
    - depend
        - rpc
            - QO（数据查询对象，各层接收上层的查询请求）
            - VO（值对象）
        - MQ-producer
            - DTO
        - mysql
            - DO（与数据库表结构一一对应，通过 DAO 层向上传输数据源对象）
            - QO（数据查询对象，各层接收上层的查询请求。）
            - VO（值对象）
        - mongo、redis
            - 同mysql
        - ...
    - service（这一层最好还是按业务划分包，将相同业务的内容划分到一起）
        - QO（数据查询对象，各层接收上层的查询请求）
        - BO（service处理结果实体）
        - DTO（传输到下层，等待下层做逻辑处理）
        - service1
        - service2
        - ... 
    - manager（多个service中间有交互时，需要引入manager）
        - QO（数据查询对象，各层接收上层的查询请求）
        - BO（service处理结果实体）
        - DTO（传输到下层，等待下层做逻辑处理）
        - service1
        - service2
        - ... 
    - schedule（这一层几乎没有实体，往往作为空方法发起调起）
        - schedule1
        - schedule2
        - ...


需要注意的是：
1. 查询和结果都禁止使用map，转换可以发生在内存中
2. 随着实体的增加，势必会需要对象之间的转换，如果将这些放置到service中，往往会增大service类，导致service类臃肿，可以考虑增加转换类，
或者在结果对象中增加函数，用于处理对象间的转换。这类对象放置到相应的实体附近。
3. 在未分层的应用中，尽量不要让下层依赖上层实体，避免以后分层改造。
4. 在功能简单的时候，如简单查询，可以直接使用mysql的QO/DTO，VO完成一个web查询功能


============分割线，更新===========

上面的mysql/mongo应该跟着对应的领域，redis/mqProducer应该属于基础应用层，rpc可以划分为service或者基础组建，对外提供服务

