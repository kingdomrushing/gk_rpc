package com.xg.gkrpc.transport;

import com.xg.gkrpc.proto.Peer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpTransportClient implements TransportClient{
    private String url;

    @Override
    public void connect(Peer peer) {
        this.url = "http://" + peer.getHost() + ":" + peer.getPort();
    }

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
