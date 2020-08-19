### ribbon 源码
以前同事在项目中使用了Spring Cloud Gateway，最近发现只有服务端稍微有报错，即便只有一个节点报错，结果所有节点的流量都会出现凹槽，
原因是spring.cloud.gateway.routes.uri配置是网关，不走负载均衡。修改为``lb://RETAIL-SETTINGS``后，会走。以前看过ribbon，
但忘得差不多了，再整一盘。。。

=======框架无穷多，每个都花大精力，不干事了！！！========

最近接入公司的灰度发布，是利用eureka做的服务发现，服务内部的转发应该是增加ribbon的实现实现的，就简单看下源码。

#### 版本信息
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-commons</artifactId>
    <version>1.2.2.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-netflix-core</artifactId>
    <version>1.3.1.RELEASE</version>
</dependency>

<dependency>
    <groupId>com.netflix.ribbon</groupId>
    <artifactId>ribbon</artifactId>
    <version>2.2</version>
</dependency>

#### 流程

##### 启动
容器启动时，通过 LoadBalancerAutoConfiguration、RibbonAutoConfiguration 两个AutoConfiguration 来实现ribbon的注册。

``` java
@Configuration
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(LoadBalancerClient.class)  // 这个bean就是RibbonAutoConfiguration 提供的
@EnableConfigurationProperties(LoadBalancerRetryProperties.class)
public class LoadBalancerAutoConfiguration {

    @LoadBalanced // 表示只针对标记了LoadBalanced的才会有负载均衡，原理见：@Qualifier说明
    @Autowired(required = false)
    private List<RestTemplate> restTemplates = Collections.emptyList();
    
    @Bean
    // 利用SmartInitializingSingleton 在bean初始化完之后，为RestTemplate增加拦截器
    public SmartInitializingSingleton loadBalancedRestTemplateInitializer(
            final List<RestTemplateCustomizer> customizers) {
        return new SmartInitializingSingleton() {
            @Override
            public void afterSingletonsInstantiated() {
                for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
                    for (RestTemplateCustomizer customizer : customizers) {
                        customizer.customize(restTemplate);
                    }
                }
            }
        };
    }
    
    @Autowired(required = false)
    private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();
    
    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerRequestFactory loadBalancerRequestFactory(
            LoadBalancerClient loadBalancerClient) {
        return new LoadBalancerRequestFactory(loadBalancerClient, transformers);
    }
`
	// XXX 这里忽悠了 重试相关的代码。
    @Bean
    public LoadBalancerInterceptor ribbonInterceptor(
            LoadBalancerClient loadBalancerClient,
            LoadBalancerRequestFactory requestFactory) {
        return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    // 给 RestTemplate 添加一个interceptor
    public RestTemplateCustomizer restTemplateCustomizer(
            final LoadBalancerInterceptor loadBalancerInterceptor) {
        return new RestTemplateCustomizer() {
            @Override
            public void customize(RestTemplate restTemplate) {
                List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                        restTemplate.getInterceptors());
                list.add(loadBalancerInterceptor);
                restTemplate.setInterceptors(list);
            }
        };
    }
}

```

##### 执行请求

上面bean注册的流程中可以看到，本质就是添加一个LoadBalancerInterceptor，源码如下，
``` java
// 最终其实还是调用的 LoadBalancerClient 的execute方法。
class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
    
    private LoadBalancerClient loadBalancer;
    private LoadBalancerRequestFactory requestFactory;
    
    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
            final ClientHttpRequestExecution execution) throws IOException {
        final URI originalUri = request.getURI();
        String serviceName = originalUri.getHost();
        Assert.state(serviceName != null, "Request URI does not contain a valid hostname: " + originalUri);
        return this.loadBalancer.execute(serviceName, requestFactory.createRequest(request, body, execution));
    }
}
```
其实最终委托的是LoadBalancerClient执行execute，这个LoadBalancerClient是容器提供的:在RibbonAutoConfiguration中创建的RibbonLoadBalancerClient

``` java
public class RibbonLoadBalancerClient implements LoadBalancerClient {
    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
        ILoadBalancer loadBalancer = getLoadBalancer(serviceId);
        // 调用的是 ILoadBalancer#chooseServer
        Server server = getServer(loadBalancer);
        if (server == null) {
            throw new IllegalStateException("No instances available for " + serviceId);
        }
        RibbonServer ribbonServer = new RibbonServer(serviceId, server, isSecure(server,
                serviceId), serverIntrospector(serviceId).getMetadata(server));
    
        return execute(serviceId, ribbonServer, request);
    }
    @Override
    public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException {
        // do sth
    }
}
```
默认的实现是ZoneAwareLoadBalancer，它初始化是在RibbonClientConfiguration#ribbonLoadBalancer。choose方法内部除了分区处理外，
本质还是调用BaseLoadBalancer#chooseServer，内部又是调用IRule#choose，

![IRule 类图(盗图)](https://pic2.zhimg.com/80/v2-549542dfe5ce8423918a497a6b18a8ff_720w.jpg)

每个实现的具体说明见：[Spring Cloud源码分析（二）Ribbon](https://zhuanlan.zhihu.com/p/31750966)


昨天翻东西，又翻到《spring cloud 微服务实战》，发现内容都有。。。


### 引用

- [Spring Cloud源码分析（二）Ribbon](https://zhuanlan.zhihu.com/p/31750966)
- 《spring cloud 微服务实战》翟永超 版


