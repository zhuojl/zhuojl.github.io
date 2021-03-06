# review 总结

## review 好文整理
- [代码审查之最佳实践](https://mp.weixin.qq.com/s/p_CNasQxzdni4G2eD0xUrQ?from=singlemessage)
- [CODE REVIEW中的几个提示](https://coolshell.cn/articles/1302.html)
- [从CODE REVIEW 谈如何做技术](https://coolshell.cn/articles/11432.html)
- [同事1的review总结](http://671b134e.wiz03.com/share/s/1D6Nde1iPA2G2Ubs6f1kzm8B3pQwiK0YmQtG2d06T83a1fhS)
- [同事2的review总结](http://note.youdao.com/noteshare?id=068837d5dca69ddfcf41c35884de896b&sub=3FF9CB9D60424070B0B1977FE34B8EEC)

## 个人review总结：

- 使用 卫语句 优化 if else，[如](https://www.cnblogs.com/Braveliu/p/7364369.html)

- 对外接口返回值，不要直接返回数组,不宜扩展

- 方法只做一件事，短小，让代码主干更清晰！

- 一些builder方法写在实体内，避免service等方法太臃肿

- 使用继承，提炼公共方法，避免重复方法，

- redis锁高于事务锁问题

```java
class Demonstration {

    @Override
    @Transactional(rollbackFor = Exception.class) 
    public void test() {
        // 业务基本验证
        redisLock; // redis锁
        try {
            if (notExist) { // 如果不存在
                insert(); // 则插入
            }
        } finally {
            releaseRedisLock; // 释放redis锁
        }
    }
}
```

- 定义枚举值，如果不入库，用字符表示更好

- java.lang.AbstractStringBuilder.setLength(0) 了解下..

- 可能为空的返回值，尽量返回Optional，内部访问的其他情况默认不为null，不验证null

- 对外接口的返回值，能返回list时，不要返回map，避免有多个key时增加接口

- Thread.sleep修改为TimeUnit.sleep 更能表达清楚休眠的时间

- 增加状态码返回，而非直接抛出异常，让父级捕获，容易出现太长的异常链

- jdk自带Objects类有不少好用的静态方法，requireNonNull,isNull,equals,hashCode生成函数等

- 所有非业务逻辑的实现，都可以考虑抽象成工具方法或者某种结构的处理。比如list的合并。

- 对于不需要对await有时间需求的CountDownLatch，用ExecutorService.submit()更好

- 复杂对象传递，不要用字符串拼接，也许最终入库是json或者其他格式的特殊字符串，但是程序内部要有对象，
这样在理解和验证的时候才方便做验证和逻辑处理，看到别人业务代码中的正则验证留下了眼泪

- 利用位操作，求交并集了解下

- TODO ：rpc接口参数如何避免重复定义
 
- 由于类中调用内部的方法，不走代理，在需要代理的地方可以使用SpringBeanUtils.getBean(getClass())

- 【面向对象】一些简单的对象属性的赋值，使用工具类计算后的值等，不要放于service中，放在关联的对象中

- 大的定时任务需要单独于面向用户的任务，避免带来稳定性等的影响 

- guava工具类的使用，range，cache, RateLimiter, retryer, Lists

- 外部接口的入参及方法一定要加注释

- 数据变更过程记录，允许冗余数据

- 外部接口在接口定义处，附加接口定义地址

- 对外部的接口查询条件应该尽量多的保证，避免重复多次的对外提供接口，除非特殊情况（待发现）group by 接口单独实现

- 关于多重实现，代理/装饰还是facade？使用代理

- 查询一定时间范围内的数据，常常需要特别注意具体是查哪个时间点！！！！

- 代码紧凑度，相关代码放一起，代码精良短小，考虑代码位置

- 如果一个类的属性可以由某几个对象解出来，可以考虑使用 建造者模式 将复杂度隔离到builder 类中，或者说用builder类代替build方法。

- foreach 中处理了数据统计，也处理了类型转换，将类型转换与数据统计分开处理

- 使用foreach进行多值求和时，使用stream().reduce()替换，更能表明用途

- 调用外部接口，把外部文档连接写在方法体上

- 当一个事件可能被几个地方发出来时，需要增加类型表明事件来源