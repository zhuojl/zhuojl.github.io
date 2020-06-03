### BeanPostProcessor 不完整记录
看一遍忘一遍，果然人类的本质就是``复读机``

BeanPostProcessor 有以下两个方法，在bean 实例化之后被调用，更准确地说是在AbstractAutowireCapableBeanFactory#populateBean之后，  
分别在初始化（AbstractAutowireCapableBeanFactory#invokeInitMethods）前后调用。
```
public interface BeanPostProcessor {
    // 在bean Initialization（初始化）之前执行，这里注意区别一下 instantiation(实例化)，后面说
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    // 在bean Initialization（初始化）之后执行，这里注意区别一下 instantiation(实例化)，后面说
    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

#### BeanPostProcessor 的实现
在 BeanPostProcessor 的基础上，有多个接口做了扩展，也是bean的生命周期中必经的

##### InstantiationAwareBeanPostProcessor

通常用于 取缔 特定 Bean的 默认实例化，例如创建具有特殊TargetSource的代理（池化的目标，延迟初始化目标等），或实现其他注入策略，例如字段注入。

这是特殊用途的接口，主要用于spring框架内部，推荐尽可能直接实现BeanPostProcessor，或者继承InstantiationAwareBeanPostProcessorAdapter 屏蔽掉实现

```
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    // 这个方法用来在对象实例化前直接返回一个对象（如代理对象）来代替通过内置的实例化流程创建对象；
    // 在 AbstractAutowireCapableBeanFactory#resolveBeforeInstantiation（createBean()方法中，doCreateBean之前） 中被调用，
    // 如果有结果，调用 BeanPostProcessor.postProcessAfterInitialization，而非该接口下面一个方法
    @Nullable
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }
    
    //在对象实例化完毕执行AbstractAutowireCapableBeanFactory#populateBean中首先被调用 如果返回false则spring不再对对应的bean实例进行自动依赖注入。
    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }
   
    // 这里是在spring处理完默认的成员属性，应用到指定的bean之前进行回调，可以用来检查和修改属性，最终返回的PropertyValues会应用到bean中
    // @Autowired、@Resource等就是根据这个回调来实现最终注入依赖的属性的。
    // 在 AbstractAutowireCapableBeanFactory#populateBean 中被调用，在 postProcessAfterInstantiation 之后，applyPropertyValues 之前
    @Nullable
    default PropertyValues postProcessPropertyValues(y
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        return pvs;
    }
}
```

##### SmartInstantiationAwareBeanPostProcessor

比较出名的是： getEarlyBeanReference 用于处理循环依赖。

<p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework. In general, application-provided
 * post-processors should simply implement the plain {@link BeanPostProcessor}
 * interface or derive from the {@link InstantiationAwareBeanPostProcessorAdapter}
 * class. New methods might be added to this interface even in point releases.

```
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {
    //用来返回目标对象的类型（比如代理对象通过raw class获取proxy type 用于类型匹配）
    @Nullable
    default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }
    //这里提供一个拓展点用来解析获取用来实例化的构造器（比如未通过bean定义构造器以及参数的情况下，会根据这个回调来确定构造器）
    @Nullable
    default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
            throws BeansException {
        return null;
    }
    //获取要提前暴露的bean的引用，用来支持单例对象的循环引用（一般是bean自身，如果是代理对象则需要取用代理引用）
    default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

**循环依赖**
```
protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
    Object exposedObject = bean;
    if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
                if (exposedObject == null) {
                    return null;
                }
            }
        }
    }
    return exposedObject;
}
```

