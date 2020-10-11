### 搭建Tomcat 源码环境
   

### startup.sh(startup.bat) & catalina.sh (catalina.bat)
   当我们初学tomcat的时候, 肯定先要学习怎样启动tomcat. 在tomcat的bin目录下有两个启动tomcat的文件, 一个是startup.bat, 它用于windows环境下启动tomcat; 另一个是startup.sh, 
   它用于linux环境下tomcat的启动. 两个文件中的逻辑是一样的.
   
   1.startup.bat(startup.sh) 文件主要做了一件事就是启动 catalina.bat 或 catalina.sh
   
   2.catalina.bat(catalina.sh)  最终执行了org.apache.catalina.startup.Bootstrap 类中的main方法.


### Bootstrap 
    
   每个应用程序都有一个唯一的入口(即main函数), tomcat启动相关的类位于catalina.startup包路径下，入口是类Bootstrap中的main()函数。Bootstrap启动类主要完成了三方面的内容，分别如下：
    
       ①在静态代码块中设置 catalinaHome 和 catalinaBase 两个路径；
            (1)catalinaHome:tomcat的安装目录
            
            (2)catalinaBase:tomcat的工作目录
       
       ②common、server、shared三个类加载器的初始化；
       
       ③利用反射机制实例化org.apache.catalina.startup.Catalina类。

### Tomcat 的启动流程分析

   ![](tomcat.assets/tomcat启动流程图.png)
   
   
   从图中我们可知从Bootstrap类的main方法开始, tomcat会以链的方式逐级调用各个模块的init()方法进行初始化, 待各个模块都初始化后, 又会逐级调用各个模块的start()方法启动各个模块
   
   下面只分析Bootstrap 怎么创建 Catalina对象，并调用load() 方法：
```
 public static void main(String args[]) {

        if (daemon == null) {
            // Don't set daemon until init() has completed
            // 创建本类对象
            Bootstrap bootstrap = new Bootstrap();
            try {
                // 调用init() 初始化方法进行初始化
                bootstrap.init(); // catalinaaemon
            } catch (Throwable t) {
                handleThrowable(t);
                t.printStackTrace();
                return;
            }
            daemon = bootstrap;
        } else {
            // When running as a service the call to stop will be on a new
            // thread so make sure the correct class loader is used to prevent
            // a range of class not found exceptions.
            Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
        }

        try {
            
            // 判断启动参数，一般是默认启动方式 start
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }

            if (command.equals("startd")) {
                args[args.length - 1] = "start";
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stopd")) {
                args[args.length - 1] = "stop";
                daemon.stop();
            } else if (command.equals("start")) {
                daemon.setAwait(true);  // 设置阻塞标志
                daemon.load(args);      // 解析server.xml,初始化Catalina
                daemon.start();
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
            } else if (command.equals("stop")) {
                daemon.stopServer(args);
            } else if (command.equals("configtest")) {
                daemon.load(args);
                if (null == daemon.getServer()) {
                    System.exit(1);
                }
                System.exit(0);
            } else {
                log.warn("Bootstrap: command \"" + command + "\" does not exist.");
            }
        } catch (Throwable t) {
            // Unwrap the Exception for clearer error reporting
            if (t instanceof InvocationTargetException &&
                    t.getCause() != null) {
                t = t.getCause();
            }
            handleThrowable(t);
            t.printStackTrace();
            System.exit(1);
        }
    }
```
  调用 Bootstrap中 load 方法
```
    /**
     * Load daemon.
     * 调用Catalina实例的load方法
     */
    private void load(String[] arguments)
        throws Exception {

        // Call the load() method
        String methodName = "load";
        Object param[];
        Class<?> paramTypes[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        //最终通过反射调用私有成员变量 catalinaDaemon 的load() 方法。
        // catalinaDaemon 私有成员变量 在Bootstrap 的init() 初始化方法中通过反射完成初始化，实际就是 Catalina 对象
        Method method =
            catalinaDaemon.getClass().getMethod(methodName, paramTypes);
        if (log.isDebugEnabled())
            log.debug("Calling startup class " + method);
        method.invoke(catalinaDaemon, param);
    }
```
 
 ### Tomcat7 BIO处理请求过程
   
   ![](tomcat.assets/tomcat架构平视图.jpg)

