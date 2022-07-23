package com.huang.nettyTest.client;


import com.huang.nettyTest.coder.NettyKryoDecoder;
import com.huang.nettyTest.coder.NettyKryoEncoder;
import com.huang.nettyTest.dto.RequestDto;
import com.huang.nettyTest.dto.ResponseDto;
import com.huang.nettyTest.serializer.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static Bootstrap bootstrap;
    private String host;
    private int port;

    public NettyClient(String host,int port){
        this.host = host;
        this.port = port;
    }
    //初始化参数
    static {
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                //日志打印的级别，在info或以上打印出来
                .handler(new LoggingHandler(LogLevel.INFO))
                //设置响应时间，如果超过指定时间，抛出异常
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {

                    protected void initChannel(SocketChannel socketChannel)  {
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, ResponseDto.class));
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RequestDto.class));
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }


    /**
     *
     * @param requestDto 发送的消息
     * @return ResponseDto 从服务端接收的信息
     */
    public ResponseDto sendMessage(RequestDto requestDto){

        try {
            //阻塞连接
            ChannelFuture connect = bootstrap.connect(host, port).sync();
            logger.info("客户端已经连接上了服务器，位置host:{},port:{}",host,port);
            //获取管道
            Channel channel = connect.channel();
            logger.info("开始发送消息");
            //发送消息，并且打印消息发送的是否成功
            if(channel!=null){
                channel.writeAndFlush(requestDto).addListener(future -> {
                    if(future.isSuccess()){
                        logger.info("客户端成功的发送了消息，消息为：{}",requestDto.toString());
                    }else{
                        logger.error("消息发送失败，{}",future.cause());
                    }
                });
            }
            //阻塞关闭
            channel.closeFuture().sync();
            //从Attribute中获取服务端响应的消息
            AttributeKey<ResponseDto> key = AttributeKey.valueOf("responseDto");

            //返回消息
            return channel.attr(key).get();
        } catch (InterruptedException e) {
           logger.error("客户端发生错误",e);
        }

        return null;
    }

    public static void main(String[] args) {
        RequestDto requestDto = RequestDto.builder()
                .interfaceName("loginService")
                .method("login").build();
        NettyClient nettyClient = new NettyClient("127.0.0.1", 8888);
        for (int i = 0; i < 3; i++) {
            nettyClient.sendMessage(requestDto);
        }
        ResponseDto responseDto = nettyClient.sendMessage(requestDto);
        System.out.println(responseDto.toString());
    }
}
