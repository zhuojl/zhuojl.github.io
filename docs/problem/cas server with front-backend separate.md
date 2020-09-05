### 前后端分离项目中自动cas流程处理

#### 现状
在当前的项目中，常常出现点击某个菜单会先跳到其他页面的情况，用户体验极差，经过初步排查，应该是cas的问题。

网上cas的帖子比较多，给个[高赞](https://www.jianshu.com/p/75edcc05acfd)的.

按照正常cas流程，登录是悄然完成，至少是对用户无感知的。但是在现有的系统是个特殊的流程，每次都是跳默认页面，体验太差。。。。
而且这个问题从公司成立到现在~~6年了居然没人解决~~(应该是公司这两三年开始搞前后端分离就有问题了)，诧异而又充满兴趣。。。。。

下面画了个图描述了一下正常cas流程和我们系统的流程，正常cas流程是抄的**高赞**，看这篇之前需要至少先看懂**高赞**；
后一部分是我们现在后端微服务的请求流程

app1: 前后端在一起的项目；  
app2: 前后端分离的后端项目

```puml
actor user
participant Browser
participant casServer
participant app1
participant app2

autonumber
group 用户首次访问app1流程
user -> Browser: goto app1
activate Browser

Browser -> app1: GET https://app1.example.com/
activate app1
app1 --> Browser: 302 Location:https://cas.example.com/cas/login? \n service=https%3a%2f%2fapp1.example.com%2f
deactivate app1

Browser -> casServer: GET https://cas.example.com/cas/login? \n service=https%3a%2f%2fapp1.example.com%2f
activate casServer
casServer --> Browser: CAS login form
deactivate casServer

user -> Browser: submit account/password
Browser -> casServer: POST https://cas.example.com/cas/login? \n service=https%3a%2f%2fapp1.example.com%2f
activate casServer
casServer -> casServer: Authenticate user
casServer --> Browser: Set-Cookie:CASTGC=TGT-2345678 \n 302 LOcation https://app1.example.com?ticket=ST-12345678
deactivate casServer

Browser -> app1: GET https://app1.example.com?ticket=ST-12345678
activate app1
app1 -> casServer: GET https://cas.example.com/serviceValidate?\n service=https%3a%2f%2fapp1.example.com%2f&ticket=ST-12345678
casServer --> app1: 200[XML content]
app1 --> Browser: Set-Cookie: JSESSIONID=ABC1234567 \n 302 Location:https://app1.example.com
deactivate app1

Browser -> app1: Cookie:JSESSIONID=ABC1234567 GET https://app1.example.com
activate app1
app1 -> app1: validate session cookie
app1 --> Browser: [200] and content
deactivate app1
deactivate Browser
end

== 用户已经完成cas server的登录，开始第一次访问app2服务的受保护的资源 ==

user -> Browser: 访问前端服务的页面
activate Browser
Browser -> app2: /loginInfo
activate app2
note left
这个接口没有登录保护，
访问app2的其他受保护的资源前，
先调用这个接口获取用户信息和处理登录
end note
app2 --> Browser: 200 login=false,ssoUrl=https://cas.example.com/cas/login?service=https%3a%2f%2fapp2.example.com%2fcas%2flogin
deactivate app2
Browser -> Browser: window.location.href=#{ssoUrl} 
Browser -> casServer: Cookie:CASTGC=TGT-2345678 \n https://cas.example.com/cas/login?service=https%3a%2f%2fapp2.example.com%2fcas%2flogin
activate casServer
casServer --> Browser: 302 Location:https://app2.example.com%2fcas%2flogin?ticket=ST-app2-XXX
deactivate casServer
Browser -> app2: https://app2.example.com%2fcas%2flogin?ticket=ST-app2-XXX
activate app2
app2 -> casServer: GET https://cas.example.com/serviceValidate?\n service=https%3a%2f%2fapp2.example.com%2f&ticket=ST-app2-XXX
casServer --> app2: 200[XML content]
app2 --> Browser: Set-Cookie: JSESSIONID=app2-XXX-XXX \n 302 Location:https://app2.example.com/default.html
deactivate app2
note left #f5222d
在目前的框架中，登录成功后跳转的是固定的默认首页，
所以不管第一次访问的是哪一个前端的页面，
第一次都会跳转到首页
end note
Browser -> app2: https://app2.example.com/default.html Cookie: JSESSIONID=app2-XXX-XXX
activate app2
app2 -> app2: validate session cookie
app2 --> Browser: [200] and content
deactivate app2
deactivate Browser

```
#### 尝试解决
##### cas 自动登录，不用特殊接口处理
根据cas协议，登录认证过程都是自动完成，而我们系统中却特殊处理，所以尝试让cas流程自动完成登录并请求对应的业务接口。
这里我们用了一个例子（git地址待补充）测试。

在接口通过上面demo服务app2 的LoginFilter时，因为没有登录，会触发登录重定向，即上图的第 ``13`` 步，此时前端报错：
``` text
Access to XMLHttpRequest at 'http://app2.skrein.com:8222/getData' from origin 'http://localhost:8111' has been 
blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```
最开始以为是跨域问题，增加上跨域配置仍然无法解决。在网上找到这篇帖子才放弃，[ajax 重定向跨域问题](https://momogugu.github.io/2018/08/23/ajax-%E9%87%8D%E5%AE%9A%E5%90%91%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98.html)
所以cas没法解决的ajax请求的问题，只能通过上面 ``17-29`` 的方式解决。

##### 将当前前端页面当参数透传
搜了众多资料之后，发现这篇[CAS认证前后端分离单点登录调研](https://momogugu.github.io/2018/08/23/ajax-%E9%87%8D%E5%AE%9A%E5%90%91%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98.html)，

所以，可以通过给``18、19``步增加一个参数=``当前前端页面的地址``，经过一步步透传，在``26``的时候设置Location为透传过来的url，
就可以完成原始页面的访问。

本来是想添加在service中，即：ssoUrl=https://cas.example.com/cas/login?service=https%3a%2f%2fapp2.example.com%2f``%3foriginalUrl%3dhttp%3a%2f%2fwww.baidu.com``，
但是在第24步的时候报错：
``` text
Ticket 'ST-7088-Ppxz765dxO40jTGKqoU5' does not match supplied service.
The original service was 'https://app2.example.com/callback?originalUrl=www.baidu.com' 
and the supplied service was 'https://app2.example.com/callback'.
```
如此一来，就只能让casServer特殊处理某些参数才行，让维护cas server的同事看代码，等了一天，他都没时间看，所以干脆要了源码自己看，找到如下代码,
```java
class CustomSimpleWebApplicationServiceImpl extends AbstractWebApplicationService {
    // 重定向时需要回传的参数名称前缀，在重定向设置参数是获取queryUrl带到响应中
    private static final String CONST_PARAM_PREFIX = "cust_";
}
```
所以只需要在跳转casServer时，加一个以cust_开头的参数就可以让casServer透传。

**最终跑通的流程图如下**
```puml
actor user
participant Browser
participant casServer
participant app2
note right of Browser
有时也代表前端服务，
比如第2、3；12、14
end note
autonumber
== 用户已经完成cas server的登录，开始第一次访问app2服务的受保护的资源 ==

user -> Browser: 访问前端服务的页面
activate Browser
Browser -> app2: /loginInfo?<font color=red><b>cust_url=http://www.baidu.com(发起请求的当前页面的地址)
activate app2
note left
前端请求时带上当前页面url
end note

app2 --> Browser: 200 login=false,ssoUrl=https://cas.example.com/cas/login?service=https%3a%2f%2fapp2.example.com%2fcas%2flogin&<font color=red><b>cust_url=http%3a%2f%2fwww.baidu.com
deactivate app2
note left
app2 登录验证不通过，将cust_url拼接到url中，
传递给casServer，等casServer透传回来
end note

Browser -> Browser: window.location.href=#{ssoUrl} 
Browser -> casServer: Cookie:CASTGC=TGT-2345678 \n https://cas.example.com/cas/login?cust_url=http%3a%2f%2fwww.baidu.com&service=https%3a%2f%2fapp2.example.com%2fcas%2flogin
activate casServer
casServer --> Browser: 302 Location:https://app2.example.com%2fcas%2flogin?ticket=ST-app2-XXX&<font color=red><b>cust_url=http%3a%2f%2fwww.baidu.com
deactivate casServer
note left
casServer 透传所有cust_开头的参数，
包括这里的cust_url
end note

Browser -> app2: https://app2.example.com%2fcas%2flogin?ticket=ST-app2-XXX&cust_url=http%3a%2f%2fwww.baidu.com
activate app2
app2 -> casServer: GET https://cas.example.com/serviceValidate?\n service=https%3a%2f%2fapp2.example.com%2f&ticket=ST-app2-XXX
casServer --> app2: 200[XML content]
app2 --> Browser: Set-Cookie: JSESSIONID=app2-XXX-XXX \n 302 <font color=red><b>Location:http://www.baidu.com
deactivate app2
note left
app2在casServer验证通过之后，从request中拿cust_url，
并设置为 重定向的地址。
end note
Browser -> Browser: 访问原始的前端页面： http://www.baidu.com 
Browser -> app2: /loginInfo Cookie: JSESSIONID=app2-XXX-XXX
activate app2
app2 -> app2: validate session cookie
app2 --> Browser: [200] and content
deactivate app2
deactivate Browser

```


因为历史原因，目前维护的系统中有多套cas client，spring security, shiro, pac4j，还得找到每种的处理方式。

这里简单列一下使用spring security的商品服务的修改
``` java
class CustomCasAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public final void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        logger.info("<<<<<<<<<<<<<<<<<==未授权:{}==>>>>>>>>>>>>>>>>>>", request.getRequestURI());
        String urlEncodedService = this.createServiceUrl(request, response);
        String redirectUrl = this.createRedirectUrl(urlEncodedService);
        redirectUrl = passCustomerParam(redirectUrl, request);
    
        if (StringUtils.isEmpty(request.getHeader(X_REQUESTED_WITH))) {
            this.preCommence(request, response);
            response.sendRedirect(redirectUrl);
        } else {
            writeJson(response, redirectUrl);
        }
    }
    // 参数透传处理
    private String passCustomerParam(String url, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(url);
        try {
            Enumeration<String> it = request.getParameterNames();
            while (it.hasMoreElements()) {
                String key = it.nextElement();
                if (key.startsWith(CONST_PARAM_PREFIX)) {
                    String value = request.getParameter(key);
                    log.info("customer param:{},{}", key, value);
                    sb.append("&").append(key).append("=").append(CommonUtils.urlEncode(value));
                }
            }
        } catch (Exception e) {
            log.error("passCustomerParam error", e);
        }
        return sb.toString();
    }

}
```
``` text
SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
// 配置从请求中获取登录成功后访问的地址。
successHandler.setTargetUrlParameter("cust_url");
```


### 补充
最近观察了一下其他业务的登录，发现别人没有透传参数也可以顺利跳转，咨询对方发现对方把访问的页面存session里了。。。  
在下次登录的时候就可以拿到原始页面。。。 虽然能达到效果，但是感觉没有咋家的好 = =！



#### 引用
- [cas 介绍](https://www.jianshu.com/p/75edcc05acfd)
- [ajax 重定向跨域问题](https://momogugu.github.io/2018/08/23/ajax-%E9%87%8D%E5%AE%9A%E5%90%91%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98.html)
- [CAS认证前后端分离单点登录调研](https://momogugu.github.io/2018/08/23/ajax-%E9%87%8D%E5%AE%9A%E5%90%91%E8%B7%A8%E5%9F%9F%E9%97%AE%E9%A2%98.html)，
- [UserSpring Security 中文文档#cas 验证](https://www.docs4dev.com/docs/zh/spring-security/4.2.10.RELEASE/reference/cas.html)
- [#关联收藏# Jasig cas 单点登录系统Server&Java Client配置](https://blog.csdn.net/puma_dong/article/details/11564309)
- [#关联收藏# jasig CAS实现单点登录](https://my.oschina.net/indestiny/blog/200768)
