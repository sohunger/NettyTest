package com.huang.nettyTest.dto;

import lombok.*;

/**
 * 客户端请求实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class RequestDto {
        //接口名称
        private String interfaceName;
        //方法名
        private String method;
}
