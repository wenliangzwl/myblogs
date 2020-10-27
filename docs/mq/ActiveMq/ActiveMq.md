### ActiveMQ原理剖析

#### 两种运行模型
   
   PTP点对点通信：
   
   使用queue作为信息载体，满足生产者与消费者模式，一个消息只能被一个消费者使用，没有被消费的消息可以持久保持在queue 中等待被消费。
   
   Pub/Sub发布订阅模式：
   
   使用Topic主题作为通信载体，类似于广播模式，在消息广播期间，所有的订阅者都可以接受到广播消息，在一条消息广播之后才订阅的用户是收不到该条消息的。


#### ActiveMQ的组成模块
   
   Broker:消息服务器，作为server提供消息核心服务。
   
   Producer:消息生产者，业务的发起方，负责生产消息传输给broker。
   
   Consumer:消息消费者，业务的处理方，负责从broker获取消息并进行业务逻辑处理。
   
   Topic:主题，发布订阅模式下的消息统一汇集地，不同生产者向topic发送消息，由MQ服务器分发到不同的订阅者，实现消息的广播。
   
   Queue:队列，PTP模式下，特定生产者向特定queue发送消息，消费者订阅特定的 queue完成指定消息的接收。
   
   Message:消息体，根据不同通信协议定义的固定格式进行编码的数据包，来封装业务 数据，实现消息的传输。

#### ActiveMQ的常用协议
   
   AMQP协议
   AMQP即Advanced Message Queuing Protocol,一个提供统一消息服务的应用层标准高级消 息队列协议,是应用层协议的一个开放标准,为面向消息的中间件设计。基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件不同产品，不同开发语言等条件的限制。
   
   
   MQTT协议
   MQTT(Message Queuing Telemetry Transport，消息队列遥测传输)是IBM开发的一个即时 通讯协议，有可能成为物联网的重要组成部分。该协议支持所有平台，几乎可以把所有联网 物品和外部连接起来，被用来当做传感器和致动器(比如通过Twitter让房屋联网)的通信协 议。
   
   
   STOMP协议
   STOMP(Streaming Text Orientated Message Protocol)是流文本定向消息协议，是一种为 MOM(Message Oriented Middleware，面向消息的中间件)设计的简单文本协议。STOMP提 供一个可互操作的连接格式，允许客户端与任意STOMP消息代理(Broker)进行交互。
   
   
   OPENWIRE协议
   ActiveMQ特有的协议，官方描述如下
   
   OpenWire is our cross language Wire Protocol to allow native access to ActiveMQ from a number of different languages and platforms. The Java OpenWire transport is the default transport in ActiveMQ 4.x or later. For other languages see the following...

   对于ActiveMQ的上述协议，每种协议端口都不一样，可以自行修改。
   
   编辑activemq.xml,在transportConnectors标签中注销、修改或删除不使用的协议。
```
<transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
<transportConnector name="amqp" uri="amqp://0.0.0.0:5672?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
<transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
<transportConnector name="mqtt" uri="mqtt://0.0.0.0:1883?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
<transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
```

### 