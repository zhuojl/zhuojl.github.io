
# 写在前面
关于 mybatis参数，mybatis文档中介绍的比较少，是否方法的所有参数都需要加@Param注解，如果不加又该怎么获取？

这里我们不妨想一下，如果我们需要一个属性#{user.name}，我们怎么去获取？
***
假装~~思考~~
***
其实是需要先从user的父级对象中获取user对象，再从user对象中获取name对象，那么有什么数据结构或者对象可以满足这种需求呢，其实就是Map和POJO能够满足这种需求。那其实mybatis要做的事情就是组装成POJO或者Map，其中map的key就是属性的名称。

# 给参数命名
- 如果是多个参数，则得到一个Map<String, Object>（key=参数的名称，value=参数）
- 如果是单个参数得到参数本身
- 如果是集合参数，处理集合

## 初始化参数的名称
这发生在初始化ParamNameResolver，得到一个Map<Integer, String>（key=参数索引，value=参数名称），名称获取逻辑如下：

org.apache.ibatis.reflection.ParamNameResolver#ParamNameResolver
```java
public ParamNameResolver(Configuration config, Method method) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
        // 1.如果为特殊参数（RowBounds（分页对象） 和 ResultHandler（结果处理）），则不会记入mapper的实际参数
        if (isSpecialParameter(paramTypes[paramIndex])) {
            // skip special parameters
            continue;
        }
        String name = null;
        for (Annotation annotation : paramAnnotations[paramIndex]) {
            // 如果有注解Param，则名称为param属性值
            if (annotation instanceof Param) {
                hasParamAnnotation = true;
                name = ((Param) annotation).value();
                break;
            }
        }
        if (name == null) {
            // @Param was not specified.
            /* 如果配置了useActualParamName=true的话，则取实际参数的名称，而实际参数的名称取决于 jvm运行参数 -parameter，
            如果启动没设置该值，则参数名为arg+索引，(关于useActualParamName参数的解释：允许使用方法签名中的名称作为语句参数名称。
            */
            if (config.isUseActualParamName()) {
                name = getActualParamName(method, paramIndex);
            }
            if (name == null) {
                // use the parameter index as the name ("0", "1", ...)
                // gcode issue #71
                // 否则，该参数的名称为0,1....n
                name = String.valueOf(map.size());
            }
        }
        map.put(paramIndex, name);
    }
    names = Collections.unmodifiableSortedMap(map);
}
```
## getNamedParams获取参数对象
上面说，names为一个Map<String, String> 的map，key=参数的索引，value=参数的名称，getNamedParams就是将参数与名称映射，得到一个map<String,Object>(key=参数名，value=参数）或者一个实际对象（当有效参数（不包括RowBounds和ResultHandler）只有一个时，并且没有使用Param注解）

org.apache.ibatis.reflection.ParamNameResolver#getNamedParams
```java
public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    // 没有参数
    if (args == null || paramCount == 0) {
        return null;
    // 只有一个参数，且没有Param注解时，返回对象本身
    } else if (!hasParamAnnotation && paramCount == 1) {
        return args[names.firstKey()];
    } else {
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        // 遍历参数名称
        for (Map.Entry<Integer, String> entry : names.entrySet()) {
            // 参数名称，与参数隐射
            param.put(entry.getValue(), args[entry.getKey()]);
            // add generic param names (param1, param2, ...)
            final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
            // ensure not to overwrite parameter named with @Param
            if (!names.containsValue(genericParamName)) {
                param.put(genericParamName, args[entry.getKey()]);
            }
            i++;
        }
        return param;
    }
}
```
插曲
``
在最开始看的时候，有些疑惑ParamNameResolver 和 getNamedParams 为什么分开。这是因为ParamNameResolver是方法一对一的不会因为依次查询而更改，是可以跟着方法一起被缓存的，而不需要每次执行都初始化一个ParamNameResolver
``

## 集合和数组特殊处理

在执行查询之前，还调用了org.apache.ibatis.session.defaults.DefaultSqlSession#wrapCollection方法，来处理集合和数组对象（在上面方法（getNamedParams）中，如果只有一个参数，且没有注解，会返回该对象，如果正好是集合，数组，那么此时会是集合和数组），参数设置如下：

  如果object instanceof Collection，name=collection，p
  如果object instanceof List，name=list
  如果object != null && object.getClass().isArray()，name=array

org.apache.ibatis.session.defaults.DefaultSqlSession#wrapCollection
```java
private Object wrapCollection(final Object object) {
    if (object instanceof Collection) {
        StrictMap<Object> map = new StrictMap<>();
        map.put("collection", object);
        if (object instanceof List) {
            map.put("list", object);
        }
        return map;
    } else if (object != null && object.getClass().isArray()) {
        StrictMap<Object> map = new StrictMap<>();
        map.put("array", object);
        return map;
    }
    return object;
}
```
# 解析参数
设置参数是发生在该方法（org.apache.ibatis.scripting.defaults.DefaultParameterHandler#setParameters），并在这时候从参数中解析取值。
这里需要说下ParameterMapping,它是在有参数匹配时，就会生成这么一个对象，表示匹配了一个参数

**获取属性值逻辑：**

org.apache.ibatis.scripting.defaults.DefaultParameterHandler#setParameters
```java
ParameterMapping parameterMapping = parameterMappings.get(i);
Object value;
String propertyName = parameterMapping.getProperty();
// 如果是hasAdditionalParameter，getAdditionalParameter，这个参数在`后面`会再提到
if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
    value = boundSql.getAdditionalParameter(propertyName);
// 如果是参数是null，value=null；
} else if (parameterObject == null) {
    value = null;
/* 如果参数满足：org.apache.ibatis.type.TypeHandlerRegistry#hasTypeHandler(java.lang.Class<?>)
（可简单理解为org.apache.ibatis.type.TypeHandlerRegistry#TYPE_HANDLER_MAP中包含的该类的handler，TYPE_HANDLER_MAP在配置中和对象初始化时增加值）*/
} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
    value = parameterObject;
} else {
    /* 从org.apache.ibatis.reflection.MetaObject#getValue中获取，内部从org.apache.ibatis.reflection.wrapper.ObjectWrapper#get获取，
    可以简单理解为：如果参数有“.”,则根据字类生成一个metaObject递归调用getValue，最终调用是：objectWrapper.get(); */
    MetaObject metaObject = configuration.newMetaObject(parameterObject);
    value = metaObject.getValue(propertyName);
}
```
**ObjectWrapper**
ObjectWrapper默认提供了3个实现，在MetaObject 构造器中初始化
```java
// 如果对象就是wrapper，取wrapper，
if (object instanceof ObjectWrapper) {
    this.objectWrapper = (ObjectWrapper) object;
// 如果objectWrapperFactory有这个类的wrapper，则从factory中获取，用户自定义wrapper
} else if (objectWrapperFactory.hasWrapperFor(object)) {
    this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
// 如果对象是Map的实例
} else if (object instanceof Map) {
    this.objectWrapper = new MapWrapper(this, (Map) object);
// 如果对象是集合
} else if (object instanceof Collection) {
    this.objectWrapper = new CollectionWrapper(this, (Collection) object);
} else {
    // 剩下的实体对象处理
    this.objectWrapper = new BeanWrapper(this, object);
}
```

|ObjectWrapper| 处理类型 | 底层结构 |
|:-:|:-:|:-:|
| MapWrapper | 对象是map的实例 | 持有map，map |
|CollectionWrapper|集合对象|不过内部实现基本全是抛错，所以也不能直接引用属性#{list.size}|
|BeanWrapper|数组对象，普通对象|持有一个MetaClass,内部是实体的所有get，set方法等，数组类是没有属性的get，set方法的，所以也不能直接引用#{array.length}|		
**疑问：**
1. 为什么CollectionWrapper 内部全是抛错，array也没有属性可以获取，那foreach是怎么工作的呢？`后面`

在sql解析时，org.apache.ibatis.scripting.xmltags.DynamicSqlSource#getBoundSql 会重新解析sql，解析成select id, name from users where id in ( #{__frch_id_0}, #{__frch_id_1}) 类似的sql，并将对应参数的键值对设置到org.apache.ibatis.scripting.xmltags.DynamicContext#bindings中，在做参数设置时，已经没有foreach标签，只有简单参数

# 结论：
1. 参数名称不建议使用索引值（param+有效参数索引或者arg+索引或者0,1...n-1），如果参数顺序更改了，容易出错
2. 当一个参数时，可以不用@Param注解，如果是基本类型，String，xml中的变量定义和mapper参数名称可以不一致，但是最好一致，如果是POJO，不加注解可以不通过POJO名称直接访问POJO中字段。
3. 多个属性时，都用@Param注解，不然名称无法获取（目前我们并没有加-paramters 参数），只能通过  1 中方式获取
4. 使用array和list时，注意不要访问size()或者length这样的属性/方法
		
# 参考
[mybatis 3.x源码深度解析与最佳实践](http://www.cnblogs.com/zhjh256/p/8512392.html )（倒是没参考，不过佩服这篇幅，以后应该可以借鉴）



