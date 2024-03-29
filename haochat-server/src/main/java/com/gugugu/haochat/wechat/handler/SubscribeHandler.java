package com.gugugu.haochat.wechat.handler;

import com.gugugu.haochat.wechat.service.WxMessageService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Slf4j
public class SubscribeHandler extends AbstractHandler {
    @Resource
    private WxMessageService wxMessageService;

    @Override
    public WxMpXmlOutMessage handle(
            WxMpXmlMessage wxMpXmlMessage,
            Map<String, Object> context,
            WxMpService wxMpService,
            WxSessionManager sessionManager
    ) {
        this.logger.info("新关注用户 OPENID: " + wxMpXmlMessage.getFromUser());
        try {
            // 通知前端订阅成功，即登录成功
            return wxMessageService.subscribe(wxMpService, wxMpXmlMessage);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }

        return null;
    }

}