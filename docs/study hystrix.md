# hystrix 学习

## 写在前面

项目引用了hystrix，但是没有很好的用起来，相比服务注册中心，客户端负载均衡，hystrix可能更重要，因为服务总是有不可用的时候，
如果没有熔断/降级措施等措施，那么很可能让整个服务不可用。
### 目的
其实关于hystrix的资料也很多，写这个主要是为了整理一些自己感兴趣的点，学习以及后续回顾，毕竟再去看哪些资料梳理也是要时间的
### 需要知道的背景
hystrix已经不再开发新功能，目前处于维护状态，官方建议老项目继续使用hystrix，新项目可以考虑使用新框架[resilience4j](https://github.com/resilience4j/resilience4j),
## What does it do [源](https://github.com/Netflix/Hystrix#what-does-it-do)
1) 延迟和故障容忍：阻止级联（cascading）异常；后退和优雅的降级；快速恢复失败；使用断路器隔离 线程和信号量
2) 准实时监控和更改配置，告警，决策等
3) 并行执行；并发缓存；通过合并（collapsing）请求处理批量请求

## How-it-Works [源](https://github.com/Netflix/Hystrix/wiki/How-it-Works)
[文中](https://github.com/Netflix/Hystrix/wiki/How-it-Works)先梳理流程，序列，再详细讲了断路器/隔离/线程和线程池/请求合并/请求缓存的原理。
这些内容在《spring cloud 微服务实战》(下文中简称：书)中都有大致说到，不过内容基本都是官方文档的翻版吧。

### command请求流程 
[图](https://raw.githubusercontent.com/wiki/Netflix/Hystrix/images/hystrix-command-flow-chart.png)，书中的图很模糊。
HystrixCommand与HystrixObservableCommand的方法中，底层调用的都是Observable 的实现
- execute() — blocks, then returns the single response received from the dependency (or throws an exception in case of an error)
- queue() — returns a Future with which you can obtain the single response from the dependency
- observe() — subscribes to the Observable that represents the response(s) from the dependency and returns an Observable that replicates that source Observable
- toObservable() — returns an Observable that, when you subscribe to it, will execute the Hystrix command and emit its responses
### 断路器
[图](https://raw.githubusercontent.com/wiki/Netflix/Hystrix/images/circuit-breaker-1280.png)
The precise way that the circuit opening and closing occurs is as follows:

1. Assuming the volume across a circuit meets a certain threshold (HystrixCommandProperties.circuitBreakerRequestVolumeThreshold())...
2. And assuming that the error percentage exceeds the threshold error percentage (HystrixCommandProperties.circuitBreakerErrorThresholdPercentage())...
3. Then the circuit-breaker transitions from CLOSED to OPEN.
4. While it is open, it short-circuits all requests made against that circuit-breaker.
5. After some amount of time (HystrixCommandProperties.circuitBreakerSleepWindowInMilliseconds()), the next single request is let through (this is the HALF-OPEN state). 
If the request fails, the circuit-breaker returns to the OPEN state for the duration of the sleep window. If the request succeeds, 
the circuit-breaker transitions to CLOSED and the logic in 1. takes over again.

### 隔离
hystrix使用舱壁模式做隔离，即在THREAD模式下为每个group单独分配线程或者为semaphore模式下单独分配信号量


#### 线程和线程池
选择线程和线程池做隔离的原因(机翻，没太看懂。。)：
- 许多应用程序针对由许多不同团队开发的数十种不同服务执行数十种（有时甚至超过100种）不同的后端服务调用。
- 每种服务都提供自己的客户端库。
- 客户端库一直在变化。
- 客户端库逻辑可以改变以添加新的网络调用。
- 客户端库可以包含重试，数据解析，缓存（内存或跨网络）等逻辑，以及其他此类行为。
- 客户端库往往是“黑盒子” - 不透明他们的用户关于实现细节，网络访问模式，配置默认值等。
- 在几个真实的生产中断中，确定是“哦，某些东西改变了，属性应该调整”或“客户端库改变了它的行为。”
- 即使客户端本身不会改变，服务本身可以改变，这可能会影响性能特征，从而导致客户端配置无效。
- 传递依赖性可以引入其他客户端库不是预期的，可能没有正确配置。
- 大多数网络访问是同步执行的。
- 客户端代码中也可能出现故障和延迟，而不仅仅是在网络调用中

线程池的收益（机翻）：
- 该应用程序完全受到失控客户端库的保护。给定依赖库的池可以填满，而不会影响应用程序的其余部分。
- 应用程序可以接受风险低得多的新客户端库。如果出现问题，它将被隔离到库中并且不会影响其他所有内容。
- 当失败的客户端再次变得健康时，线程池将清除并且应用程序立即恢复正常性能，而不是长时间恢复整个Tomcat容器不堪重负。
- 如果客户端库配置错误，线程池的运行状况将很快证明这一点（通过增加的错误，延迟，超时，拒绝等），你可以处理它（通常是通过实时的动态属性），而不影响应用程序功能。
- 如果客户端服务改变了性能特征（这通常发生在一个问题上），这反过来导致需要调整属性（增加/减少超时，更改重试等），
这再次成为通过线程池指标（错误，延迟，超时，拒绝）可见，并且可以在不影响其他客户端，请求或用户的情况下进行处理。
- 超越隔离优势，拥有专用线程pools提供内置并发性，可用于在同步客户端库之上构建异步外观（类似于Netflix API如何在Hystrix命令之上构建一个被动的，完全异步的Java API）。

简而言之，线程池提供的隔离允许优雅地处理客户端库和子系统性能特征的不断变化和动态组合，而不会导致中断。
注意：尽管隔离是单独的线程提供的，但您的底层客户端代码应该还有超时和/或响应线程中断，因此它无法无限制地阻塞并使Hystrix线程池饱和。

线程池的缺点：
线程池的主要缺点是它们增加了计算开销。每个命令执行都涉及在单独的线程上运行命令所涉及的排队，调度和上下文切换。
Netflix在设计此系统时，决定接受此开销的成本以换取它提供的好处并认为它是次要的足以不会产生重大的成本或性能影响。

#### Semaphores
没有使用Semaphore, 简单使用atomic，超过原子变量就拒绝，不支持超时，超时其实是依赖object.wait或者LockSupport.park实现的，这样就需要多线程/线程池。。。


### 请求合并和缓存忽略
略，不是重点

## 与spring boot结合
略，不是重点


## 常见疑问 [源](https://github.com/Netflix/Hystrix/wiki/FAQ%20:%20General)




## 引用

- [hystrix.README](https://github.com/Netflix/Hystrix/blob/master/README.md)
- [hystrix.wiki](https://github.com/Netflix/Hystrix/wiki)
- 《spring cloud 微服务实战》（翟永超版）
- [随便看的分享](https://www.jianshu.com/p/6c574abe50c1)