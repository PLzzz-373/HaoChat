package com.gugugu.haochat.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.gugugu.haochat.websocket.domain.enums.WSReqTypeEnum;
import com.gugugu.haochat.websocket.domain.vo.request.ws.WSBaseReq;
import com.gugugu.haochat.websocket.service.WebSocketService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        webSocketService = SpringUtil.getBean(WebSocketService.class);
        webSocketService.connect(ctx.channel());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.webSocketService = SpringUtil.getBean(WebSocketService.class);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        disconnect(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        disconnect(ctx.channel());
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws  Exception{
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            //保存连接
            webSocketService.connect(ctx.channel());
            //获取http请求头中的token
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            if(StrUtil.isNotBlank(token)){
                //权限验证 握手
                webSocketService.authorize(ctx.channel(),token);
            }
            //读空闲
        }else if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                log.info("长时间无操作，断开连接");
                disconnect(ctx.channel());
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        WSBaseReq wsBaseReq = JSONUtil.toBean(text,WSBaseReq.class);
        switch (WSReqTypeEnum.of(wsBaseReq.getType())){
            case LOGIN:
                //登录
                webSocketService.handleLoginReq(ctx.channel());
                break;
            case AUTHORIZE:
                //授权
                webSocketService.authorize(ctx.channel(), wsBaseReq.getData());
                break;
            case HEARTBEAT:
                //心跳包
                break;
            case LOGOUT:
                System.out.println("退出");
                //退出登录
                webSocketService.logout(ctx.channel());
                break;
            default:
        }


    }

    private void disconnect(Channel channel){
        webSocketService.disconnect(channel);
        channel.close();
    }
}
