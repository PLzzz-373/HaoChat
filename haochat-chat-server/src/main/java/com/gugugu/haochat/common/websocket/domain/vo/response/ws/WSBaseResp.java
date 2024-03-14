package com.gugugu.haochat.common.websocket.domain.vo.response.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WSBaseResp<T> {

    private Integer type;
    private T data;
}

