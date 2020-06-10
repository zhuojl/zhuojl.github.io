### 建造者模式

说明参见[菜鸟教程](https://www.runoob.com/design-pattern/builder-pattern.html)

常见的示例中，有两种方式，一种是builder类持有目标类的大部分字段/组成元素，一种是builder持有目标对象，然后一步步完成build。

源码中用到的建造者：
feign.Feign的内部类feign.Feign.Builder，builder内持有需要的各种属性，通过build构建两个封装对象，再构建最终的ReflectiveFeign。
引用时，通过feign.Feign静态方法，获取Builder对象，再调用build()构建目标对象。



[建造者模式--Builder](https://juejin.im/post/5a23bdd36fb9a045272568a6) Builder