package com.gugugu.haochat.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.service.UserService;
import com.gugugu.haochat.user.service.WxMsgService;
import com.gugugu.haochat.user.service.adapter.TextBuilder;
import com.gugugu.haochat.user.service.adapter.UserAdapter;
import com.gugugu.haochat.websocket.service.WebSocketService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WxMsgServiceImpl implements WxMsgService {
    @Autowired
    private WebSocketService webSocketService;

    private static final ConcurrentHashMap<String, Integer> WAIT_AUTHORIZE_MAP = new ConcurrentHashMap<>();
    @Value("${wx.mp.callback}")
    private String callback;
    private static final String URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
    @Autowired
    @Lazy
    private WxMpService wxMpService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Override
    public WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage) {
        //获取到当前用户的openID
        String openId = wxMpXmlMessage.getFromUser();
        Integer code = getEventKey(wxMpXmlMessage);
        if(Objects.isNull(code)){
            return null;
        }
        //判断用户是否注册过,且授权成功了(名字头像等信息都拿到过了)
        User user = userDao.getByOpenId(openId);
        boolean registered = Objects.nonNull(user);
        boolean authorized = registered && StringUtil.isNotBlank(user.getAvatar());
        if(registered && authorized){
            //通过code找到channel，给前端推送消息
            //直接登录
            webSocketService.scanLoginSuccess(code,user.getId());
            return TextBuilder.build("登录成功，已上线",wxMpXmlMessage);
        }
        if(!registered){
            User insert = UserAdapter.buildUserSave(openId);
            userService.register(insert);
        }
        WAIT_AUTHORIZE_MAP.put(openId,code);
        webSocketService.waitAuthorize(code);
        //推送链接让用户授权
        String authorizeUrl = String.format(URL, wxMpService.getWxMpConfigStorage().getAppId(), URLEncoder.encode(callback + "/wx/portal/public/callBack"));
        log.info(authorizeUrl);
        return TextBuilder.build("请点击授权：<a href=\"" + authorizeUrl + "\">登录</a>", wxMpXmlMessage);


    }
    public WxMpXmlOutMessage subscribe(WxMpService wxMpService, WxMpXmlMessage wxMpXmlMessage) {
        // 获取到当前用户的openid
        String openId = wxMpXmlMessage.getFromUser();
        Integer code = getEventKey(wxMpXmlMessage);
        // 判断用户是否注册过
        User user = userDao.getByOpenId(openId);
        if (!ObjectUtil.isNull(user) && !StrUtil.isBlank(user.getAvatar())) {
            // 登录
            webSocketService.scanLoginSuccess(code, user.getId());
        }
        if (ObjectUtil.isNull(user)) {
            User insert = UserAdapter.buildUserSave(openId);
            // 注册，这时候只保存用户的openid，名字和头像信息都没办法进行保存（因为没权限）
            userService.register(insert);
            //  如果用户没有关注过该公众号，扫码不会触发微信的扫码事件，
            //  我们也就无法存储code -> channel的映射关系，后续的扫码成功事件也就没办法通过code拿到channel，
            //  然后推送给前端进行提示
            // 保存openid和场景code的关系，后续才能通知到前端
            WAIT_AUTHORIZE_MAP.put(openId, code);
            // 授权流程,给用户发送授权消息，并且异步通知前端订阅成功
            threadPoolTaskExecutor.execute(() -> {
                // 判断如果等待登录队列中无该码，则表示已经超时或者已经被移除了
                webSocketService.subscribeSuccess(code);
            });
        }
        String url = String.format(
                URL,
                wxMpService
                        .getWxMpConfigStorage()
                        .getAppId(),
                URLEncoder.encode(callback + "/wx/portal/public/callBack")
        );
        String authorizeUrl = String.format(URL, wxMpService.getWxMpConfigStorage().getAppId(), URLEncoder.encode(callback + "/wx/portal/public/callBack"));
        String format = String.format("请点击授权：<a href=\"" + authorizeUrl + "\">登录</a>", url);
        return TextBuilder.build("感谢关注，" + format, wxMpXmlMessage);
    }
    @Override
    public void authorize(WxOAuth2UserInfo userInfo) {
        String openid = userInfo.getOpenid();
        User user = userDao.getByOpenId(openid);
        //更新用户信息
        if(StrUtil.isBlank(user.getAvatar())){
            fillUserInfo(user.getId(),userInfo);
        }
        //通过code找到用户channel进行登录
        Integer code = WAIT_AUTHORIZE_MAP.remove(openid);
        webSocketService.scanLoginSuccess(code,user.getId());

    }

    private void fillUserInfo(Long uid, WxOAuth2UserInfo userInfo) {
        User user = UserAdapter.buildAuthorizeUser(uid, userInfo);
        userDao.updateById(user);
    }

    private Integer getEventKey(WxMpXmlMessage wxMpXmlMessage) {
        try{
            String eventKey = wxMpXmlMessage.getEventKey();
            String code = eventKey.replace("qrscene_", "");
            return Integer.parseInt(code);
        }catch (Exception e){
            log.error("getEventKey error eventKey:{}",wxMpXmlMessage.getEventKey(),e);
            return null;
        }

    }
}
