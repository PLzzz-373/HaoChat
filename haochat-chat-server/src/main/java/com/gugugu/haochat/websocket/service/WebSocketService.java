package com.gugugu.haochat.websocket.service;

import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBlack;
import io.netty.channel.Channel;

public interface WebSocketService {
    void connect(Channel channel);

    void handleLoginReq(Channel channel);


    void scanLoginSuccess(Integer code, Long id);

    void waitAuthorize(Integer code);

    void authorize(Channel channel, String token);

    void sendToAllOnline(WSBaseResp<?> resp, Long uid);

    void disconnect(Channel channel);

    void subscribeSuccess(Integer code);

    void logout(Channel channel);

    void sendToUid(WSBaseResp<?> wsBaseMsg, Long uid);
}
