package com.gugugu.haochat.websocket.domain.vo.response.ws;

import lombok.Data;

@Data
public class WSBaseResp<T> {

    private Integer type;
    private T data;
}

