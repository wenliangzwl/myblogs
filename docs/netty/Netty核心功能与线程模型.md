### 1. Netty初探

   NIO 的类库和 API 繁杂， 使用麻烦： 需要熟练掌握Selector、 ServerSocketChannel、 SocketChannel、 ByteBuffer等。

   开发工作量和难度都非常大： 例如客户端面临断线重连、 网络闪断、心跳处理、半包读写、 网络拥塞和异常流的处理等等。

   Netty 对 JDK 自带的 NIO 的 API 进行了良好的封装，解决了上述问题。且Netty拥有高性能、 吞吐量更高，延迟更低，减少资源消耗，最小化不必要的内存复制等优点。

   Netty 现在都在用的是4.x，5.x版本已经废弃，Netty 4.x 需要JDK 6以上版本支持
   
### 2. Netty的使用场景

   1. 互联网行业：在分布式系统中，各个节点之间需要远程服务调用，高性能的 RPC 框架必不可少，Netty 作为异步高性能的通信框架，往往作为基础通信组件被这些 RPC 框架使用。典型的应用有：阿里分布式服务框架 Dubbo 的 RPC 框架使用 Dubbo 协议进行节点间通信，Dubbo 协议默认使用 Netty 作为基础通信组件，用于实现。各进程节点之间的内部通信。Rocketmq底层也是用的Netty作为基础通信组件。

   2. 游戏行业：无论是手游服务端还是大型的网络游戏，Java 语言得到了越来越广泛的应用。Netty 作为高性能的基础通信组件，它本身提供了 TCP/UDP 和 HTTP 协议栈。

   3. 大数据领域：经典的 Hadoop 的高性能通信和序列化组件 Avro 的 RPC 框架，默认采用 Netty 进行跨界点通信，它的 Netty Service 基于 Netty 框架二次封装实现。

   netty相关开源项目：https://netty.io/wiki/related-projects.html

### 3. Netty通讯示例

#### 3.1 引入依赖 

```
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.35.Final</version>
</dependency>
```

#### 3.2 服务端代码

```java
public class MyNettyServer {

    public static void main(String[] args) {

        // 创建两个线程组 bossGroup 和 workerGroup，含有的子线程 NioEventLoopGroup 的个数默认为cpu 核数的两倍
        // bossGroup 只是处理连接请求， workerGroup 负责 和客户端业务处理
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建 服务器端 的启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup,workerGroup)// 设置两个线程组
                    .channel(NioServerSocketChannel.class) // 使用 NioServerSocketChannel 作为服务器的通道实现
                    // 初始化服务器连接队列大小，服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接.
                    // 多个客户端同时过来的时候，服务端将不能处理的客户端连接请求放到队列中等待处理
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() { //  创建通道初始化对象，设置初始参数

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 对 workerGroup 的 SocketChannel 设置 处理器
                            socketChannel.pipeline().addLast(new MyNettyServerHandler());
                        }
                    });

            log.info("netty server start ....");

            // 绑定一个端口 并且同步，生成了一个 ChannelFuture 异步对象，通过 isDone() 等方法可以判断异步事件的执行情况
            // 启动服务器(并绑定端口)， bind 是异步操作，sync 方法是 等待异步操作执行完毕
            ChannelFuture cf = bootstrap.bind(9000).sync();

            // 给 cf 注册监听器，监听关心的事件
          /*  cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (cf.isSuccess()) {
                        log.info("监听端口9000成功");
                    }else {
                        log.info("监听端口9000失败");
                    }
                }
            });*/

            // 对通道关闭进行监听，closeFuture 是异步操作，监听通道关闭
            // 通过 sync 方法 同步等待 通道关闭 处理完毕，这里会阻塞等待 通道关闭完成
            cf.channel().closeFuture().sync();


        } catch (Exception e) {
            log.error("error: {}",e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }
}

public class MyNettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     *  读取客户端发送的数据
     * @param ctx  上下文对象, 含有通道channel，管道pipeline
     * @param msg 就是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("服务器读取线程: {}", Thread.currentThread().getName());
        ByteBuf buf = (ByteBuf) msg;
        log.info("客户端发送的消息: {}", buf.toString(CharsetUtil.UTF_8));
    }

    /**
     *  数据读取完毕处理方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("helloClient...", CharsetUtil.UTF_8);
        ctx.writeAndFlush(buf);
    }

    /**
     * 处理异常, 一般是需要关闭通道
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

#### 3.3 客户端代码 

```java
public class MyNettyClient {

    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            // 创建客户端启动对象
            // 注意客户端使用的是 Bootstrap ，而不是 ServerBootstrap
            Bootstrap bootstrap = new Bootstrap();

            // 设置相关参数
            bootstrap.group(group) // 设置线程组
                    .channel(NioSocketChannel.class) // 使用 NioSocketChannel 作为客户端的通道实现
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 加入 处理器
                            socketChannel.pipeline().addLast(new MyNettyClientHandler());
                        }
                    });

            log.info(" netty client start ....");

            // 启动客户端去连接服务器端
            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();

            // 对关闭 通道进行 监听
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            log.info("error : {}", e);
        }finally {
            group.shutdownGracefully();
        }
    }
}


public class MyNettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     *  当客户端连接服务器完成就会触发该方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("helloserver ....".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(buf);
    }

    /**
     * 当通道有读取事件时会触发，即服务端发送数据给客户端
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        log.info("收到服务端的消息: {}", buf.toString(CharsetUtil.UTF_8));
        log.info("服务端的地址是: {}", ctx.channel().remoteAddress());
    }

    /**
     *  处理异常, 一般是需要关闭通道
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("error: {}", cause);
        ctx.close();
    }
}
```