### druid close connection error
上周线上服务巡查的时候，发下如下日志报错
```
[catalina-exec-21] [DEBUG]  com.alibaba.druid.util.JdbcUtils - close connection error
com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: Communications link failure during rollback(). Transaction resolution unknown.
at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method) ~[?:1.8.0_252]
at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62) ~[?:1.8.0_252]
at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45) ~[?:1.8.0_252]
at java.lang.reflect.Constructor.newInstance(Constructor.java:423) ~[?:1.8.0_252]
at com.mysql.jdbc.Util.handleNewInstance(Util.java:409) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.Util.getInstance(Util.java:384) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:1013) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:987) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:973) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:918) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.ConnectionImpl.rollback(ConnectionImpl.java:5086) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.ConnectionImpl.realClose(ConnectionImpl.java:4675) ~[mysql-connector-java-5.1.30.jar:?]
at com.mysql.jdbc.ConnectionImpl.close(ConnectionImpl.java:1650) ~[mysql-connector-java-5.1.30.jar:?]
at com.alibaba.druid.util.JdbcUtils.close(JdbcUtils.java:85) [druid-1.0.27.jar:1.0.27]
at com.alibaba.druid.pool.DruidDataSource.discardConnection(DruidDataSource.java:1092) [druid-1.0.27.jar:1.0.27]
at com.alibaba.druid.pool.DruidDataSource.getConnectionDirect(DruidDataSource.java:1034) [druid-1.0.27.jar:1.0.27]
at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:994) [druid-1.0.27.jar:1.0.27]
at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:984) [druid-1.0.27.jar:1.0.27]
at com.alibaba.druid.pool.DruidDataSource.getConnection(DruidDataSource.java:103) [druid-1.0.27.jar:1.0.27]
```

从报错堆栈来看，应该是在获取链接时，跳过无效链接，关闭链接时报了网络异常。
通过观察服务一天的情况，报错数量跟请求量成反比，猜测是：请求越少，空闲越多，需要关闭的越多。

当前使用的druid，版本：1.0.27

网上搜druid异常信息，找到如下信息：
- 问题同https://github.com/alibaba/druid/issues/1056，但没有解决方法。
- https://github.com/alibaba/druid/issues/2651 设置keepAlive=true后也没有修复，而且需要升级版本。
- https://github.com/alibaba/druid/issues/1282 去除removeAbandoned 也不行。
- https://github.com/alibaba/druid/issues/2197 设置defaultAutoCommit=true。实测能解决问题。
- https://github.com/alibaba/druid/issues/1056 几乎同上
- https://github.com/alibaba/druid/issues/2663 一个问题描述


经过了 long long time ago，终于看到这个报错是个debug，其实不影响业务流程的。。。。

又想起long long time ago，dba改了wait_timeout（86400 -> 10），并给了druid连接池的配置时间建议：``maxWait=4000，timeBetweenEvictionRunsMillis=40000``
综合一下，原因就是：程序中的timeBetweenEvictionRunsMillis 和 minEvictableIdleTimeMillis 都没有相应的改低，如果请求量低，服务中的链接失效可能性就很大，  
当程序拿链接就会检测到链接失效进行关闭，就会报链接异常，本来这个异常日志是debug，而系统中的日志级别就是debug，所以有这样的日志，只要调高级别就行了。

当然，这里的minEvictableIdleTimeMillis应该设置成10*1000以内，timeBetweenEvictionRunsMillis 应该设置为minEvictableIdleTimeMillis的几分之一，  
参照druid常规配置，timeBetweenEvictionRunsMillis可以配置成5000 ？


相关：
- [wait_timeout](https://dev.mysql.com/doc/refman/5.7/en/server-system-variables.html#sysvar_wait_timeout)
- [autocommit](https://dev.mysql.com/doc/refman/5.7/en/server-system-variables.html#sysvar_autocommit)
上面的问题也可以通过设置defaultAutoCommit=true来避免。。。没深究。。。

