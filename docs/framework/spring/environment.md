### springboot 环境变量/配置
问题：
- springboot 配置的加载顺序是怎样的？
- springboot 配置相关源码大致长啥样？
- springboot 多环境配置的最佳实践是什么？

PS:当前springboot版本 1.5.9
#### 配置的加载顺序是怎样的
加载顺序见[官方文档](https://docs.spring.io/spring-boot/docs/1.5.9.RELEASE/reference/htmlsingle/#boot-features-external-config)，
不用刻意记，记住大概的顺序就行。毕竟项目中不可能每个都用。官网还做了一些知识延伸，值得一看。

#### 配置相关源码
首先看几个重要的类：
- PropertyResolver 它是spring 属性处理的接口，[PropertyResolver简单解析](https://my.oschina.net/lixin91/blog/670374)

- Environment extends PropertyResolver 是Spring对当前程序运行期间的环境的封装，主要提供了两大功能：profile和property(父接口PropertyResolver提供)
    - ConfigurableEnvironment extends Environment, ConfigurablePropertyResolver
    - AbstractEnvironment implements ConfigurableEnvironment
    - ConfigurableWebEnvironment extends ConfigurableEnvironment
    - MockEnvironment    单元测试 mock
    - StandardEnvironment extends AbstractEnvironment 标准环境
    - StandardServletEnvironment web环境 extends StandardEnvironment, implements ConfigurableWebEnvironment

- PropertySource 属性源，Environment中持有的属性
常见的实现很多，简单列几个
    - JOptCommandLinePropertySource java option
    - MapPropertySource map的属性
    - PropertiesPropertySource Property的属性
    - RandomValuePropertySource 随机的属性
    - SimpleCommandLinePropertySource 命令行的属性
    - StubPropertySource in PropertySource ？？？
    - SystemEnvironmentPropertySource 系统环境变量 属性
```
public abstract class PropertySource<T> {
    // 看这个结构，是不是可以无限套娃 = =
    protected final T source;
}
```

- MutablePropertySources 持有多个PropertySource，StandardEnvironment中持有它，implements PropertySources, extends Iterable<PropertySource<?>>

- EnvironmentPostProcessor 环境后置处理器，实现的也有很多，其中 ConfigFileApplicationListener 就是springboot处理配置的核心，

##### 总结
[阿里fang jian大佬的分享](https://fangjian0423.github.io/2017/06/10/springboot-environment-analysis/)写的很好，学习！！！

总的来说，springboot中的环境是 StandardServletEnvironment 或者 StandardEnvironment，它的类图如下，可以通过***增加PropertyResource，
这也是spring cloud zk的处理方式，具体内部的处理逻辑还是见 上面大佬 的分享，例子说明也很详细。



##### 其他知识
- PropertySourceLoader 属性加载器，springboot真正加载配置的两个类，在ConfigFileApplicationListener中使用
  - PropertiesPropertySourceLoader 支持从xml或properties格式的文件中加载数据，加载成Property
  - YamlPropertySourceLoader 支持从yml或者yaml格式的文件中加载数据，加载成Map

PropertySourcesPlaceholderConfigurer 与 PropertyPlaceholderConfigurer [区别](https://www.jianshu.com/p/a3c7ff0de5ac)；
springboot 默认是PropertySourcesPlaceholderConfigurer，感觉这两个类的主要作用就是替换xml中的占位符，不处理bean中@Value注解是没用的

PropertySourcesPropertyResolver 核心方法，从持有的属性源集合foreach拿属性，拿到就返回。
```
protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
    if (this.propertySources != null) {
        // 多 属性源 集合
        for (PropertySource<?> propertySource : this.propertySources) {
            if (logger.isTraceEnabled()) {
                logger.trace("Searching for key '" + key + "' in PropertySource '" +
                        propertySource.getName() + "'");
            }
            // 从 属性源 中 拿数据
            Object value = propertySource.getProperty(key);
            if (value != null) {
                // 如果拿到的是占位符，再嵌套拿
                if (resolveNestedPlaceholders && value instanceof String) {
                    value = resolveNestedPlaceholders((String) value);
                }
                // 日志记录
                logKeyFound(key, propertySource, value);
                // 如果有需要就转换，默认使用DefaultConversionService转换
                return convertValueIfNecessary(value, targetValueType);
                // 拿到就返回
            }
        }
    }
    if (logger.isDebugEnabled()) {
        logger.debug("Could not find key '" + key + "' in any property source");
    }
    return null;
}
```

##### 扩展
spring cloud zk 配置加载核心实现在ZookeeperPropertySourceLocator implements PropertySourceLocator;
本质就是组装一个多PropertySource的CompositePropertySource，再把PropertySource套在Environment中

调用链路：
SpringApplication.run => SpringApplication.prepareContext => SpringApplication.applyInitializers  
=> PropertySourceBootstrapConfiguration.initialize => PropertySourceLocator.locate

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
