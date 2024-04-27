package com.gugugu.haochat.common.emoji.controller;

import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.domain.vo.req.IdReqVO;
import com.gugugu.haochat.common.domain.vo.resp.IdRespVO;
import com.gugugu.haochat.common.emoji.domain.vo.req.EmojiReq;
import com.gugugu.haochat.common.emoji.domain.vo.resp.EmojiResp;
import com.gugugu.haochat.common.emoji.service.EmojiService;
import com.gugugu.haochat.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user/emoji")
@Api(tags = "用户表情包管理相关接口")
public class EmojiController {

    @Autowired
    private EmojiService emojiService;

    @GetMapping("/list")
    @ApiOperation("表情包列表")
    public ApiResult<List<EmojiResp>> getEmojisPage(){
        return ApiResult.success(emojiService.list(RequestHolder.get().getUid()));
    }

    @PostMapping
    @ApiOperation("新增表情包")
    public ApiResult<IdRespVO> insertEmojis(@Valid @RequestBody EmojiReq req){
        return emojiService.insert(req, RequestHolder.get().getUid());
    }

    @DeleteMapping()
    @ApiOperation("删除表情包")
    public ApiResult<Void> deleteEmojis(@Valid @RequestBody IdReqVO req){
        emojiService.remove(req.getId(), RequestHolder.get().getUid());
        return ApiResult.success();
    }
}
