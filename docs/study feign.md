### feign调研

虽然之前就看过feign，可是为了工作更加效率, 维护更方便, 引入时还是需要看下做个调研. 
当然公司很多同事都做了这块的文档, 有默认执行流程的, 有简单使用指南的, 还有优化的(目前空白= =), 
这里自己除了资料汇总, 其他什么都像拾人牙慧. 所以这里不做了, 本篇完 = =

### 流程
feign的源码其实很少,  核心库只有50个类左右, 通过[官方例子](https://github.com/OpenFeign/feign/blob/master/example-github/src/main/java/example/github/GitHubExample.java)可以很快熟悉整个执行流程. 

1. 为 ``建造者`` feign.Feign.Builder 填充属性
2. Builder.build() 构造 ``工厂类`` feign.Feign
3. feign.Feign#newInstance 工厂类构建生成``代理类``
4. 代理类执行方法

### 源码
feign.Feign.Builder
``` 
/**
 * 请求链接器，在{@link feign.Target#apply(feign.RequestTemplate)}之前执行，
 * 用于http增强，过滤修改等。
 */
private final List<RequestInterceptor> requestInterceptors =
    new ArrayList<>();

/**
 * 日志级别
 */
private Logger.Level logLevel = Logger.Level.NONE;

/**
 * Defines what annotations and values are valid on interfaces.
 *
 * 通过方法{@link Contract#parseAndValidateMetadata(java.lang.Class)},将类解析为一组方法元数据 {@link feign.MethodMetadata}
 * 其实就是通过这个类来做类解析,它决定了接口方法书写规则.
 * 默认实现：
 * {@link Contract.Default}
 *
 * 其他实现：
 * {@link feign.hystrix.HystrixDelegatingContract}
 * {@link feign.spring.SpringContract}
 */
private Contract contract = new Contract.Default();

/**
 * 提交请求的客户端，实现者需要保持线程安全, feign抽象出一个feign.Request类,包括了http请求的各个部分(请求方法, 请求url, 请求头, 请求体)和一个feign.RequestTemplate
 * 各个Client实现中, 将Request对象转换为自己需要的请求, 从而完成client的扩展.
 *
 * 默认实现：
 * {@link Client.Default}
 * 其他实现：
 * {@link feign.okhttp.OkHttpClient}
 * {@link feign.httpclient.ApacheHttpClient}
 * ...
 */
private Client client = new Client.Default(null, null);

/**
 * 如字面意思，重试器，默认100ms一次，1秒内最多重试5次。
 */
private Retryer retryer = new Retryer.Default();

private Logger logger = new NoOpLogger();

/**
 * 编码器，默认只能处理String和byte[]
 * json编码器 GsonEncoder、JacksonEncoder
 * 其他见源码
 */
private Encoder encoder = new Encoder.Default();

/**
 * 解码器，默认可以解析String和byte[]
 * json编码器 GsonEncoder、JacksonEncoder
 *
 * OptionalDecoder 可以处理Optional出参
 * StreamDecoder 难用 = =
 */
private Decoder decoder = new Decoder.Default();

/**
 * 将传入的实体转换为查询参数
 * 默认两个实现， 差别：
 * @see FieldQueryMapEncoder
 *  {@link feign.querymap.FieldQueryMapEncoder.ObjectParamMetadata#parseObjectType(java.lang.Class)}
 * @see BeanQueryMapEncoder
 *  {@link feign.querymap.BeanQueryMapEncoder.ObjectParamMetadata#parseObjectType(java.lang.Class)}
 *
 */
private QueryMapEncoder queryMapEncoder = new QueryMapEncoder.Default();

/**
 * 异常解析，字面意思。。。。
 */
private ErrorDecoder errorDecoder = new ErrorDecoder.Default();

/**
 * Controls the per-request settings currently required to be implemented by all {@link Client
 * clients}
 */
private Options options = new Options();

/**
 * 动态代理工厂类，用于定制{@link InvocationHandler}
 * 如{@link feign.hystrix.HystrixInvocationHandler}的生成
 */
private InvocationHandlerFactory invocationHandlerFactory =
    new InvocationHandlerFactory.Default();

private boolean decode404;
private boolean closeAfterDecode = true;

/**
 * 异常传播策略，
 * NONE 没有
 * UNWRAP 解除RetryableException包装，抛出底层异常
 */
private ExceptionPropagationPolicy propagationPolicy = NONE;

```

```
public interface Target<T> {

  /* 代理的接口类 */
  Class<T> type();

  /* target名称 */
  String name();

  /* url的baseUrl, 如https://api/v2 */
  String url();

  /**
   * Targets a template to this target, adding the {@link #url() base url} and any target-specific
   * headers or query parameters. <br>
   * <br>
   * For example: <br>
   * 
   * <pre>
   * public Request apply(RequestTemplate input) {
   *   input.insert(0, url());
   *   input.replaceHeader(&quot;X-Auth&quot;, currentToken);
   *   return input.request();
   * }
   * </pre>
   * This call is similar to {@code
   * javax.ws.rs.client.WebTarget.request()}, except that we expect transient, but necessary
   * decoration to be applied on invocation.
   */
  public Request apply(RequestTemplate input);
}
```
Target主要方法是apply,用于产生一个feign.Request, 默认实现类:feign.Target.HardCodedTarget, 
其他实现有:
feign.ribbon.LoadBalancingTarget,在feign.Request构建时, 增加了ribbon逻辑,重分配ip、host.

### 整体感受
大量的面向接口编程, 并在接口处提供了默认实现, 在Builder中提供了默认实现, 也可以修改属性来修改行为, 以便对扩展开放.
设计模式方面:
1. 在feign的构造中, 使用建造者模式, 将属性填充到Builder中, 通过build方法构建Feign, 隐藏构造过程.
2. 装饰或者代理, RibbonClient 用于功能增强, HystrixDelegatingContract,  用于属性修正. 
3. 模版方法, 策略...
### 接入
1. pom配置
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>1.4.3.RELEASE</version>
</dependency>
```
2. 通过注解EnableFeignClients引入.
3. 接口方法常见用法
方法可用注解(SpringQueryMap、PathVariable、RequestHeader、RequestParam), 常见用法如下, 其他见[官方示例](https://github.com/spring-cloud/spring-cloud-openfeign/blob/master/spring-cloud-openfeign-core/src/test/java/org/springframework/cloud/openfeign/support/SpringMvcContractTests.java)

```
将参数全部转换为请求url
@RequestMapping(value = "/test", method = RequestMethod.GET)
ResponseEntity<TestObject> getTest(@RequestParam Map<String, String> params);


请求头修改
@RequestMapping(path = "/headerMap")
String headerMap(@RequestHeader MultiValueMap<String, String> headerMap,
      @RequestHeader(name = "aHeader") String aHeader);

请求参数修改
String CUSTOM_PATTERN = "dd-MM-yyyy HH:mm";
@RequestMapping(method = RequestMethod.GET)
String getTest(@RequestParam(name = "localDateTime") @DateTimeFormat(
      pattern = CUSTOM_PATTERN) LocalDateTime localDateTime);

读取SpringQueryMap中部分参数作为queryParameter, 如果不加后面的RequestParam就是全部属性作为url参数
@RequestMapping(path = "/queryMapObject?aParam3={aParam3}")
String queryMapObject(@SpringQueryMap TestObject queryMap,
      @RequestParam(name = "aParam") String aParam, @RequestParam(name = "aParam1") String aParam1);
```

4. 参数配置
相关配置及用法见: org.springframework.cloud.openfeign.FeignClientProperties, 
默认配置见相关Builder 及 org.springframework.cloud.openfeign.FeignClientFactoryBean#configureUsingConfiguration
可以通过propertis增加配置, 也可以通过注入相关bean来修改配置, 如:
```
feign.client.config.defalut.logger-level=full 默认日志级别;
feign.client.config.my-service.logger-level=full my-service的日志级别;
@Bean
public Request.Options options() {
     return new Request.Options();
}
```
5. 项目中场景处理
    1. 分区设置 ==> 通过@RequestHeader处理, 不用线程传递
    2. 部分接口有请求头的概念 ==> 增加代理实现, 类似spring-cloud-feign源码中[分页实现﻿](https://github.com/spring-cloud/spring-cloud-openfeign/blob/master/spring-cloud-openfeign-core/src/main/java/org/springframework/cloud/openfeign/support/PageableSpringEncoder.java)

6. 注意事项
不要使用PathVariable注解, 这样请求链接是动态的, 公司出现过因为链接动态,所以统计时是按不同联机统计的,因而出现内存问题, 等找到链接copy到这.

### 参考
https://github.com/spring-cloud/spring-cloud-openfeign
https://github.com/OpenFeign/feign