#### 使用协议 

   tomcat 启动会将 server.xml 的 Connector 标签解析成 Connector 类 ，Connector 类中有一个 setProtocol(String protocol) 方法，
   根据 Connector标签中的 protocol 属性 判断是使用哪种协议，然后会根据协议，设置不同的协议处理器 
```
// package org.apache.catalina.connector.Connector

 public void setProtocol(String protocol) {

        if (AprLifecycleListener.isAprAvailable()) { // 默认false
            if ("HTTP/1.1".equals(protocol)) {
                setProtocolHandlerClassName
                ("org.apache.coyote.http11.Http11AprProtocol");
            } else if ("AJP/1.3".equals(protocol)) {
                setProtocolHandlerClassName
                ("org.apache.coyote.ajp.AjpAprProtocol");
            } else if (protocol != null) {
                setProtocolHandlerClassName(protocol);
            } else {
                setProtocolHandlerClassName
                ("org.apache.coyote.http11.Http11AprProtocol");
            }
        } else {
            if ("HTTP/1.1".equals(protocol)) {  // tomcat7 默认bio
                setProtocolHandlerClassName
                ("org.apache.coyote.http11.Http11Protocol");  // BIO
            } else if ("AJP/1.3".equals(protocol)) {  // 跟apache 交互的协议，一般都不会用到
                setProtocolHandlerClassName
                ("org.apache.coyote.ajp.AjpProtocol");
            } else if (protocol != null) {  // 根据 类名设置 协议处理器
                setProtocolHandlerClassName(protocol); // org.apache.coyote.http11NIOProxot
            }
        }

    }
```

####  协议处理器（BIO 为例）
   
   根据 Connector 类设置的协议处理器，最终会在对应的协议处理器的构造方法中new 一个 Endpoint，这里 使用的 JIoEndpoint。
    
```
// package org.apache.coyote.http11.Http11Protocol

    public Http11Protocol() {
        endpoint = new JIoEndpoint();  // 这个其实就是bio Endpoint
        cHandler = new Http11ConnectionHandler(this);
        ((JIoEndpoint) endpoint).setHandler(cHandler);
        setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }
```

#### Endpoint
   
   Endpoint 根据io 模型拿到数据，在根据不同协议去解析数据
   Endpoint 就是接收处理 socket 连接的，然后根据不同的协议 实现不同的 Endpoint。
   
   Endpoint 的抽象类 是 AbstractEndpoint ，具体有三个实现类
       
       1.AprEndpoint 
       2.JIoEndpoint  实际就是 bio
       3.NioEndpoint  实际就是nio
   
  以  JIoEndpoint 为例
```
// package org.apache.tomcat.util.net.JIoEndpoint

    protected class Acceptor extends AbstractEndpoint.Acceptor {

        @Override
        public void run() {

            int errorDelay = 0;

            // Loop until we receive a shutdown command
            while (running) {

                // Loop if endpoint is paused
                // 如果Endpoint仍然在运行，但是被暂停了，那么就无限循环，从而不能接受请求
                while (paused && running) {
                    state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }

                if (!running) {
                    break;
                }
                state = AcceptorState.RUNNING;

                try {
                    //if we have reached max connections, wait
                    //达到了最大连接数限制则等待
                    countUpOrAwaitConnection();

                    Socket socket = null;  // bio，nio
                    try {
                        // Accept the next incoming connection from the server
                        // bio socket 接收数据
                        // 此处是阻塞的，那么running属性就算已经被改成false，那么怎么进入到下一次循环呢？
                        socket = serverSocketFactory.acceptSocket(serverSocket);//
                        System.out.println("接收到了一个socket连接");

                    } catch (IOException ioe) {
                        countDownConnection();
                        // Introduce delay if necessary
                        errorDelay = handleExceptionWithDelay(errorDelay);
                        // re-throw
                        throw ioe;
                    }
                    // Successful accept, reset the error delay
                    errorDelay = 0;

                    // Configure the socket
                    // 如果Endpoint正在运行并且没有被暂停，那么就处理该socket
                    if (running && !paused && setSocketOptions(socket)) {
                        // Hand this socket off to an appropriate processor
                        // socket被正常的交给了线程池，processSocket就会返回true
                        // 如果没有被交给线程池或者中途Endpoint被停止了，则返回false
                        // 返回false则关闭该socket
                        // 处理数据
                        if (!processSocket(socket)) {
                            countDownConnection();
                            // Close socket right away
                            closeSocket(socket);
                        }
                    } else {
                        countDownConnection();
                        // Close socket right away
                        closeSocket(socket);
                    }
                } catch (IOException x) {
                    if (running) {
                        log.error(sm.getString("endpoint.accept.fail"), x);
                    }
                } catch (NullPointerException npe) {
                    if (running) {
                        log.error(sm.getString("endpoint.accept.fail"), npe);
                    }
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    log.error(sm.getString("endpoint.accept.fail"), t);
                }
            }
            state = AcceptorState.ENDED;
        }
    }
```
#### 处理socket 数据 

   调用  SocketProcessor 线程类处理socket 数据
  
