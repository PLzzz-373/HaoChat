package com.gugugu.haochat.common.emoji.service;

import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.domain.vo.req.IdReqVO;
import com.gugugu.haochat.common.domain.vo.resp.IdRespVO;
import com.gugugu.haochat.common.emoji.domain.vo.req.EmojiReq;
import com.gugugu.haochat.common.emoji.domain.vo.resp.EmojiResp;

import java.util.List;

public interface EmojiService {
    List<EmojiResp> list(Long uid);

    ApiResult<IdRespVO> insert(EmojiReq req, Long uid);

    void remove(Long id, Long uid);
}
