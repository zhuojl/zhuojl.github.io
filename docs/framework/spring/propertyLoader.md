### springboot 环境变量/配置
问题：
- springboot 配置的加载顺序是怎样的？
- springboot 配置相关源码大致长啥样？
- springboot 多环境配置的最佳实践是什么？

#### 配置的加载顺序是怎样的
加载顺序见[官方文档](https://docs.spring.io/spring-boot/docs/1.5.9.RELEASE/reference/htmlsingle/#boot-features-external-config)，
不用刻意记，记住大概的顺序就行。毕竟项目中不可能每个都用。官网还做了一些知识延伸，值得一看。

#### 配置相关源码
首先看几个重要的类：
- PropertyResolver
    - ConfigurablePropertyResolver
    - AbstractPropertyResolver
    - PropertySourcesPropertyResolver


- Environment extends PropertyResolver
    - AbstractEnvironment
    - ConfigurableEnvironment extends Environment, ConfigurablePropertyResolver
    - ConfigurableWebEnvironment extends ConfigurableEnvironment
    - MockEnvironment
    - StandardEnvironment
    - StandardServletEnvironment extends StandardEnvironment, implements ConfigurableWebEnvironment

- PropertySource  ``手法特别，关注一波``，实现很多，简单列几个
    - 1 in PropertySourcesPlaceholderConfigurer
    - AnnotationsPropertySource
    - CommandLinePropertySource
    - ComparisonPropertySource in PropertySource
    - CompositePropertySource
    - ConfigurationPropertySources in  ConfigFileApplicationListener
    - EnumerableCompositePropertySource
    - EnumerablePropertySource
    - JOptCommandLinePropertySource
    - JndiPropertySource
    - MapPropertySource
    - MockPropertySource
    - PropertiesPropertySource
    - RandomValuePropertySource
    - ResourcePropertySource
    - ServletConfigPropertySource
    - ServletContextPropertySource
    - SimpleCommandLinePropertySource
    - Source in DefaultApplicationArguments
    - StubPropertySource in PropertySource
    - SystemEnvironmentPropertySource


- MutablePropertySources implements PropertySources, extends Iterable<PropertySource<?>>
- PropertySourceLoader
    - PropertiesPropertySourceLoader 支持从xml或properties格式的文件中加载数据
    - YamlPropertySourceLoader 支持从yml或者yaml格式的文件中加载数据。

- EnvironmentPostProcessor

- PropertyPlaceholderConfigurer extends PlaceholderConfigurerSupport;

PlaceholderConfigurerSupport extends PropertyResourceConfigurer
		implements BeanNameAware, BeanFactoryAware;

PropertyResourceConfigurer extends PropertiesLoaderSupport
		implements BeanFactoryPostProcessor, PriorityOrdered;

Environment的构造以及PropertySource的生成

##### 扩展
spring cloud zk 配置加载核心实现：

ZookeeperPropertySourceLocator implements PropertySourceLocator;
ConfigFileApplicationListener implements EnvironmentPostProcessor, SmartApplicationListener, Ordered 处理配置

##### 属性绑定
通过ConfigurationPropertiesBindingPostProcessor 处理ConfigurationProperties，  
AutowiredAnnotationBeanPostProcessor处理value 来完成属性绑定。

拿属性，通过PropertySourcesPropertyResolver#getProperty(java.lang.String, java.lang.Class<T>, boolean)

#### 多环境配置的最佳实践

maven.resources.resource
maven.resources.filter

#### 引用
- [PropertyPlaceholderConfigurer](https://www.cnblogs.com/junzi2099/p/8042336.html)
- [PropertyPlaceholderConfigurer](https://blog.csdn.net/honghailiang888/article/details/51880312)
- [SpringBoot 多环境配置最佳实践](https://www.infoq.cn/article/Q-ese4CxV2IWmltsJcGX)
- [SpringBoot 配置环境的构造过程](https://fangjian0423.github.io/2017/06/10/springboot-environment-analysis/)
- [ConfigFileApplicationListener](https://my.oschina.net/u/1178126/blog/1822846)
- [spring 官文](https://docs.spring.io/spring-boot/docs/1.5.9.RELEASE/reference/htmlsingle/#boot-features-external-config)
