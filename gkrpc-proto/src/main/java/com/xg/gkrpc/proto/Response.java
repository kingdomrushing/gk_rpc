package com.xg.gkrpc.proto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示RPC的返回
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {

    private Integer code = 0;

    private String message = "ok";

    private Object data;
}
