
package remoting;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import sun.rmi.runtime.Log;

import java.io.Closeable;
import java.util.Map;

/**
 * NettyClient.
 */
@Slf4j
public class NettyClient implements Closeable{

    protected Bootstrap b;

    protected EventLoopGroup group;

    private AbstractInvocationHandler handler;

    private String host;

    private int port;

    public NettyClient(String host, int port, AbstractInvocationHandler handler){
        this.host = host;
        this.port = port;
        this.handler = handler;
        try {
            log.info("netty client start... connected host is {} , port is {}", host, port);
            init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() throws InterruptedException {
        b = new Bootstrap();
        group = new NioEventLoopGroup(4);
        b.group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        initClientChannel(ch);
                    }
                });
    }

    public void initClientChannel(SocketChannel ch) {
        ch.pipeline().addLast(new NettyHandler(handler));
    }

    public ChannelFuture connect() {
        ChannelFuture connect = b.connect(host, port);
        try {
            connect.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return connect;
    }

    @Override
    public void close() {
        group.shutdownGracefully();
    }

}
