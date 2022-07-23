package com.huang.nettyTest.coder;

import com.huang.nettyTest.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 自定义解码器
 */
@AllArgsConstructor
@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private Serializer serializer;
    private Class<?> genericClass;


    private static final int MIN_LEN = 4;

    /**
     *
     * @param channelHandlerContext
     * @param byteBuf  共享缓冲区
     * @param list  需要转换的对象格式吗
     * @throws Exception
     */
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list)  {
        //判断缓冲区中至少要有四个字节才能开始解码
        if(byteBuf.readableBytes()>=MIN_LEN){
            //标记当前解码的位置
            byteBuf.markReaderIndex();
            //从共享缓冲区获取消息的长度
            int length = byteBuf.readInt();
            //进行判断如果缓冲区中的长度小于0，不进行读取
            if(length<0 || byteBuf.readableBytes()<0){
                log.error("可读取的数据错误,缓冲区中的数据长度为负数");
                return;
            }
            if (byteBuf.readableBytes()<length){
                log.error("可读取的长度小于数据长度");
                return;
            }
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);
            Object deserializer = serializer.deserializer(bytes, genericClass);
            list.add(deserializer);
            log.info("解码成功");
        }
    }
}
