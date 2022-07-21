package com.huang.nettyTest.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.huang.nettyTest.dto.RequestDto;
import com.huang.nettyTest.dto.ResponseDto;
import com.huang.nettyTest.exception.KryoSerializerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class KryoSerializer implements Serializer{

    /**
     * 因为kryo本身不是线程安全的，所以可以使用ThreadLocal来让每一个调用这个类的方法时产生不同的kryo对象。
    */
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
                    Kryo kryo = new Kryo();
                    kryo.register(RequestDto.class);
                    kryo.register(ResponseDto.class);
                    kryo.setReferences(true);
                    kryo.setRegistrationRequired(false);
                    return kryo;
                });

    /**
     *序列化
     * @param obj 需要进行序列化的对象
     * @return
     */
    public byte[] serializer(Object obj) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
               Output out = new Output(outputStream);
                ){
            //获取kryo对象
            Kryo kryo = kryoThreadLocal.get();
            //将obj序列化写入out对象中
            kryo.writeObject(out,obj);
            //需要进行关闭，否则可能会内存泄漏（删除了key即ThreadLocal，但是里面的value还存在）
            kryoThreadLocal.remove();
            //返回out对象的byte[]
            return out.toBytes();
        }catch (Exception e){
            throw new KryoSerializerException("序列化失败");
        }
    }

    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            Input in = new Input(inputStream);
                ){
            //获取Kryo对象
            Kryo kryo = kryoThreadLocal.get();
            //将读取到的byte数组转换为指定的对象
            T object = kryo.readObject(in, clazz);
            //销毁
            kryoThreadLocal.remove();
            //返回指定的对象格式，cast()强转
            return clazz.cast(object);
        }catch (Exception e){
            throw new KryoSerializerException("反序列化失败");
        }
    }
}
