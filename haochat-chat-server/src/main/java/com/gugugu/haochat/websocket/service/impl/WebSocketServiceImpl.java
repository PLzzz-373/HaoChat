package com.gugugu.haochat.websocket.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gugugu.haochat.common.config.ThreadPoolConfig;
import com.gugugu.haochat.common.constant.RedisKey;
import com.gugugu.haochat.common.event.UserOfflineEvent;
import com.gugugu.haochat.common.event.UserOnlineEvent;
import com.gugugu.haochat.common.utils.RedisUtils;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.service.LoginService;
import com.gugugu.haochat.websocket.NettyUtil;
import com.gugugu.haochat.websocket.domain.dto.WSChannelExtraDTO;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBlack;
import com.gugugu.haochat.websocket.service.WebSocketService;
import com.gugugu.haochat.websocket.service.adapter.WebSocketAdapter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    @Autowired
    private LoginService loginService;
    @Autowired
    @Lazy
    private WxMpService wxMpService;
    @Autowired
    private UserDao userDao;
    @Autowired
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    /**
     * 所有以及连接的websocket的channel和额外参数的映射关系
     */
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    /**
     * 所有在线用户和对应channel的映射关系
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();
    public static final Duration EXPIRE_TIME = Duration.ofHours(1);
    private static final String LOGIN_CODE = "loginCode";
    public static final int MAXIMUM_SIZE = 1000;
    private static final Cache<Integer, Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterWrite(EXPIRE_TIME)
            .build();

    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, new WSChannelExtraDTO());
    }

    @SneakyThrows
    @Override
    public void handleLoginReq(Channel channel) {
        //生成随机二维码，并向微信申请带参二维码推送给前端
        Integer code = generateLoginCode(channel);
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, (int) EXPIRE_TIME.getSeconds());

        sendMsg(channel, WebSocketAdapter.buildResp(wxMpQrCodeTicket));
    }



    @Override
    public void scanLoginSuccess(Integer code, Long uid) {
        //确认连接在机器上
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if(Objects.isNull(channel)){
            return;
        }
        User user = userDao.getById(uid);
        //移除code
        WAIT_LOGIN_MAP.invalidate(code);
        //调用登录模块获取token
        String token = loginService.login(uid);
        loginSuccess(channel,user,token);
    }

    @Override
    public void waitAuthorize(Integer code) {
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if(Objects.isNull(channel)){
            return;
        }
        sendMsg(channel,WebSocketAdapter.buildWaitAuthorizeResp());
    }

    @Override
    public void authorize(Channel channel, String token) {
        boolean verifySuccess = loginService.verify(token);
        if(verifySuccess){
            User user = userDao.getById(loginService.getValidUid(token));
            loginSuccess(channel,user,token);

        }else {
            sendMsg(channel,WebSocketAdapter.buildInvalidTokenResp());
        }
    }

    @Override
    public void sendToAllOnline(WSBaseResp<WSBlack> resp, Long skipUid) {
        ONLINE_WS_MAP.forEach(((channel, ext) -> {
            if(Objects.nonNull(skipUid) && Objects.equals(ext.getUid(),skipUid)){
                return  ;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(channel, resp));
        }));
    }

    @Override
    public void disconnect(Channel channel) {
        //删除连接
        ONLINE_WS_MAP.remove(channel);
        //删除用户连接 设置为离线
        Long uid = NettyUtil.getAttr(channel, NettyUtil.UID);
        if(uid == null){
            return;
        }
        offline(channel, uid);

    }

    @Override
    public void subscribeSuccess(Integer code) {
        if (code == null) {
            // 这个地方是防止意外情况的发生：
            //  例如：当用户不是从扫码登录过来的，而是直接通过微信搜索进行了订阅公众号，从而导致报错
            return;
        }
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if (Objects.isNull(channel)) {
            // 超时或已移除 code -> channel ×
            return;
        }
        Long uid = NettyUtil.getAttr(channel, NettyUtil.UID);
        String ip = NettyUtil.getAttr(channel, NettyUtil.IP);
        User user = userDao.getById(uid);
        User update = new User();
        update.setId(user.getId());
        user.refreshIp(ip);
        update.setIpInfo(user.getIpInfo());
        update.setLastOptTime(new Date());
        sendMsgToOne(channel, WebSocketAdapter.buildSubscribeSuccessResp());
    }

    @Override
    public void logout(Channel channel) {
        Long uid = NettyUtil.getAttr(channel, NettyUtil.UID);
        offline(channel, uid);

    }

    private void sendMsgToOne(Channel channel, WSBaseResp<?> wsBaseResp) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(wsBaseResp)));
    }

    private void offline(Channel channel, Long uid) {
        ONLINE_UID_MAP.remove(uid);
        applicationEventPublisher.publishEvent(new UserOfflineEvent(this, uid));
    }

    private void loginSuccess(Channel channel, User user, String token) {
        //更新上线列表
        online(channel, user.getId());
        //推送成功消息
        //保存channel的对应uid
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        wsChannelExtraDTO.setUid(user.getId());
        sendMsg(channel,WebSocketAdapter.buildResp(user,token));
        user.setLastOptTime(new Date());
        user.refreshIp(NettyUtil.getAttr(channel,NettyUtil.IP));
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this,user));
    }

    private void online(Channel channel, Long uid) {
        //先看用户连接有没有建立，没有就建立连接
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.getOrDefault(channel, new WSChannelExtraDTO());
        ONLINE_WS_MAP.putIfAbsent(channel, wsChannelExtraDTO);
        //更新用户在线表
        ONLINE_UID_MAP.putIfAbsent(uid, new CopyOnWriteArrayList<>());
        ONLINE_UID_MAP.get(uid).add(channel);
        NettyUtil.setAttr(channel, NettyUtil.UID, uid);
    }


    private void sendMsg(Channel channel, WSBaseResp<?> resp) {
        channel.writeAndFlush(new TextWebSocketFrame((JSONUtil.toJsonStr(resp))));
    }

    private Integer generateLoginCode(Channel channel) {
        int inc;
        do {
            //本地cache时间必须比redis key过期时间短，否则会出现并发问题
            inc = RedisUtils.integerInc(RedisKey.getKey(LOGIN_CODE), (int) EXPIRE_TIME.toMinutes(), TimeUnit.MINUTES);
        } while (WAIT_LOGIN_MAP.asMap().containsKey(inc));
        //储存一份在本地
        WAIT_LOGIN_MAP.put(inc, channel);
        return inc;
    }
}
