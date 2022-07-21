package com.huang.nettyTest.serializer;

public interface Serializer {

    /**
     * 序列化方法
     * @Param obj 需要进行序列化的对象
     */
    byte[] serializer(Object obj);



    /**
     * 反序列化方法
     * @Param bytes 需要进行反序列化的数据
     * @Param clazz 反序列化后得到的对象类型
     */
    <T> T deserializer(byte[] bytes,Class<T> clazz);
}
