## review 好文整理
- [代码审查之最佳实践](https://mp.weixin.qq.com/s/p_CNasQxzdni4G2eD0xUrQ?from=singlemessage)
- [CODE REVIEW中的几个提示](https://coolshell.cn/articles/1302.html)
- [从CODE REVIEW 谈如何做技术](https://coolshell.cn/articles/11432.html)
- [同事1的review总结](http://671b134e.wiz03.com/share/s/1D6Nde1iPA2G2Ubs6f1kzm8B3pQwiK0YmQtG2d06T83a1fhS)
- [同事2的review总结](http://note.youdao.com/noteshare?id=068837d5dca69ddfcf41c35884de896b&sub=3FF9CB9D60424070B0B1977FE34B8EEC)

## 个人review总结：

- if else 简化

修改前：
```java
class Demonstration {
    
    // 待优化
    public void function() {
        if(boolean1) {
            // doSth1
        } else {
            if(boolean2) {
                // doSth2
            } else {
                // doSth3
            }
        }
    }
    
    // 优化后
    public void function(){
        if(boolean1) {
            // doSth1
            return;
        }
        if(boolean2) {
            // doSth2
            return;
        }
        // doSth3
    }
}
```

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

- 可能为空的返回值，尽量返回Optional

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
 




