package com.huang.nettyTest.coder;

import com.huang.nettyTest.serializer.KryoSerializer;
import com.huang.nettyTest.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 自定义编码器
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer;
    private final   Class<?> genericClass;
    /**
     *
     * @param channelHandlerContext 管道的上下文
     * @param o  需要转换的对象
     * @param byteBuf  这个是一个共区
     * @throws Exception
     */
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        //判断o是否为genericClass的实例
        if(genericClass.isInstance(o)){
            //进行序列化，转换为字节数组
            byte[] bytes = this.serializer.serializer(o);
            //获取长度
            int length = bytes.length;
            //写入长度
            byteBuf.writeInt(length);
            //写入数据
            byteBuf.writeBytes(bytes);
        }
    }
}
