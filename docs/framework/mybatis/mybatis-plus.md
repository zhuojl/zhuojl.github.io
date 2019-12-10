[[toc]]
### 背景
抛开统计服务不谈, 其他的业务逻辑, 较多是单表操作, mybatis有很多代码生成工具, idea插件,maven插件, 
个人定制的生成代码库等, 而这些直接生成的框架, 在需要改字段时,往往需要改很多地方, 繁琐易漏, 
而mybatis可以没有xml, 所以更方便修改

### 核心逻辑
这里不解释mybatis-spring的加载流程, mybatis-plus(下称MP)和spring的结合同mybatis-spring, 
只是MP重写了MybatisConfiguration, 并覆盖了添加mapper的方法, 所以在可以将方法手动注入到mappedStatements, 
具体xml生成的细节从com.baomidou.mybatisplus.core.MybatisMapperRegistry#addMapper 入手看.

核心是要加载MP的MybatisConfiguration。

### 接入
接入比较简单, 为了方便接入, 减少配置, 使用starter版本

<dependency>
<groupId>com.baomidou</groupId>
<artifactId>mybatis-plus-boot-starter</artifactId>
<version>3.1.0</version>
</dependency>

卧槽, 在我们的项目里mybatis-spring的启动方式是MapperScan...，同时引用的mybatis-spring-boot-starter只是障眼法.?????

Mapper的注册逻辑在MapperScannerRegistrar 而不是org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar

### 与mybatis的兼容
兼容原来的myabtis, 本来mybatis就支持继承方法, mybatis-plus只是做了sqlSource的生成. 另外BaseMapper中的方法也可以被子类覆盖.

### 扩展
https://github.com/baomidou/mybatis-plus-samples/tree/master/mybatis-plus-sample-customize-basemapper

### 注意事项
#### 分页
https://mp.baomidou.com/guide/page.html

#### 逻辑删除
https://mp.baomidou.com/guide/logic-delete.html

#### 版本选择
选择的3.1.0版本, 3.1.1版本加了996icu license 协议  = =

### 遇到的问题
jar包冲突, 需要引入高版本的mybatis-spring jar包

### 引用
- github地址: https://github.com/baomidou/mybatis-plus
- 官方地址: https://mp.baomidou.com/guide/
- 示例地址: https://github.com/baomidou/mybatis-plus-samples