```
// package org.apache.tomcat.util.net.JIoEndpoint

  protected boolean processSocket(Socket socket) {
        // Process the request from this socket
        try {
            SocketWrapper<Socket> wrapper = new SocketWrapper<Socket>(socket);
            wrapper.setKeepAliveLeft(getMaxKeepAliveRequests());
            wrapper.setSecure(isSSLEnabled());
            // During shutdown, executor may be null - avoid NPE
            if (!running) {
                return false;
            }
            // bio， 一个socket连接对应一个线程
            // 一个http请求对应一个线程？
            getExecutor().execute(new SocketProcessor(wrapper));
        } catch (RejectedExecutionException x) {
            log.warn("Socket processing request was rejected for:"+socket,x);
            return false;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            // This means we got an OOM or similar creating a thread, or that
            // the pool and its queue are full
            log.error(sm.getString("endpoint.process.fail"), t);
            return false;
        }
        return true;
    }
```

####  SocketProcessor 线程类 

  调用  handler.process(SocketWrapper<Socket> socket,SocketStatus status) 方法
```
// package org.apache.tomcat.util.net.JIoEndpoint.SocketProcessor  JIoEndpoint 的内部类

        @Override
        public void run() {
            boolean launch = false;
            synchronized (socket) {
                // 开始处理socket
                // Socket默认状态为OPEN
                try {
                    SocketState state = SocketState.OPEN;

                    try {
                        // SSL handshake
                        serverSocketFactory.handshake(socket.getSocket());
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        if (log.isDebugEnabled()) {
                            log.debug(sm.getString("endpoint.err.handshake"), t);
                        }
                        // Tell to close the socket
                        state = SocketState.CLOSED;
                    }

                    // 当前socket没有关闭则处理socket
                    if ((state != SocketState.CLOSED)) {
                        // SocketState是Tomcat定义的一个状态,这个状态需要处理一下socket才能确定，因为跟客户端，跟具体的请求信息有关系
                        if (status == null) {
                            state = handler.process(socket, SocketStatus.OPEN_READ);
                        } else {
                            // status表示应该读数据还是应该写数据
                            // state表示处理完socket后socket的状态
                            state = handler.process(socket,status);
                        }
                    }
                    // 如果Socket的状态是被关闭，那么就减掉连接数并关闭socket
                    // 那么Socket的状态是在什么时候被关闭的？
                    if (state == SocketState.CLOSED) {
                        // Close socket
                        if (log.isTraceEnabled()) {
                            log.trace("Closing socket:"+socket);
                        }
                        countDownConnection();
                        try {
                            socket.getSocket().close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    } else if (state == SocketState.OPEN ||
                            state == SocketState.UPGRADING ||
                            state == SocketState.UPGRADING_TOMCAT  ||
                            state == SocketState.UPGRADED){
                        socket.setKeptAlive(true);
                        socket.access();
                        launch = true;
                    } else if (state == SocketState.LONG) {
                        // socket不会关闭，但是当前线程会执行结束
                        socket.access();
                        waitingRequests.add(socket);
                    }
                } finally {
                    if (launch) {
                        try {
                            getExecutor().execute(new SocketProcessor(socket, SocketStatus.OPEN_READ));
                        } catch (RejectedExecutionException x) {
                            log.warn("Socket reprocessing request was rejected for:"+socket,x);
                            try {
                                //unable to handle connection at this time
                                handler.process(socket, SocketStatus.DISCONNECT);
                            } finally {
                                countDownConnection();
                            }


                        } catch (NullPointerException npe) {
                            if (running) {
                                log.error(sm.getString("endpoint.launch.fail"),
                                        npe);
                            }
                        }
                    }
                }
            }
            socket = null;
            // Finish up this request
        }
```

