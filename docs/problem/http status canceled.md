### http 状态码 canceled

最近做一个老服务拆分工作，方案是全量copy服务，后续在新服务迭代做功能迁移。遇到好几个问题，虽然结果都是op瞎搞的，
但是问题还是有意思，记录一下。

#### 背景
tomcat 服务；shiro + cas；ngnix 反向代理

自己对shiro不熟悉

#### 现状
请求过程，其实也是cas认证过程：
``` 
1、https://a.com/a.jsp
↓
2、https://cas.com/login?service=https://a.com/shiro-cas
↓
3、https://a.com/shiro-cas?ticket=ST-1210136-go2jmLtJCRfhHYXXXXXXX（浏览器显示状态为canceled，阻止了页面重定向）
↓
4、http://a.com/a.jsp (到这一步时，https成http，在console中有错误提示信息blocked)
```


#### 排查
请求canceled的原因：

- The DOM element that caused the request to be made got deleted (i.e. an IMG is being loaded, but before the load happened, you deleted the IMG node)
- You did something that made loading the data unnecessary. (i.e. you started loading a iframe, then changed the src or overwrite the contents)
- There are lots of requests going to the same server, and a network problem on earlier requests showed that subsequent requests weren't going to work (DNS lookup error, earlier (same) request resulted e.g. HTTP 400 error code, etc)

感觉都不是！

在上面的认证流程中，其实已经走完cas验证，重新进入页面后可以正常访问，是cas问题吗？cas处理完验证但是遇到某种错误，没有返回正常的状态（30x）
从网上得到的消息，都没有找到信息。

是shiro的问题吗，不熟悉，想本地debug，但是这个服务不能本地登录，权限组限制了本地认证！ 

网上找资料``cas认证第三步canceled``，各种骚姿势都有。找了一圈答案是：``TOMCAT默认情况下的redirect会是80``

#### 解决
配置ngnix: proxy_redirect       http:// https://;

修改tomcat配置，增加配置proxyName，proxyPort，scheme，secure：
``` html
<Connector port="8080"
  proxyName="这里是域名"
  proxyPort="443"
  scheme="https"
  secure="true"
  protocol="org.apache.coyote.http11.Http11NioProtocol"
  connectionTimeout="20000" URIEncoding="UTF-8" useBodyEncodingForURI="true"
  redirectPort="8443" />
```

#### 参考
- https://www.oschina.net/question/102370_2236727?p=1

- https://yq.aliyun.com/articles/47111

