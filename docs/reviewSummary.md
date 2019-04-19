## review 好文整理
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

- 

 