#### AbstractProtocol<S>
   
   1.创建处理器 processor = createProcessor(); // HTTP11NIOProce
   
   2.大多数情况下走这个分支  state = processor.process(wrapper);
   
   
```
// package org.apache.coyote.AbstractProtocol<S>

public SocketState process(SocketWrapper<S> wrapper,
        SocketStatus status) {
    if (wrapper == null) {
        // Nothing to do. Socket has been closed.
        return SocketState.CLOSED;
    }

    S socket = wrapper.getSocket();
    if (socket == null) {
        // Nothing to do. Socket has been closed.
        return SocketState.CLOSED;
    }

    Processor<S> processor = connections.get(socket);
    if (status == SocketStatus.DISCONNECT && processor == null) {
        // Nothing to do. Endpoint requested a close and there is no
        // longer a processor associated with this socket.
        return SocketState.CLOSED;
    }
    // 设置为非异步，就是同步
    wrapper.setAsync(false);
    ContainerThreadMarker.markAsContainerThread();

    try {
        if (processor == null) {
            // 从被回收的processor中获取processor
            processor = recycledProcessors.poll();
        }
        if (processor == null) {
            //1.创建处理器
            processor = createProcessor(); // HTTP11NIOProce
        }

        initSsl(wrapper, processor);

        SocketState state = SocketState.CLOSED;
        do {
            if (status == SocketStatus.DISCONNECT &&
                    !processor.isComet()) {
                // Do nothing here, just wait for it to get recycled
                // Don't do this for Comet we need to generate an end
                // event (see BZ 54022)
            } else if (processor.isAsync() || state == SocketState.ASYNC_END) {
                // 要么Tomcat线程还没结束，业务线程就已经调用过complete方法了，然后利用while走到这个分支
                // 要么Tomcat线程结束后，在超时时间内业务线程调用complete方法，然后构造一个新的SocketProcessor对象扔到线程池里走到这个分支
                // 要么Tomcat线程结束后，超过超时时间了，由AsyncTimeout线程来构造一个SocketProcessor对象扔到线程池里走到这个分支
                // 不管怎么样，在整个调用异步servlet的流程中，此分支只经历一次，用来将output缓冲区中的内容发送出去

                state = processor.asyncDispatch(status);
                if (state == SocketState.OPEN) {
                    state = processor.process(wrapper);
                }
            } else if (processor.isComet()) {
                state = processor.event(status);
            } else if (processor.getUpgradeInbound() != null) {
                state = processor.upgradeDispatch();
            } else if (processor.isUpgrade()) {
                state = processor.upgradeDispatch(status);
            } else {
                // 2.大多数情况下走这个分支
                state = processor.process(wrapper);
            }

            if (state != SocketState.CLOSED && processor.isAsync()) {
                // 代码执行到这里，就去判断一下之前有没有调用过complete方法
                // 如果调用，那么当前的AsyncState就会从COMPLETE_PENDING-->调用doComplete方法改为COMPLETING，SocketState为ASYNC_END
                // 如果没有调用，那么当前的AsyncState就会从STARTING-->STARTED，SocketState为LONG
                //
                // 状态转换，有三种情况
                // 1. COMPLETE_PENDING--->COMPLETING，COMPLETE_PENDING是在调用complete方法时候由STARTING改变过来的
                // 2. STARTING---->STARTED，STARTED的下一个状态需要有complete方法来改变，会改成COMPLETING
                // 3. COMPLETING---->DISPATCHED
                state = processor.asyncPostProcess();
            }

            if (state == SocketState.UPGRADING) {
                // Get the HTTP upgrade handler
                HttpUpgradeHandler httpUpgradeHandler =
                        processor.getHttpUpgradeHandler();
                // Release the Http11 processor to be re-used
                release(wrapper, processor, false, false);
                // Create the upgrade processor
                processor = createUpgradeProcessor(
                        wrapper, httpUpgradeHandler);
                // Mark the connection as upgraded
                wrapper.setUpgraded(true);
                // Associate with the processor with the connection
                connections.put(socket, processor);
                httpUpgradeHandler.init((WebConnection) processor);
            } else if (state == SocketState.UPGRADING_TOMCAT) {
                // Get the UpgradeInbound handler
                org.apache.coyote.http11.upgrade.UpgradeInbound inbound =
                        processor.getUpgradeInbound();
                // Release the Http11 processor to be re-used
                release(wrapper, processor, false, false);
                // Create the light-weight upgrade processor
                processor = createUpgradeProcessor(wrapper, inbound);
                inbound.onUpgradeComplete();
            }
            if (getLog().isDebugEnabled()) {
                getLog().debug("Socket: [" + wrapper +
                        "], Status in: [" + status +
                        "], State out: [" + state + "]");
            }
            // 如果在访问异步servlet时，代码执行到这里，已经调用过complete方法了，那么状态就是SocketState.ASYNC_END
        } while (state == SocketState.ASYNC_END ||
                state == SocketState.UPGRADING ||
                state == SocketState.UPGRADING_TOMCAT);

        if (state == SocketState.LONG) {
          
            connections.put(socket, processor);
            longPoll(wrapper, processor);
        } else if (state == SocketState.OPEN) {
            // In keep-alive but between requests. OK to recycle
            // processor. Continue to poll for the next request.
            connections.remove(socket);
            release(wrapper, processor, false, true);
        } else if (state == SocketState.SENDFILE) {
         
            connections.put(socket, processor);
        } else if (state == SocketState.UPGRADED) {
            // Need to keep the connection associated with the processor
            connections.put(socket, processor);
         
            if (status != SocketStatus.OPEN_WRITE) {
                longPoll(wrapper, processor);
            }
        } else {
            // Connection closed. OK to recycle the processor. Upgrade
            // processors are not recycled.
            connections.remove(socket);
            if (processor.isUpgrade()) {
                processor.getHttpUpgradeHandler().destroy();
            } else if (processor instanceof org.apache.coyote.http11.upgrade.UpgradeProcessor) {
                // NO-OP
            } else {
                release(wrapper, processor, true, false);
            }
        }
        return state;
    } catch(java.net.SocketException e) {
        // SocketExceptions are normal
        getLog().debug(sm.getString(
                "abstractConnectionHandler.socketexception.debug"), e);
    } catch (java.io.IOException e) {
        // IOExceptions are normal
        getLog().debug(sm.getString(
                "abstractConnectionHandler.ioexception.debug"), e);
    }
 
    catch (Throwable e) {
        ExceptionUtils.handleThrowable(e);
      
        getLog().error(
                sm.getString("abstractConnectionHandler.error"), e);
    }
    // Make sure socket/processor is removed from the list of current
    // connections
    connections.remove(socket);
    // Don't try to add upgrade processors back into the pool
    if (!(processor instanceof org.apache.coyote.http11.upgrade.UpgradeProcessor)
            && !processor.isUpgrade()) {
        release(wrapper, processor, true, false);
    }
    return SocketState.CLOSED;
}
```

