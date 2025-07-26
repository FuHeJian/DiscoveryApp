# Netty-Java 架构原理图

## 1. Netty 整体架构图

```mermaid
graph TB
    subgraph "Application Layer"
        A[应用程序]
    end
    
    subgraph "Netty Core"
        B[Bootstrap]
        C[EventLoopGroup]
        D[Channel]
        E[ChannelPipeline]
        F[ChannelHandler]
    end
    
    subgraph "Transport Layer"
        G[NIO Selector]
        H[Socket Channel]
    end
    
    subgraph "Network Layer"
        I[TCP/UDP]
        J[Network Interface]
    end
    
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    D --> G
    G --> H
    H --> I
    I --> J
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style C fill:#e8f5e8
    style D fill:#fff3e0
    style E fill:#fce4ec
    style F fill:#f1f8e9
```

## 2. EventLoop 工作原理

```mermaid
graph LR
    subgraph "EventLoopGroup"
        A[EventLoop 1]
        B[EventLoop 2]
        C[EventLoop 3]
        D[EventLoop N]
    end
    
    subgraph "Channel Registration"
        E[Channel 1]
        F[Channel 2]
        G[Channel 3]
        H[Channel M]
    end
    
    subgraph "Event Processing"
        I[Accept Event]
        J[Read Event]
        K[Write Event]
        L[Connect Event]
    end
    
    A --> E
    B --> F
    C --> G
    D --> H
    
    E --> I
    F --> J
    G --> K
    H --> L
    
    style A fill:#e3f2fd
    style B fill:#e3f2fd
    style C fill:#e3f2fd
    style D fill:#e3f2fd
    style E fill:#f3e5f5
    style F fill:#f3e5f5
    style G fill:#f3e5f5
    style H fill:#f3e5f5
```

## 3. ChannelPipeline 数据流

```mermaid
graph LR
    subgraph "Inbound Pipeline"
        A[ChannelInboundHandler 1]
        B[ChannelInboundHandler 2]
        C[ChannelInboundHandler 3]
    end
    
    subgraph "Outbound Pipeline"
        D[ChannelOutboundHandler 1]
        E[ChannelOutboundHandler 2]
        F[ChannelOutboundHandler 3]
    end
    
    subgraph "Network"
        G[Network Data]
    end
    
    G --> A
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    
    style A fill:#e8f5e8
    style B fill:#e8f5e8
    style C fill:#e8f5e8
    style D fill:#fff3e0
    style E fill:#fff3e0
    style F fill:#fff3e0
    style G fill:#fce4ec
```

## 4. Bootstrap 启动流程

```mermaid
sequenceDiagram
    participant App as 应用程序
    participant Bootstrap as Bootstrap
    participant EventLoopGroup as EventLoopGroup
    participant Channel as Channel
    participant Pipeline as ChannelPipeline
    
    App->>Bootstrap: 创建Bootstrap
    Bootstrap->>EventLoopGroup: 设置EventLoopGroup
    Bootstrap->>Channel: 设置Channel类型
    Bootstrap->>Pipeline: 添加ChannelHandler
    Bootstrap->>Channel: 绑定端口
    Channel->>EventLoopGroup: 注册到EventLoop
    EventLoopGroup->>Channel: 开始监听事件
    Channel->>Pipeline: 处理网络事件
```

## 5. 线程模型

```mermaid
graph TB
    subgraph "Boss EventLoopGroup"
        A[Boss EventLoop 1]
        B[Boss EventLoop 2]
    end
    
    subgraph "Worker EventLoopGroup"
        C[Worker EventLoop 1]
        D[Worker EventLoop 2]
        E[Worker EventLoop 3]
        F[Worker EventLoop 4]
    end
    
    subgraph "Channel Handlers"
        G[Handler 1]
        H[Handler 2]
        I[Handler 3]
        J[Handler 4]
    end
    
    A --> C
    A --> D
    B --> E
    B --> F
    
    C --> G
    D --> H
    E --> I
    F --> J
    
    style A fill:#ffebee
    style B fill:#ffebee
    style C fill:#e8f5e8
    style D fill:#e8f5e8
    style E fill:#e8f5e8
    style F fill:#e8f5e8
```

## 6. 数据编解码流程

```mermaid
graph LR
    subgraph "Network"
        A[Raw Bytes]
    end
    
    subgraph "Decoder"
        B[ByteToMessageDecoder]
        C[MessageToMessageDecoder]
    end
    
    subgraph "Business Logic"
        D[Business Handler]
    end
    
    subgraph "Encoder"
        E[MessageToMessageEncoder]
        F[MessageToByteEncoder]
    end
    
    subgraph "Network"
        G[Raw Bytes]
    end
    
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    
    style A fill:#fce4ec
    style B fill:#e3f2fd
    style C fill:#e3f2fd
    style D fill:#e8f5e8
    style E fill:#fff3e0
    style F fill:#fff3e0
    style G fill:#fce4ec
```

## 7. 连接生命周期

```mermaid
stateDiagram-v2
    [*] --> Created
    Created --> Registered
    Registered --> Active
    Active --> Inactive
    Inactive --> Unregistered
    Unregistered --> [*]
    
    Active --> Active : Read/Write Events
    Active --> Inactive : Connection Lost
    Inactive --> Active : Reconnect
```

## 8. 内存管理

```mermaid
graph TB
    subgraph "Pooled ByteBuf"
        A[PooledDirectByteBuf]
        B[PooledHeapByteBuf]
    end
    
    subgraph "Unpooled ByteBuf"
        C[UnpooledDirectByteBuf]
        D[UnpooledHeapByteBuf]
    end
    
    subgraph "Memory Pool"
        E[PoolArena]
        F[PoolChunk]
        G[PoolSubpage]
    end
    
    A --> E
    B --> E
    E --> F
    F --> G
    
    style A fill:#e8f5e8
    style B fill:#e8f5e8
    style C fill:#fff3e0
    style D fill:#fff3e0
    style E fill:#f3e5f5
    style F fill:#f3e5f5
    style G fill:#f3e5f5
```

## 核心组件说明

### 1. Bootstrap
- **作用**: 客户端和服务端的启动引导类
- **功能**: 配置Channel、EventLoopGroup、ChannelHandler等

### 2. EventLoopGroup
- **作用**: 管理EventLoop的线程池
- **功能**: 处理Channel的I/O操作和事件

### 3. Channel
- **作用**: 网络连接的抽象
- **功能**: 代表一个网络连接，可以读写数据

### 4. ChannelPipeline
- **作用**: ChannelHandler的容器
- **功能**: 处理入站和出站的数据流

### 5. ChannelHandler
- **作用**: 处理I/O事件和数据
- **类型**: 
  - ChannelInboundHandler: 处理入站数据
  - ChannelOutboundHandler: 处理出站数据

### 6. ByteBuf
- **作用**: Netty的字节缓冲区
- **特点**: 比Java NIO ByteBuffer更高效

## 工作原理总结

1. **启动阶段**: Bootstrap配置并启动EventLoopGroup
2. **连接阶段**: 创建Channel并注册到EventLoop
3. **事件处理**: EventLoop监听网络事件并分发给ChannelPipeline
4. **数据处理**: ChannelHandler链式处理数据
5. **资源管理**: 自动管理内存和连接资源

Netty通过这种设计实现了高性能、高并发的网络应用框架。 