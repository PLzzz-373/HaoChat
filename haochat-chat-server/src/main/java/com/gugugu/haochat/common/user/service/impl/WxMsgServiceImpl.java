package com.gugugu.haochat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.gugugu.haochat.common.user.dao.UserDao;
import com.gugugu.haochat.common.user.domain.entity.User;
import com.gugugu.haochat.common.user.service.UserService;
import com.gugugu.haochat.common.user.service.WxMsgService;
import com.gugugu.haochat.common.user.service.adapter.TextBuilder;
import com.gugugu.haochat.common.user.service.adapter.UserAdapter;
import com.gugugu.haochat.common.websocket.service.WebSocketService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
    @Override
    public WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage) {

        String openId = wxMpXmlMessage.getFromUser();
        Integer code = getEventKey(wxMpXmlMessage);
        if(Objects.isNull(code)){
            return null;
        }
        User user = userDao.getByOpenId(openId);
        boolean registered = Objects.nonNull(user);
        boolean authorized = registered && StringUtil.isNotBlank(user.getAvatar());
        if(registered && authorized){
            //todo 登录成功逻辑
            //通过code找到channel，给前端推送消息
            webSocketService.scanLoginSuccess(code,user.getId());


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
