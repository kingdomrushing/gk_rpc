# 基于Jetty的RPC框架实现

## 项目模块
* gkrpc-common     公共方法定义
* gkrpc-proto      协议模块
* gkrpc-codec      序列化模块
* gkrpc-transport  网络通信模块
* gkrpc-server     服务端模块
* gkrpc-client     客户端模块
* gkrpc-example    测试模块

## 1. gkrpc-common 公共方法定义模块
> 该模块目前主要为一些反射工具，其具体实现如下:
```
public class ReflectionUtils {

    /**
     * 根据class创建对象
     * @param clazz 待创建对象的类
     * @param <T> 对象类型
     * @return 创建好的对象
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取某个class的公有方法
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Method[] getPublicMethods(Class<T> clazz) {
        // 类自身所有的方法，包括私有，公共等，不包含父类的
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> pMethods = new ArrayList<>();
        for (Method declaredMethod : declaredMethods) {
            if (Modifier.isPublic(declaredMethod.getModifiers())) {
                pMethods.add(declaredMethod);
            }
        }
        return pMethods.toArray(new Method[0]);
    }

    /**
     * 调用指定对象的指定方法，若是静态方法，则obj为NULL
     * @param obj 指定对象
     * @param method 指定方法
     * @param args 参数
     * @return 方法结果
     */
    public static Object invoke(Object obj,
                                Method method,
                                Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
```
>> 上述getPublicMethods()方法一个用途是获取Server的所有公共方法并注册这些服务。 invoke()方法用于执行指定实例对象的method。
## 2. gkrpc-proto 协议模块
> 该模块用于规定数据传输协议和规约，其主要类有4个：
>> 2.1 Peer类表示网络传输的一个端点
```
@Data
@AllArgsConstructor
public class Peer {

    private String host;

    private Integer port;
}
```
>> 2.2 ServiceDescriptor类用来描述某项服务，即某个类的具体方法。
```
public class ServiceDescriptor {

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * 参数类型
     */
    private String[] parameterTypes;
    
    // ...
}
```

>> 2.3 Request类用于储存某一需要执行方法的method描述（即serviceDescriptor）与实参。
```
@Data
public class Request {
    private ServiceDescriptor serviceDescriptor;
    private Object [] parameters;
}

// 这里的实参将会在client调用方法时通过动态代理获取，并且通过http协议传递到Server进行处理。
// 而在Server中会根据传递的class与实例通过反射进行实际方法的执行，最后将执行结果通过Response类进行返回。  
```
>> 2.4 Reponse类描述如下：
```
@Data
public class Response {
    /**
     * 服务返回编码，0-成功 ，非0失败
     */
    private int code;//成功与否
    /**
     * 具体的错误信息
     */
    private String message  = "ok";
    /**
     * 返回的数据
     */
    private Object data;
}

```
## 3. gkrpc-codec 序列化与反序列化模块
> 数据在网络中传输是以流的形式，Request请求需要先序列化成数据流，然后在网络中传输，服务端接收到请求后，需要先将数据流反序列化成Request对象
>> 这里已实现了JSON序列化与反序列化方法
## 4 gkrpc-transport 网络通信模块
> 该模块主要用于client与server的http通信处理问题，其client请求内容以Request类形式封装传输，server响应内容以Reponse类封装返回。
>> 4.1 HTTPTransportClient类实现如下：
```
public class HttpTransportClient implements TransportClient{
    private String url;

    @Override
    public void connect(Peer peer) {
        this.url = "http://" + peer.getHost() + ":" + peer.getPort();
    }

    // 向server传递数据并且获取响应数据。其最终调用将在RPCClient类中调用
    @Override
    public InputStream write(InputStream data) {
        try {
            // 建立http连接
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            // 将data请求数据发送给该地址的服务
            // outputStream: Returns an output stream that writes to this connection.
            IOUtils.copy(data, urlConnection.getOutputStream());

            int result = urlConnection.getResponseCode();
            if (result == HttpURLConnection.HTTP_OK) {
                // 成功发送请求，则从输入流中获取数据
                return urlConnection.getInputStream();
            } else {
                return urlConnection.getErrorStream();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {

    }
}
```
> 4.2 HTTPTransportServer 类主要实现如下：
```
@Slf4j
public class HttpTransportServer implements TransportServer{

    private RequestHandler requestHandler;

    private Server server;

    @Override
    public void init(int port, RequestHandler handler) {
        this.requestHandler = handler;
        // 创建Jetty的服务
        this.server = new Server(port);

        // servlet 接收请求，针对每个请求，Jetty会从线程池中拿出一个线程去处理请求
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        // 注册到server中
        server.setHandler(servletContextHandler);

        // 创建ServletHolder托管RequestServlet
        ServletHolder servletHolder = new ServletHolder(new RequestServlet());
        servletContextHandler.addServlet(servletHolder, "/*");
    }

    @Override
    public void start() {
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    class RequestServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            log.info("client post data !!!");
            ServletInputStream inputStream = req.getInputStream();
            ServletOutputStream outputStream = resp.getOutputStream();
            if (requestHandler != null) {
                requestHandler.onRequest(inputStream, outputStream);
            }
            outputStream.flush();
        }
    }
}


```
>> 上述类使用Jetty容器完成init(),start(),stop()功能。上述类最重要一个关注点在于RequestHandler实例的初始化，该抽象类定义于Transport模块，主要用于server处理来自client的请求。其抽象方法实现将在RPCServer类中详细讲解。

