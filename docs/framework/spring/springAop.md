### spring aop 不完整记录

#### AOP相关概念
- 方面（Aspect）：一个关注点的模块化，这个关注点实现可能另外横切多个对象。事务管理是J2EE应用中一个很好的横切关注点例子。方面用Spring的 Advisor或拦截器实现。
- 连接点（Joinpoint）: 程序执行过程中明确的点，如方法的调用或特定的异常被抛出。
- 通知（Advice）: 在特定的连接点，AOP框架执行的动作。各种类型的通知包括“around”、“before”和“throws”通知。通知类型将在下面讨论。
  许多AOP框架包括Spring都是以拦截器做通知模型，维护一个“围绕”连接点的拦截器链。Spring中定义了四个advice: BeforeAdvice, AfterAdvice, ThrowAdvice和DynamicIntroductionAdvice
- 切入点（Pointcut）: 指定一个通知将被引发的一系列连接点的集合。AOP框架必须允许开发者指定切入点：例如，使用正则表达式。 Spring定义了Pointcut接口，
  用来组合MethodMatcher和ClassFilter，可以通过名字很清楚的理解， MethodMatcher是用来检查目标类的方法是否可以被应用此通知，
  而ClassFilter是用来检查Pointcut是否应该应用到目标类上
- 引入（Introduction）: 添加方法或字段到被通知的类。 Spring允许引入新的接口到任何被通知的对象。例如，你可以使用一个引入使任何对象实现 IsModified接口，
  来简化缓存。Spring中要使用Introduction, 可有通过DelegatingIntroductionInterceptor来实现通知，通过DefaultIntroductionAdvisor来配置Advice和代理类要实现的接口
- 目标对象（Target Object）: 包含连接点的对象。也被称作被通知或被代理对象。POJO
- AOP代理（AOP Proxy）: AOP框架创建的对象，包含通知。 在Spring中，AOP代理可以是JDK动态代理或者CGLIB代理。
- 织入（Weaving）: 组装方面来创建一个被通知对象。这可以在编译时完成（例如使用AspectJ编译器），也可以在运行时完成。Spring和其他纯Java AOP框架一样，在运行时完成织入。

#### bean代理流程
总大面上来说，spring 内部都是通过BeanPostProcessor来实现bean代理的，内部又细分了两套，分别是AbstractAdvisingBeanPostProcessor的子类  
和 AbstractAutoProxyCreator 的子类。

#### ProxyProcessorSupport extends ProxyConfig implements Ordered, BeanClassLoaderAware, AopInfrastructureBean
上面说的两个类都实现了ProxyProcessorSupport，ProxyProcessorSupport的定义如上，  
其中AopInfrastructureBean 只是用于标记是Spring's AOP 基础设施，标记这类bean不会进行 auto-proxying, 尽管条件都匹配。
ProxyConfig用于创建代理的配置的便利超类，以确保所有代理创建者具有一致的属性，

```
// true代表直接代理类，false代表代理接口。默认为false
private boolean proxyTargetClass = false;
// 是否执行某些优化，感觉基本没怎么用到
private boolean optimize = false;
// 代表子类是否能被转换为Advised接口，默认为false，表示可以
boolean opaque = false;
// 是否暴露代理，也就是是否把当前代理对象绑定到AopContext的ThreadLocal属性currentProxy上去，
// 常用于代理类里面的代理方法需要调用同类里面另外一个代理方法的场景。
boolean exposeProxy = false;
// 当前代理配置是否被冻结，如果被冻结，配置将不能被修改
private boolean frozen = false;

```
ProxyProcessorSupport 的核心方法是 evaluateProxyInterfaces()，筛选类符合条件的接口，否则设置setProxyTargetClass(true)
```
protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
    // 查询所有的接口
    Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
    boolean hasReasonableProxyInterface = false;
    for (Class<?> ifc : targetInterfaces) {
        if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) &&
                ifc.getMethods().length > 0) {
            hasReasonableProxyInterface = true;
            break;
        }
    }
    // 如果有满足条件的接口
    if (hasReasonableProxyInterface) {
        // Must allow for introductions; can't just set interfaces to the target's interfaces only.
        for (Class<?> ifc : targetInterfaces) {
            proxyFactory.addInterface(ifc);
        }
    }
    else {
        // 否则设置setProxyTargetClass
        proxyFactory.setProxyTargetClass(true);
    }
}

protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
    return (InitializingBean.class == ifc || DisposableBean.class == ifc ||
            Closeable.class == ifc || "java.lang.AutoCloseable".equals(ifc.getName()) ||
            ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
}

protected boolean isInternalLanguageInterface(Class<?> ifc) {
    return (ifc.getName().equals("groovy.lang.GroovyObject") ||
            ifc.getName().endsWith(".cglib.proxy.Factory") ||
            ifc.getName().endsWith(".bytebuddy.MockAccess"));
}


```

##### AbstractAdvisingBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor
Base class for {@link BeanPostProcessor} implementations that apply a Spring AOP {@link Advisor} to specific beans.
主要逻辑是通过BeanPostProcessor后置代理bean
```
public Object postProcessAfterInitialization(Object bean, String beanName) {
    // spring aop框架的bean 不需要代理处理
    if (bean instanceof AopInfrastructureBean) {
        return bean;
    }

    // 如果已经被增强，增加advisor
    if (bean instanceof Advised) {
        Advised advised = (Advised) bean;
        if (!advised.isFrozen() && isEligible(AopUtils.getTargetClass(bean))) {
            // Add our local Advisor to the existing proxy's Advisor chain...
            if (this.beforeExistingAdvisors) {
                advised.addAdvisor(0, this.advisor);
            }
            else {
                advised.addAdvisor(this.advisor);
            }
            return bean;
        }
    }
    // 如果符合要求，isEligible调用的是：AopUtils.canApply(this.advisor, targetClass);
    if (isEligible(bean, beanName)) {
        // 创建proxyFactory
        ProxyFactory proxyFactory = prepareProxyFactory(bean, beanName);
        if (!proxyFactory.isProxyTargetClass()) {
            // 筛选类符合条件的接口，否则设置setProxyTargetClass(true) 上面有提到
            evaluateProxyInterfaces(bean.getClass(), proxyFactory);
        }
        // AsyncAnnotationBeanPostProcessor、 MethodValidationPostProcessor 等实现都是通过修改this.advisor来实现的
        proxyFactory.addAdvisor(this.advisor);
        customizeProxyFactory(proxyFactory);
        return proxyFactory.getProxy(getProxyClassLoader());
    }

    // No proxy needed.
    return bean;
}

// isEligible 判断调用的方法
public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
    if (advisor instanceof IntroductionAdvisor) {
        return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
    }
    else if (advisor instanceof PointcutAdvisor) {
        PointcutAdvisor pca = (PointcutAdvisor) advisor;
        // AopUtils#canApply(org.springframework.aop.Pointcut, java.lang.Class<?>, boolean)
        return canApply(pca.getPointcut(), targetClass, hasIntroductions);
    }
    else {
        // It doesn't have a pointcut so we assume it applies.
        return true;
    }
}

```


