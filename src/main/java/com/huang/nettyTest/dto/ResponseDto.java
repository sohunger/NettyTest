package com.huang.nettyTest.dto;

import lombok.*;

/**
 * 服务端响应实体类
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ResponseDto {
    //响应消息
    private String message;
}