## 5 gkrpc-server 模块
> 本项目最核心两个模块之一，主要作用是定义了处理client请求的方法。
>> 5.1 RPCServer类实现如下：
```
/**
     * 服务配置信息
     */
    private RPCServerConfig config;

    private TransportServer net;

    private Encoder encoder;

    private Decoder decoder;

    private ServiceManager serviceManager;

    private RequestHandler handler = new RequestHandler() {
        @Override
        public void onRequest(InputStream inputStream, OutputStream outputStream) {
            Response response = new Response();
            try {
                // 处理请求
                byte[] bytes = IOUtils.readFully(inputStream, inputStream.available(), true);
                Request request = decoder.decode(bytes, Request.class);
                log.info("get request: {}", request.toString());
                ServiceInstance serviceInstance = serviceManager.lookup(request);
                if (serviceInstance == null) {
                    throw new IllegalStateException("service not exist!");
                }
                Object res = ServiceInvoke.invoke(serviceInstance, request);
                response.setData(res);

            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                response.setCode(400);
                response.setMessage("RpcServer get Error: " + e.getClass().getName() + "\n :" + e.getMessage());
            } finally {
                byte[] bytes = encoder.encode(response);
                try {
                    outputStream.write(bytes);
                    log.info("response to client!");
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
    };

    public RPCServer() {
        this(new RPCServerConfig());
    }

    public RPCServer(RPCServerConfig rpcServerConfig) {
        this.config = rpcServerConfig;

        this.encoder = ReflectionUtils.newInstance(config.getEncoder());
        this.decoder = ReflectionUtils.newInstance(config.getDecoder());
        this.net = ReflectionUtils.newInstance(config.getTransportClass());
        this.net.init(config.getPort(), this.handler);

        this.serviceManager = new ServiceManager();
    }

    public <T> void register(Class<T> interfaceClass, T bean) {
        serviceManager.register(interfaceClass, bean);
    }

    public void start() {
        this.net.start();
    }

    public void stop() {
        this.net.stop();
    }
}

// 该方法初时较复杂，理清各个类之后将比较明了。
// 上述RPCServerConfig主要用于常量配置的定义，Encoder与Decoder分别为编码器与解码器不做过多解释。
``` 
>> 5.2 ServiceManager类的实现如下:
```
/**
 * 管理RPC暴露的服务
 */
@Slf4j
public class ServiceManager {
    /**
     * 保存注册的service
     */
    private Map<ServiceDescriptor, ServiceInstance> services;

    public ServiceManager() {
        this.services = new ConcurrentHashMap<>();
    }

    /**
     * 注册服务
     * @param interfaceClass
     * @param bean
     * @param <T>
     */
    public <T> void register(Class<T> interfaceClass, T bean) {
        Method[] methods = ReflectionUtils.getPublicMethods(interfaceClass);
        for (Method method : methods) {
            ServiceInstance serviceInstance = new ServiceInstance(bean, method);
            ServiceDescriptor sd = ServiceDescriptor.getServiceDescriptor(interfaceClass, method);
            this.services.put(sd, serviceInstance);

            log.info("register service: {} {}", sd.getClassName(), sd.getMethodName());
        }
    }

    /**
     * 查找服务
     * @param request
     * @return
     */
    public ServiceInstance lookup(Request request) {
        return services.get(request.getServiceDescriptor());
    }
```
>> register()方法主要用于注册该class的所有共有方法，并且获取之前讲述的ServiceDescriptor实例与ServiceInstance作为键值对的形式存储。**（需要注意的是这里的registetr方法的参数bean正是需要执行的实例对象）**其ServiceInstance类的定义如下：
```
@Data
@AllArgsConstructor
public class ServiceInstance {
    private Object target;
    private Method method;
}
```
>> 其内部主要定义了连个变量，一个是需要执行某个method的目标对象，另一个是需要执行的method。**（到这里应该认识了ServiceManager的真正作用，存储method的描述与实例的对应关系，方便通过client的传参进行get）**  
>> 回到上述初始类RpcServer，最需要注意的是RequestHandler的实现。其onRequest()方法通过Servlet的inputStream与OutputStream参数获取来自Client的数据，并且通过获取到的Request实例参数从ServiceManager中get实例对象与method。因为Request对象中包含有Client获取到的实际参数，因此将上述参数一起传递到ServiceInvoker对象进行执行。该类实现如下：
```
/**
 * 通过反射执行服务实例的方法
 */
public class ServiceInvoke {

    public static Object invoke(ServiceInstance serviceInstance, Request request) {
        return ReflectionUtils.invoke(
                serviceInstance.getTarget(),
                serviceInstance.getMethod(),
                request.getParameters());
    }
}
```
>> 上述代码最终只是调用common模块的反射工具封装执行。
## 6 gkrpc-client 模块
> 该模块主要功能有连个一个是动态代理获取实参，一个是请求Server进行过程调用。
>> 其RPCClient类实现如下：
```
public class RPCClient {

    private RPCClientConfig config;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;

    public RPCClient() {
        this(new RPCClientConfig());
    }

    public RPCClient(RPCClientConfig config) {
        this.config = config;

        this.encoder = ReflectionUtils.newInstance(config.getEncoderClass());
        this.decoder = ReflectionUtils.newInstance(config.getDecoderClass());
        this.selector = ReflectionUtils.newInstance(config.getSelectorClass());
        this.selector.init(config.getServers(), config.getConnectCount(), config.getTransportClass());
    }

    /**
     * 创建代理对象,由代理对象去发送请求
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{clazz},
                new RemoteHandler(clazz, encoder, decoder, selector));
    }
}
```
>> 该类需要注意两点，之一是TransportSelector对象，其实现如下：
```
@Slf4j
public class RandomTransportSelector implements TransportSelector {

    /**
     * 已经连接好的client
     */
    private List<TransportClient> clients;
    public RandomTransportSelector() {
        clients = new ArrayList<>();
    }

    @Override
    public synchronized void init(List<Peer> peers, int count, Class<? extends TransportClient> clazz) {
        count = Math.max(count,1);

        for (Peer peer : peers){
            for (int i =0;i<count;i++){
                TransportClient client = ReflectionUtils.newInstance(clazz);
                client.connect(peer);
                clients.add(client);
            }
            log.info("connect server: {} ",peer);
        }
    }

    @Override
    public synchronized TransportClient select() {
        int i = new Random().nextInt(clients.size());
        return clients.remove(i);
    }

    @Override
    public synchronized void release(TransportClient client) {
        clients.add(client);
    }

    @Override
    public synchronized void close() {
        for (TransportClient client :clients){
            client.close();
        }
        clients.clear();
    }
}
```
>> 该类主要是用于处理Client对Server的连接问题，相当于连接池，由有需求时随机返回连接。
>> 回到上述RpcClient类的getProxy()方法为动态代理，不为此介绍的重点，但是需要重点关注RemoteInvoker类，实现如下：
```
Slf4j
public class RemoteInvoker implements InvocationHandler{

    private Class clazz;
    private Encoder encoder;
    private Decoder decoder;
    private TransportSelector selector;

    public RemoteInvoker(Class clazz , Encoder encoder, Decoder decoder, TransportSelector selector) {
        this.clazz = clazz;
        this.decoder = decoder;
        this.encoder = encoder;
        this.selector = selector;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Request request = new Request();
        request.setServiceDescriptor(ServiceDescriptor.from(clazz,method));
        request.setParameters(args);

        Response response = invokeRemote(request);
        if(response==null || response.getCode()!=0){
            throw new IllegalStateException("fail to invoke remote: "+response);
        }
        return response.getData();
    }
    private Response invokeRemote(Request request){
        TransportClient client = null;
        Response response = null;
        try{
            client = selector.select();

            byte[] outBytes = encoder.encode(request);
            InputStream revice = client.write(new ByteArrayInputStream(outBytes));

            byte[] inBytes = new byte[revice.available()];
            IOUtils.readFully(revice,inBytes,0,revice.available());

           // byte[] inBytes = IOUtils.readFully(revice , revice.available());

             response = decoder.decode(inBytes,Response.class);

        }catch (IOException e) {
            log.warn(e.getMessage(),e);
            response = new Response();
            response.setCode(1);
            response.setMessage("RpcClient got error:"+e.getClass()+" : "+e.getMessage());
        }finally {
            if(client!=null){
                selector.release(client);
            }
        }
        return response;
    }
}

```
>> 上述代码需要关注invoke()方法中对代理方法的参数进行存储封装到Request对象并且最终序列化传递到Server。至此本项目个关键模块实现与执行流程介绍完毕。

# 下一步
> * 服务端线程池
> * 客户端负载均衡
> * 注册中心
> * 数据安全传输
> * 其他网络传输协议
> * 流行框架集成