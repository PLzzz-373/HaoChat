package com.gugugu.haochat.common.oss.service.impl;

import com.gugugu.haochat.common.oss.CosTemplate;
import com.gugugu.haochat.common.oss.domain.OssReq;
import com.gugugu.haochat.common.oss.domain.OssResp;
import com.gugugu.haochat.common.oss.domain.enums.OssSceneEnum;
import com.gugugu.haochat.common.oss.domain.vo.req.UploadUrlReq;
import com.gugugu.haochat.common.oss.service.OssService;
import com.gugugu.haochat.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OssServiceImpl implements OssService {
    @Autowired
    private CosTemplate cosTemplate;

    @Override
    public OssResp getUploadUrl(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum = OssSceneEnum.of(req.getScene());
        AssertUtil.isNotEmpty(sceneEnum,"场景有误");
        OssReq ossReq = OssReq.builder()
                .fileName(req.getFileName())
                .filePath(sceneEnum.getPath())
                .uid(uid)
                .build();
        return cosTemplate.getPreSignedObjectUrl(ossReq);
    }
}
