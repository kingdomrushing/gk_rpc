package com.xg.gkrpc.transport;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
