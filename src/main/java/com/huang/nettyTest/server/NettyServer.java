package com.huang.nettyTest.server;

import com.huang.nettyTest.coder.NettyKryoDecoder;
import com.huang.nettyTest.coder.NettyKryoEncoder;
import com.huang.nettyTest.dto.RequestDto;
import com.huang.nettyTest.dto.ResponseDto;
import com.huang.nettyTest.serializer.KryoSerializer;
import com.huang.nettyTest.serializer.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;

    public NettyServer(int port){
        this.port = port;
    }

    private void run(){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        Serializer serializer = new KryoSerializer();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler())
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childOption(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_BACKLOG,128)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new NettyKryoEncoder(serializer, ResponseDto.class));
                        ch.pipeline().addLast(new NettyKryoDecoder(serializer, RequestDto.class));
                        ch.pipeline().addLast(new NettyServerHandler());
                    }
                });
        try {
            //等待客户端进行连接
            ChannelFuture channelFuture = bootstrap.bind(port).sync();

            //等待关闭channel，因为使用已经在handler中做完了
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("服务端错误消息",e);
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer(8888).run();
    }
}
