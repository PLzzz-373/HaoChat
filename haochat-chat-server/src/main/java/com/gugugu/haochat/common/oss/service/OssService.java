package com.gugugu.haochat.common.oss.service;

import com.gugugu.haochat.common.oss.domain.OssResp;
import com.gugugu.haochat.common.oss.domain.vo.req.UploadUrlReq;

public interface OssService {
    OssResp getUploadUrl(Long uid, UploadUrlReq req);
}
