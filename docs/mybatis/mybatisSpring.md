# 写在前面
最近同事做了一个mybatis分享，涉及到很多东西，对其中mybatis-spring比较感兴趣，所以深入了解下，这对以后其他任何基于接口动态代理的功能模块能够更加快速的了解和学习。

# 入口
入口是在xml中配置的org.mybatis.spring.mapper.MapperScannerConfigurer，为什么是他，来看看他的定义

```java
class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {}
```
其他接口没啥实际实现，这里重点看下实现BeanDefinitionRegistryPostProcessor（它是BeanFactoryPostProcessor的一个实现类，执行是在org.springframework.context.support.AbstractApplicationContext#refresh 触发的，具体请看下面的[spring bean 生命周期](https://www.cnblogs.com/zrtqsk/p/3735273.html)）的postProcessBeanDefinitionRegistry()方法,它内部是调用ClassPathMapperScanner#scan，分两步：
1. 先调用父类ClassPathBeanDefinitionScanner的doScan方法，返回包下面符合条件的BeanDefinitionHolder，
1. 然后将在xml中配置的属性都添加到bean的属性中，需要特别注意的属性：
	1. definition.setBeanClass(MapperFactoryBean.class) 表示通过MapperFactoryBean获取mapper的动态代理，
	1. 通过definition.getPropertyValues().add sqlSessionFactory 和 sqlSessionTemplate属性，表示在实例化代理之前，会先实例化SqlSessionFactory 或者 sqlSessionTemplate

这一步就已经完成BeanDefinition注册，真正初始化bean是在org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization中完成的，这里不会细看，里面内容很多，需要看的小伙伴请看XXX；
# bean实例化
上面也说了 在实例化mapper代理之前，会先实例化依赖的属性（sqlSessionFactory 和 sqlSessionTemplate），在项目中我们常配置的是sqlSessionFactory，这里看下SqlSessionFactoryBean，主要的逻辑在org.mybatis.spring.SqlSessionFactoryBean#buildSqlSessionFactory 中，它会解析xml配置，实例化Configuration，并被SqlSessionFactory持有。

SqlSessionFactoryBean实例化之后，通过反射（org.mybatis.spring.support.SqlSessionDaoSupport#setSqlSessionFactory）把属性设置到mapperFactoryBean中，这一步其实是构建一个SqlSession，属性设置完成后，调用getObject返回实例，这样就完成了一个Mapper代理的加载。
```java
public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
}
```
这里的sqlSession是mybatis-spring自定义的SqlSessionTemplate，getMapper调用的是org.apache.ibatis.session.Configuration#getMapper,这走到了mybatis的部分，偏离了这篇的主题，有兴趣可以自行看看，就是绕绕绕，返回一个Mapper的jdk动态代理。

# 使用接口动态代理的实现

## 公司实现的feign
实现的思路其实是一样的，都是先注册bean（这里用的ImportBeanDefinitionRegistrar），通过factoryBean.getObject获取，在这里面再动态代理自己需要的逻辑
## spring aop的实现
spring aop逻辑实现不一样，他不会先去注册需要加强的bean，或者说他只会针对已有的bean进行加强，加强postProcessAfterInitialization逻辑在AspectJAwareAdvisorAutoProxyCreator中，它是BeanPostProccessor的实现类。[spring aop](http://www.importnew.com/24459.html)这篇写的很详细，可以看看。


# 延伸&引用
- [spring bean 生命周期](https://www.cnblogs.com/zrtqsk/p/3735273.html)
- [doGetBean](https://www.jianshu.com/p/4b8bcca6697c)
- [invokeBeanFactoryPostProcessors](https://www.jianshu.com/p/0e7f65afa156)
- [finishBeanFactoryInitialization](https://www.jianshu.com/p/9d8e4fb5b162)
- [spring aop](http://www.importnew.com/24459.html)
- [mybatis-spring 官网介绍](http://www.mybatis.org/spring/zh/index.html)
- [github地址](https://github.com/mybatis/spring)