##### AbstractAutoProxyCreator extends ProxyProcessorSupport implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware
![类图](https://github.com/zhuojl/blogDoc/blob/master/spring/AbstractAutoProxyCreator.png)

主要实现有：
- BeanNameAutoProxyCreator 匹配Bean的名称自动创建匹配到的Bean的代理

- AbstractAdvisorAutoProxyCreator
- InfrastructureAdvisorAutoProxyCreator 自动代理创建者，仅考虑基础架构Advisor Bean，而忽略任何应用程序定义的Advisor
- AnnotationAwareAspectJAutoProxyCreator 如果使用<aop:aspectj-autoproxy />标签来自动生成代理的话
- AspectJAwareAdvisorAutoProxyCreator
- DefaultAdvisorAutoProxyCreator


它处理代理发生在SmartInstantiationAwareBeanPostProcessor的三个方法中，底层都是调用的wrapIfNecessary()，
```
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
    // 自己代理过
    if (beanName != null && this.targetSourcedBeans.contains(beanName)) {
        return bean;
    }
    // 没代理
    if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
        return bean;
    }
    // 是否是Advice、Pointcut、Advisor、AopInfrastructureBean等
    if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
        this.advisedBeans.put(cacheKey, Boolean.FALSE);
        return bean;
    }

    // Create proxy if we have advice.
    Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
    if (specificInterceptors != DO_NOT_PROXY) {
        this.advisedBeans.put(cacheKey, Boolean.TRUE);
        // 大致同 AbstractAdvisingBeanPostProcessor 
        Object proxy = createProxy(
                bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
        this.proxyTypes.put(cacheKey, proxy.getClass());
        return proxy;
    }

    this.advisedBeans.put(cacheKey, Boolean.FALSE);
    return bean;
}

```
Advisor是由子类AbstractAdvisorAutoProxyCreator实现，入口方法getAdvicesAndAdvisorsForBean：

```
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
		//寻找Advisors，过滤，并做排序
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		// 寻找候选集
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 对候选集进行过滤
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		// 扩展, 交由子类实现
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			// 排序
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	protected List<Advisor> findCandidateAdvisors() {
		// 里面是从beanFactory获取所有Advisor接口类型的对象
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}
}

public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules.
		// 调用父类，首先获取实现了Advisor接口的对象
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		// 其次获取AspectJ注解修饰的
		advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		return advisors;
	}
}
```

代理创建调用的是：ProxyFactory#getProxy(java.lang.ClassLoader)
```
public Object getProxy(ClassLoader classLoader) {
    return createAopProxy().getProxy(classLoader);
}

protected final synchronized AopProxy createAopProxy() {
    if (!this.active) {
        activate();
    }
    // 默认是DefaultAopProxyFactory
    return getAopProxyFactory().createAopProxy(this);
}

// DefaultAopProxyFactory#createAopProxy
public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
    if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
        Class<?> targetClass = config.getTargetClass();
        if (targetClass == null) {
            throw new AopConfigException("TargetSource cannot determine target class: " +
                    "Either an interface or a target is required for proxy creation.");
        }
        if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
            return new JdkDynamicAopProxy(config);
        }
        return new ObjenesisCglibAopProxy(config);
    }
    else {
        return new JdkDynamicAopProxy(config);
    }
}

```

##### 差别与如何选择
|  类别   |                       AbstractAdvisingBeanPostProcessor                       |                           AbstractAutoProxyCreator                           |
|:------:|:------------------------------------------------------------------------------:|:---------------------------------------------------------------------------:|
| 数据结构 |                      持有一个Advisor，只处理当前实现的Advisor                      |          通过getAdvicesAndAdvisorsForBean的Advisor 获取待处理的Advisor          |
| 注入容器 |                            将当前实现作为一个bean注入                             | 将需要的Advisor注入到容器就行，在 AnnotationAwareAspectJAutoProxyCreator可以被发现 |
| 常见实现 | 不推荐，常见实现：MethodValidationPostProcessor，AsyncAnnotationBeanPostProcessor |                        通常用这种方式，通过@Aspect来实现                         |


#### 代理执行流程
在没有循环依赖的情况下，其实谁先谁后，都无所谓，因为都判断了是否已经被代理，如果被代理只是增加Advisor。
在存在循环依赖的情况下，这时候不能有AbstractAdvisingBeanPostProcessor，否则异常：
```
Caused by: org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'a': 
Bean with name 'a' has been injected into other beans [b] in its raw version as part of a circular reference,
 but has eventually been wrapped. This means that said other beans do not use the final version of the bean. 
 This is often the result of over-eager type matching - consider using 'getBeanNamesOfType' with the 'allowEagerInit' 
 flag turned off, for example.

```

果然阅读源码最好的方式就是断点着读，多读几次，直接看的话，容易混乱关系。

#### 引用
- [Spring AOP 之 理论篇](https://segmentfault.com/a/1190000007469968)
- [spring aop](https://www.cnblogs.com/ityouknow/p/5329550.html)
- [SpringAop之ProxyConfig](https://www.jianshu.com/p/1f8dbeadd79d)
- [AbstractAutoProxyCreator ](https://my.oschina.net/chengxiaoyuan/blog/1525761)
