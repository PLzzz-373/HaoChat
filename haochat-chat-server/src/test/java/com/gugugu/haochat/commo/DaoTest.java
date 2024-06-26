package com.gugugu.haochat.commo;

import com.gugugu.haochat.common.oss.CosTemplate;
import com.gugugu.haochat.common.oss.domain.OssReq;
import com.gugugu.haochat.common.oss.domain.OssResp;
import com.gugugu.haochat.user.service.LoginService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DaoTest {
    public static final long UID = 11003L;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    public LoginService loginService;
    @Autowired
    private CosTemplate cosTemplate;
    @Test
    public void test() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1, 1000);
        String url = wxMpQrCodeTicket.getUrl();
        System.out.println(url);
    }
    @Test
    public void jwt(){
        String login = loginService.login(UID);
        System.out.println(login);
    }
    @Test
    public void getUploadUrl(){
        OssReq ossReq = OssReq.builder()
                .fileName("test.jpeg")
                .filePath("/test")
                .autoPath(false)
                .build();
        OssResp preSignedObjectUrl = cosTemplate.getPreSignedObjectUrl(ossReq);
        System.out.println(preSignedObjectUrl);
    }
}