####  调用 AbstractProtocol 的具体实现类 以 AbstractHttp11Processor<S> 为例
 
   解析请求行
   
   解析请求头
   
   交给容器处理请求  adapter.service(request, response);  调用容器进行处理, 直接调用的StandardEngineValve,然后根据管道依次调用四大容器
   
```
// package org.apache.coyote.http11.AbstractHttp11Processor<S>

@Override
public SocketState process(SocketWrapper<S> socketWrapper)
    throws IOException {
    RequestInfo rp = request.getRequestProcessor();
    rp.setStage(org.apache.coyote.Constants.STAGE_PARSE);   // 设置请求状态为解析状态

    // Setting up the I/O
    setSocketWrapper(socketWrapper);
    getInputBuffer().init(socketWrapper, endpoint);     // 将socket的InputStream与InternalInputBuffer进行绑定
    getOutputBuffer().init(socketWrapper, endpoint);    // 将socket的OutputStream与InternalOutputBuffer进行绑定

    // Flags
    keepAlive = true;
    comet = false;
    openSocket = false;
    sendfileInProgress = false;
    readComplete = true;
    // NioEndpoint返回true, Bio返回false
    if (endpoint.getUsePolling()) {
        keptAlive = false;
    } else {
        keptAlive = socketWrapper.isKeptAlive();
    }

    // 如果当前活跃的线程数占线程池最大线程数的比例大于75%，那么则关闭KeepAlive，不再支持长连接
    if (disableKeepAlive()) {
        socketWrapper.setKeepAliveLeft(0);
    }

    // keepAlive默认为true,它的值会从请求中读取
    while (!getErrorState().isError() && keepAlive && !comet && !isAsync() &&
            upgradeInbound == null &&
            httpUpgradeHandler == null && !endpoint.isPaused()) {
        // keepAlive如果为true,接下来需要从socket中不停的获取http请求

        // Parsing the request header
        try {
            // 第一次从socket中读取数据，并设置socket的读取数据的超时时间
            // 对于BIO，一个socket连接建立好后，不一定马上就被Tomcat处理了，其中需要线程池的调度，所以这段等待的时间要算在socket读取数据的时间内
            // 而对于NIO而言，没有阻塞
            setRequestLineReadTimeout();

            // 解析请求行
            if (!getInputBuffer().parseRequestLine(keptAlive)) {
                // 下面这个方法在NIO时有用，比如在解析请求行时，如果没有从操作系统读到数据，则上面的方法会返回false
                // 而下面这个方法会返回true，从而退出while，表示此处read事件处理结束
                // 到下一次read事件发生了，就会从小进入到while中
                if (handleIncompleteRequestLineRead()) {
                    break;
                }
            }

            if (endpoint.isPaused()) {
                // 503 - Service unavailable
                // 如果Endpoint被暂停了，则返回503
                response.setStatus(503);
                setErrorState(ErrorState.CLOSE_CLEAN, null);
            } else {
                keptAlive = true;
                // Set this every time in case limit has been changed via JMX
                // 每次处理一个请求就重新获取一下请求头和cookies的最大限制
                request.getMimeHeaders().setLimit(endpoint.getMaxHeaderCount());
                request.getCookies().setLimit(getMaxCookieCount());
                // Currently only NIO will ever return false here
                // 解析请求头
                if (!getInputBuffer().parseHeaders()) {
                    // We've read part of the request, don't recycle it
                    // instead associate it with the socket
                    openSocket = true;
                    readComplete = false;
                    break;
                }
                if (!disableUploadTimeout) {
                    setSocketTimeout(connectionUploadTimeout);
                }
            }
        } catch (IOException e) {
            if (getLog().isDebugEnabled()) {
                getLog().debug(
                        sm.getString("http11processor.header.parse"), e);
            }
            setErrorState(ErrorState.CLOSE_NOW, e);
            break;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            UserDataHelper.Mode logMode = userDataHelper.getNextMode();
            if (logMode != null) {
                String message = sm.getString(
                        "http11processor.header.parse");
                switch (logMode) {
                    case INFO_THEN_DEBUG:
                        message += sm.getString(
                                "http11processor.fallToDebug");
                        //$FALL-THROUGH$
                    case INFO:
                        getLog().info(message, t);
                        break;
                    case DEBUG:
                        getLog().debug(message, t);
                }
            }
            // 400 - Bad Request
            response.setStatus(400);
            setErrorState(ErrorState.CLOSE_CLEAN, t);
            getAdapter().log(request, response, 0);
        }

        if (!getErrorState().isError()) {
            // Setting up filters, and parse some request headers
            rp.setStage(org.apache.coyote.Constants.STAGE_PREPARE);  // 设置请求状态为预处理状态
            try {
                prepareRequest();   // 预处理, 主要从请求中处理处keepAlive属性，以及进行一些验证，以及根据请求分析得到ActiveInputFilter
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                if (getLog().isDebugEnabled()) {
                    getLog().debug(sm.getString(
                            "http11processor.request.prepare"), t);
                }
                // 500 - Internal Server Error
                response.setStatus(500);
                setErrorState(ErrorState.CLOSE_CLEAN, t);
                getAdapter().log(request, response, 0);
            }
        }

        if (maxKeepAliveRequests == 1) {
            // 如果最大的活跃http请求数量仅仅只能为1的话，那么设置keepAlive为false，则不会继续从socket中获取Http请求了
            keepAlive = false;
        } else if (maxKeepAliveRequests > 0 &&
                socketWrapper.decrementKeepAlive() <= 0) {
            // 如果已经达到了keepAlive的最大限制，也设置为false，则不会继续从socket中获取Http请求了
            keepAlive = false;
        }

        // Process the request in the adapter
        if (!getErrorState().isError()) {
            try {
                rp.setStage(org.apache.coyote.Constants.STAGE_SERVICE); // 设置请求的状态为服务状态，表示正在处理请求
                adapter.service(request, response); // 交给容器处理请求
                // Handle when the response was committed before a serious
                // error occurred.  Throwing a ServletException should both
                // set the status to 500 and set the errorException.
                // If we fail here, then the response is likely already
                // committed, so we can't try and set headers.
                if(keepAlive && !getErrorState().isError() && (
                        response.getErrorException() != null ||
                                (!isAsync() &&
                                statusDropsConnection(response.getStatus())))) {
                    setErrorState(ErrorState.CLOSE_CLEAN, null);
                }
                setCometTimeouts(socketWrapper);
            } catch (InterruptedIOException e) {
                setErrorState(ErrorState.CLOSE_NOW, e);
            } catch (HeadersTooLargeException e) {
                getLog().error(sm.getString("http11processor.request.process"), e);
                // The response should not have been committed but check it
                // anyway to be safe
                if (response.isCommitted()) {
                    setErrorState(ErrorState.CLOSE_NOW, e);
                } else {
                    response.reset();
                    response.setStatus(500);
                    setErrorState(ErrorState.CLOSE_CLEAN, e);
                    response.setHeader("Connection", "close"); // TODO: Remove
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                getLog().error(sm.getString("http11processor.request.process"), t);
                // 500 - Internal Server Error
                response.setStatus(500);
                setErrorState(ErrorState.CLOSE_CLEAN, t);
                getAdapter().log(request, response, 0);
            }
        }

        // Finish the handling of the request
        rp.setStage(org.apache.coyote.Constants.STAGE_ENDINPUT);  // 设置请求的状态为处理请求结束

        if (!isAsync() && !comet) {
            if (getErrorState().isError()) {
                // If we know we are closing the connection, don't drain
                // input. This way uploading a 100GB file doesn't tie up the
                // thread if the servlet has rejected it.
                getInputBuffer().setSwallowInput(false);
            } else {
                // Need to check this again here in case the response was
                // committed before the error that requires the connection
                // to be closed occurred.
                checkExpectationAndResponseStatus();
            }
            // 当前http请求已经处理完了，做一些收尾工作
            endRequest();
        }

        rp.setStage(org.apache.coyote.Constants.STAGE_ENDOUTPUT); // 请求状态为输出结束

        // If there was an error, make sure the request is counted as
        // and error, and update the statistics counter
        if (getErrorState().isError()) {
            response.setStatus(500);
        }
        request.updateCounters();

        if (!isAsync() && !comet || getErrorState().isError()) {
            if (getErrorState().isIoAllowed()) {
                // 准备处理下一个请求
                getInputBuffer().nextRequest();
                getOutputBuffer().nextRequest();
            }
        }

        if (!disableUploadTimeout) {
            if(endpoint.getSoTimeout() > 0) {
                setSocketTimeout(endpoint.getSoTimeout());
            } else {
                setSocketTimeout(0);
            }
        }

        rp.setStage(org.apache.coyote.Constants.STAGE_KEEPALIVE);

        // 如果处理完当前这个Http请求之后，发现socket里没有下一个请求了,那么就退出当前循环
        // 如果是keepalive，就不会关闭socket, 如果是close就会关闭socket
        // 对于keepalive的情况，因为是一个线程处理一个socket,当退出这个while后，当前线程就会介绍，
        // 当时对于socket来说，它仍然要继续介绍连接，所以又会新开一个线程继续来处理这个socket
        if (breakKeepAliveLoop(socketWrapper)) {
            break;
        }
    }
    // 至此，循环结束

    rp.setStage(org.apache.coyote.Constants.STAGE_ENDED);

    // 主要流程就是将socket的状态设置为CLOSED
    if (getErrorState().isError() || endpoint.isPaused()) {
        return SocketState.CLOSED;
    } else if (isAsync() || comet) {
        // 异步servlet
        return SocketState.LONG;
    } else if (isUpgrade()) {
        return SocketState.UPGRADING;
    } else if (getUpgradeInbound() != null) {
        return SocketState.UPGRADING_TOMCAT;
    } else {
        if (sendfileInProgress) {
            return SocketState.SENDFILE;
        } else {
            // openSocket为true，表示不要关闭socket
            if (openSocket) {
                // readComplete表示本次读数据是否完成，比如nio中可能就没有读完数据，还需要从socket中读数据
                if (readComplete) {
                    return SocketState.OPEN;
                } else {
                    // nio可能会走到这里
                    return SocketState.LONG;
                }
            } else {
                return SocketState.CLOSED;
            }
        }
    }
}

```









 
 
 


   