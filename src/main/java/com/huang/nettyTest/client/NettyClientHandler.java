package com.huang.nettyTest.client;

import com.huang.nettyTest.dto.ResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            ResponseDto responseDto = (ResponseDto) msg;
            //获取键值
            AttributeKey<ResponseDto> key = AttributeKey.valueOf("responseDto");
            //将信息已键值对的方式放入到Attribute中，Attribute是一个共享的信息源。
            ctx.channel().attr(key).set(responseDto);
            //关闭
            ctx.channel().close();
        }finally {
            //ReferenceCountUtil是bytebuf的包装类，每一次bytebuf中减少一个引用就需要调用release方法，增加一个引用就需要调用retain方法
            ReferenceCountUtil.release(msg);
        }

    }

    //这个方法会捕获handler中的异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        logger.info("handler异常",cause);
        ctx.close();
    }
}
