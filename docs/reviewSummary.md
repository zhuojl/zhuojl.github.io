

个人review总结：

- if else 简化

修改前：
```java
if (a == null) {
   // doSomething
   if (xxx) {a=xx}
   return a;
}
```

修改后：
```java
// 层数减少一层。更简洁
if (a != null) { 
    return a;
}
if (xxx) { return xx;}
```

- 对外接口返回值，不要直接返回数组,不宜扩展

- 方法只做一件事，短小，让代码主干更清晰！

- 一些builder方法写在实体内，避免service等方法太臃肿

- 使用继承，提炼公共方法，避免重复方法，

- redis锁高于事务锁问题

```java

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
```

- 定义枚举值，如果不入库，用字符表示更好