spring源码中SmartInstantiationAwareBeanPostProcessor有实现的类是AbstractAutoProxyCreator（主要处理代理的逻辑，返回被代理的对象），  
网上其实有大量的文章说循环依赖，如[spring是如何解决循环依赖的](https://juejin.im/post/5c98a7b4f265da60ee12e9b2)。
这里稍微说下为什么要 singletonFactories 这层缓存，而不是提前调用getEarlyBeanReference，将对象放在二级缓存earlySingletonObjects中？

我想 不能少的原因可能是：
1. 不打乱bean初始化的流程（实例话 -> 属性填充 -> 初始化(包括代理处理)），
2. 如果要提前调用，对所有对象都会触发提前代理处理，就算没有循环依赖，而循环依赖本来少


##### MergedBeanDefinitionPostProcessor

<p>The {@link #postProcessMergedBeanDefinition} method may for example introspect
the bean definition in order to prepare some cached metadata before post-processing
actual instances of a bean. It is also allowed to modify the bean definition but
<i>only</i> for definition properties which are actually intended for concurrent
modification. Essentially, this only applies to operations defined on the
{@link RootBeanDefinition} itself but not to the properties of its base classes.

```
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {
    // 在bean实例化完毕后调用 可以用来修改 merged BeanDefinition的一些properties 或者用来给后续回调中缓存一些meta信息使用
    // 这个算是将merged BeanDefinition暴露出来的一个回调
    // 在 AbstractAutowireCapableBeanFactory#doCreateBean 中被调用，bean创建之后，populateBean() 之前。
    void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);
}
```

#####  DestructionAwareBeanPostProcessor

对象销毁处理

```
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {
    // 这里实现销毁对象的逻辑,例如DisposableBean.destroy(),this
	// callback will only apply to beans which the container fully manages the
	// lifecycle for. This is usually the case for singletons and scoped beans.
    // DisposableBeanAdapter#destroy 中被调用
    void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;
    
    // 判断是否需要处理这个对象的销毁
    default boolean requiresDestruction(Object bean) {
        return true;
    }
}
```

##### 常见的实现

###### AbstractAutoProxyCreator
aop处理的核心。
aop处理，[详细见](./springAop.md)

##### AbstractBeanFactoryAwareAdvisingPostProcessor extends AbstractAdvisingBeanPostProcessor；
AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor

aop处理，[详细见](./springAop.md)

###### CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor
主要处理@Resource、@PostConstruct和@PreDestroy注解的实现。

###### AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor
主要处理@Autowired、@Value、@Lookup和@Inject注解的实现，处理逻辑跟CommonAnnotationBeanPostProcessor类似。

###### BeanValidationPostProcessor implements BeanPostProcessor
默认不添加，需要手动添加。主要提供对JSR-303验证的支持，内部有个boolean类型的属性afterInitialization，默认是false。  
如果是false，在postProcessBeforeInitialization过程中对bean进行验证，否则在postProcessAfterInitialization过程对bean进行验证。

###### MethodValidationPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor
默认不添加，需要手动添加。支持方法级别的JSR-303规范。需要在类上加上@Validated注解，以及在方法的参数中加上验证注解，  
比如@Max，@Min，@NotEmpty …。 下面这个BeanForMethodValidation就加上了@Validated注解，并且在方法validate的参数里加上的JSR-303的验证注解。

###### AsyncAnnotationBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor

###### ScheduledAnnotationBeanPostProcessor implements MergedBeanDefinitionPostProcessor, DestructionAwareBeanPostProcessor,
Ordered, EmbeddedValueResolverAware, BeanNameAware, BeanFactoryAware, ApplicationContextAware, SmartInitializingSingleton。。。。

###### ApplicationContextAwareProcessor 简单判断是否是 EnvironmentAware、ApplicationContextAware等的实现，是则设置属性

###### ImportAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
处理 importAware

#### 其他

SmartInitializingSingleton 在DefaultListableBeanFactory#preInstantiateSingletons 的最后，会再判断是否是 SmartInitializingSingleton 的实现，
触发afterSingletonsInstantiated，常见的有 ScheduledAnnotationBeanPostProcessor 处理执行task；KafkaListenerAnnotationBeanPostProcessor
处理注册消费者；CacheAspectSupport后置处理。

#### 引用
-[阿里大佬总结](https://fangjian0423.github.io/2017/06/24/spring-embedded-bean-post-processor/)
-[BeanPostProcessor的五大接口](https://www.cnblogs.com/zhangjianbin/p/10059191.html)
