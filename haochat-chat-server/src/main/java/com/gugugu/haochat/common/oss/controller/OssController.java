package com.gugugu.haochat.common.oss.controller;

import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.oss.domain.OssReq;
import com.gugugu.haochat.common.oss.domain.OssResp;
import com.gugugu.haochat.common.oss.domain.vo.req.UploadUrlReq;
import com.gugugu.haochat.common.oss.service.OssService;
import com.gugugu.haochat.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/oss")
@Api(tags = "oss相关接口")
public class OssController {
    @Autowired
    private OssService ossService;

    @GetMapping("/upload/url")
    @ApiOperation("获取临时上传链接")
    public ApiResult<OssResp> getUploadUrl(@Valid UploadUrlReq req){
        return ApiResult.success(ossService.getUploadUrl(RequestHolder.get().getUid(), req));
    }
}
