package com.gugugu.haochat.common.websocket.domain.vo.request.ws;

import lombok.Data;

@Data
public class WSBaseReq {
    private Integer type;
    private String data;
}
