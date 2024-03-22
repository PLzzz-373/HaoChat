package com.gugugu.haochat.common.user.service.impl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gugugu.haochat.common.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.user.dao.UserDao;
import com.gugugu.haochat.common.user.domain.entity.IpDetail;
import com.gugugu.haochat.common.user.domain.entity.IpInfo;
import com.gugugu.haochat.common.user.domain.entity.User;
import com.gugugu.haochat.common.user.service.IpService;
import com.gugugu.haochat.common.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.*;

@Service
@Slf4j
public class IpServiceImpl implements IpService , DisposableBean {
    private static ExecutorService executor = new ThreadPoolExecutor(1,1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(500), new NamedThreadFactory("refresh-ipDetail", false));
    @Autowired
    private UserDao userDao;
    @Override
    public void refreshIpDetailAsync(Long uid) {
        executor.execute(()->{
            User user = userDao.getById(uid);
            IpInfo ipInfo = user.getIpInfo();

            if(Objects.isNull(ipInfo)){
                return;
            }
            String ip = ipInfo.needRefreshIp();
            if(StringUtils.isBlank(ip)){
                return;
            }
            IpDetail ipDetail =  tryGetIpDetailOrNullThreeTimes(ip);
            if(Objects.nonNull(ipDetail)){
                ipInfo.refreshIpDetail(ipDetail);
                User update = new User();
                update.setId(uid);
                update.setIpInfo(ipInfo);
                userDao.updateById(update);
            }
        });
    }

    private IpDetail tryGetIpDetailOrNullThreeTimes(String ip) {
        for(int i = 0; i < 3; i ++){
            IpDetail ipDetail =  GetIpDetailOrNull(ip);
            if(Objects.isNull(ipDetail)){
                return ipDetail;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("tryGetIpDetailOrNullThreeTimes InterruptedException",e);
            }
        }
        return null;
    }

    private IpDetail GetIpDetailOrNull(String ip) {
        //"https://ip.taobao.com/outGetIpInfo?ip=" + ip + "&accessKey=alibaba-inc"
        String data = HttpUtil.get("https://ip.taobao.com/outGetIpInfo?ip=" + ip + "&accessKey=alibaba-inc");
        ApiResult<IpDetail> result = JsonUtils.toObj(data, new TypeReference<ApiResult<IpDetail>>() {
        });
        IpDetail detail = result.getData();
        return detail;
    }
    @Override
    public void destroy() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {//最多等30秒，处理不完就拉倒
            if (log.isErrorEnabled()) {
                log.error("Timed out while waiting for executor [{}] to terminate", executor);
            }
        }
    }
}
