### 项目结构的思考
### 背景
一般来说我们的项目都是web服务，但是有时候有不止web服务，还有定时任务，kafka消费等。目前大部分项目的结构，都是在web层做一套配置，
如果新建一个定时任务的模块，还需要在定时任务模块中再copy一套配置（mybatis，数据源链接等等）。


#### 现状
目前项目划分为两块，一块web，一块其他业务逻辑，容器配置放置在web层

- web（MODULE，web服务模块，引用biz模块）
    - controller
    - config （做spring bean 配置的地方，我们项目中配置了feign，mybatis，线程池，kafka，redis等（。。。））
- biz（MODULE，业务模块）
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
    - schedule
        - schedule1
        - schedule2
    - ...

#### 预期
首先需要将项目做个划分，一个项目无外乎有这几个部分组成，
1. 对外部提供服务的入口，包括controller，MQ消费者等，作为入口接受外部指令或者事件
2. 请求外部数据，包括RPC请求，MQ生产者，Mysql等
3. 内部业务处理
4. 定时任务（大任务，或者统一管理任务），这类差不多也可以算作是入口，但是定时任务到后期往往需要单独成服务，
避免对常规业务处理造成性能影响

为了方便测试，扩展模块（如定时任务或者服务级模块），各个模块能独立配置，就不用copy配置，单元测试也可以分模块做，而且项目结构清晰！
示例：
- web（MODULE，web服务模块，引用biz模块）
    - controller
    - config （只做controller的配置）
- biz（MODULE，业务模块，如果这一层不细分，多个分散的config就不要）
  - feign
    - config
  - MQ-consumer
    - config
  - dao
    - config
  - service
    - config
- schedule
  - config

#### 分层问题
不过分层也会有引入一个新问题，层与层之间通信问题，继而转变为实体转换、应该存在于什么位置的问题？？
全部放在service层中？这样就和上面的原始结构一致，不能分层；或者往前一步，所有能往下沉入的都放置在下层，这样就能够满足上层依赖的问题。
但是新问题又产生了，``层与层之间基本都需要做实体转换，大量属性/字段冗余``


假设我们接受``字段冗余，大量转换``这个情况，先来看看常规实体分类（引用自阿里巴巴编码规范），讨论模块划分的问题
- DO（Data Object）：与数据库表结构一一对应，通过 DAO 层向上传输数据源对象。
- DTO（Data Transfer Object）：数据传输对象，Service 或 Manager 向外传输的对象。
- BO（Business Object）：业务对象。由 Service 层输出的封装业务逻辑的对象。
- AO（Application Object）：应用对象。在 Web 层与 Service 层之间抽象的复用对象模型，极为贴近展示层，复用度不高。``所以一般都只用VO``
- VO（View Object）：显示层对象，通常是 Web 向模板渲染引擎层传输的对象。
- Query：数据查询对象，各层接收上层的查询请求。注意超过 2 个参数的查询封装，禁止使用 Map 类来传输。
- Req：一般用来接受请求，或者rpc时发起对外部的请求

在此基础上，暂定这个结构，所有定义在上层的对象，当业务简单时，都可以往下沉。

- controller
  - Req
  - VO（显示层对象，一般项目中用Resp对象）
- MQ-consumer
    - DTO（或者*Entity），接受对象
- rpc 可以是获取用户信息，权限等接口，是基础设施的一部分，没必要抽公共模块
    - Req（数据查询对象，各层接收上层的查询请求）
    - DTO（可以用dto接受参数，也可以用Resp，一般响应体都有外部包装，像Response<*Resp> 或者 Response<*DTO>接）
- MQ-producer  TODO
    - DTO
- DAO
    - DO（与数据库表结构一一对应，通过 DAO 层向上传输数据源对象），扩展DO，让他具备附带查询字段的能力，这样，减少定义QO，有个缺点就是：为了支持扩展，父类持有的是Map
    - QO（数据查询对象，各层接收上层的查询请求。）
    - VO（值对象）
    - mapper
    - mongo
    - redis
- service（这一层最好还是按业务划分包，将相同业务的内容划分到一起）
    - QO（数据查询对象，各层接收上层的查询请求）
    - BO（service处理结果实体）
    - DTO（传输到下层，等待下层做逻辑处理）
    - service1
    - service2
    - ...
- manager（多个service中间有交互时，需要引入manager，做业务管理用）
    - QO（数据查询对象，各层接收上层的查询请求）
    - BO（manager 层传递的实体）
    - DTO（传输到下层，等待下层做逻辑处理）
    - manager1
    - manager2
    - ...
- schedule（这一层几乎没有实体，往往作为空方法发起调起）
    - schedule1
    - schedule2
    - ...


需要注意的是：
1. 随着层的设定，势必会需要对象之间的转换，如果将这些放置到service中，往往会增大service类，导致service类臃肿，可以考虑增加Converter，
或者在结果对象中增加函数，用于处理对象间的转换。在老东家的时候，大佬们喜欢在实体中加静态方法，用于处理对象转换，相比单独创建一个有工具方法的
工具类有两个好处：1、处理逻辑靠近实体，更方便看。 2、避免创建过多的转换工具类；例：
```java
// 从B中拿属性，构造A
class A {
    private String name;
    public static A from(B b) {
        return new A(b.anotherName);
    }
}

class B {
    private String anotherName;
}
```
2. 在未分层的应用中，尽量不要让下层依赖上层实体，避免以后分层改造；
~~所接触的互联网系统中，非分布式服务已经很难找了，所以一开始就可以考虑分层，当然也不是绝对的。~~
为服务划分是业务领域划分，跟分层关系不大。
3. 在业务简单，没有增加字段的情况下，上层可以直接使用下层，如果上层需要增加字段，不能直接改下层。
这个不好实施，在后续增加字段时，容易被随便改到，所以还是分开使用比较好。``可以考虑上层继承下层的实践``(待验证)

#### 分场景

##### 极简场景
简单curd，表中有什么就返回什么，不需要多的实体。接受一个DO到底,dao.do
##### 有细微业务场景
表中往往只存id，需要填充名称类等，或者需要屏蔽一些信息，增加实体：service.bo？
##### 复杂的业务场景
需要聚合多个service的时候，需要在manager中聚合，增加实体：manager.bo

do->bo->vo,req->bo->do还是不可少阿。。。

#### 领域划分来说

TODO


