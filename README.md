## TinyTomcat

> 基于 NIO 模式实现的简化版 Tomcat

简化实现 Server、Service、Host、Engine 等组件，通过解析 Server.xml 和 Web.xml 文件初始化服务器。

实现的主要功能：
- Context
- Servlet
- Filter
- Session
- Cookie
- Request
- Response
- Get & Post
- Keep-alive
- WebappClassLoader
- ......

### Tomcat 组件

![Tomcat组件图](images/tomcat组件图.png)

Tomcat 最顶层的容器是 Server，代表整个服务器，一个 Server 至少包含一个 Service。Server 掌管整个 Tomcat 的生死大权。

Service 主要包含两个部分：Connector 和 Container

- Connector用于处理连接相关的事情，并提供 Socket 与 Request 和 Response 相关的转化；
- Container用于封装和管理 Servlet，以及具体处理 Request 请求。

> 一个 Service 只有一个 Engine ，但是可以包含多个 Connectors，因为一个服务可以有多个连接，如同时提供 HTTP 和 HTTPS 链接。

### Connector 架构

Connector 通过 ProtocolHandler 来处理请求，不同的 ProtocolHandler 代表不同的连接类型，比如：Http11Protocol 使用的是普通 Socket 来连接的，Http11NioProtocol 使用的是 NioSocket 来连接的。

其中 ProtocolHandler 由包含了三个部件：Endpoint、Processor（ConnectionHandler）、Adapter。

1. Endpoint用来处理底层 Socket 的网络连接，Processor 用于将 Endpoint 接收到的 Socket 封装成 Request，Adapter 用于将 Request 交给 Container 进行具体的处理。
2. Endpoint 由于是处理底层的 Socket 网络连接，因此 Endpoint 是用来实现 TCP/IP 协议的，而Processor 用来实现 HTTP 协议的，Adapter 将请求适配到 Servlet 容器进行具体的处理。


### Tomcat 启动流程

![tomcat启动流程图](images/tomcat启动流程图.png)

### 请求处理流程

![Tomcat 请求流程](images/tomcat请求流程示意图.png)

1. 请求发送到 Tomcat，首先经过 Service；
2. 然后会交给 Connector，Connector 用于接收请求并将请求封装为 Request 和 Response 来具体处理，Request 和 Response 封装完之后再交给 Container 进行处理；
3. Container 处理完请求之后再返回给 Connector；
4. 最后 Connector 通过 Socket 将处理的结果返回给客户端。

### Tomcat NIO 工作流程

![Tomcat NIO 工作流程](images/tomcat%20nio%20工作流程.png)

#### 原理 
从上图中,可以看出一个 Connector 包含一个 Http11NioProtocol 实例、一个 CoyoteAdapter 实例，Http11NioProtocol 内部包含一个 NioEndpoint 实例、一个 Http11ConnectionHandler 实例。NioEndpoint 主要是实现了 Socket 请求监听线程 Acceptor、Socket NIO Poller线程、以及请求处理线程池。
1. 对于 Acceptor 监听到的Socket请求，经过 NioEndpoint 内部的 NIO 线程模型处理后，会转变为 SocketProcessor 在 Executor 中运行;
2. SocketProcessor 在 run 过程中会交给 Http11ConnectionHandler 处理，Http11ConnectionHandler 会从ConcurrentHashMap<NioChannel,Http11NioProcessor> 缓存中获取相应的 Http11NioProcessor 来继续处理;
3. Http11NioProcessor 主要是负责解析 Socket 请求 Header，解析完成后，会将 Request、Response（这里的请求、响应在tomcat中看成是 coyote 的请求、响应，意思是还需要 CoyoteAdapter 处理）交给 CoyoteAdapter 继续处理;
4. CoyoteAdapter 主要将 Socket 解析的 Request、Response 转化为 HttpServletRequest、HttpServletResponse，而这里的请求响应就是最后交给 Container 去处理的。

#### 实现

> Endpoint 维护了两个 map：nioProcessorMap 和 socketProcessorMap。当有新的 socket 注册到 Poller 中时，构造 Http11NioProcessor 和 SocketProcessor 对象，并存到 map 中。这两个 map 与 Poller 和 connectionHandler 共享。

1. Acceptor 接收到客户端的连接则通过 endpoint 将 socketChannel 注册到 Poller 中。
2. Poller 的 register 方法根据 isNewSocket 字段判断是否为新的连接，是新连接则生成 SocketProcessor 和 Http11NioProcessor 并存到 nioProcessorMap 和 socketProcessorMap 中；否则则从 map 中取出 SocketProcessor 。然后将 SocketProcessor 封装到 PollerEvent 中并交给 Selector。
3. Selector 检测到事件时，将 SocketProcessor 取出，并执行其 run 方法。
4. SocketProcessor 的 run 方法通过 Http11ConnectionHandler 取出并调用对应的 Http11NioProcessor。（Http11ConnectionHandler 和 Poller 、EndPoint 共享 nioProcessorMap 和 socketProcessorMap）
5. Http11NioProcessor 通过调用 socketProcessor 的方法读取原始请求，并解析封装成 Request ，生成空的 Response，然后交给 Adapter 。
6. Adapter 的 doDispatch 方法通过 Request 找到对象的 Host 主机和对应的 Context，然后将 context 装入 request 和 response 中；然后调用 service 方法。
7. Adapter 的 service 方法通过 request 的 url 从 context 的 map 中 找到对应的 Filter 生成并执行FilterChain ；然后找到对应的 ServletWrapper，并交给 servlet 执行。
8. Adapter 执行完毕后，Http11NioProcessor 将响应写入客户端，并根据是否 keep-alive 选择是否将连接重新注册到 Poller 中。
