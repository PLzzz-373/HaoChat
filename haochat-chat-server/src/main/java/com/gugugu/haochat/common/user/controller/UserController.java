package com.gugugu.haochat.common.user.controller;


import com.gugugu.haochat.common.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.user.domain.dto.ItemInfoDTO;
import com.gugugu.haochat.common.user.domain.dto.SummaryInfoDTO;
import com.gugugu.haochat.common.user.domain.vo.req.user.ItemInfoReq;
import com.gugugu.haochat.common.user.domain.vo.req.user.ModifyNameReq;
import com.gugugu.haochat.common.user.domain.vo.req.user.SummaryInfoReq;
import com.gugugu.haochat.common.user.domain.vo.req.user.WearingBadgeReq;
import com.gugugu.haochat.common.user.domain.vo.resp.user.BadgeResp;
import com.gugugu.haochat.common.user.domain.vo.resp.user.UserInfoResp;
import com.gugugu.haochat.common.user.service.UserService;
import com.gugugu.haochat.common.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/user")
@Api(tags = "用户模块接口")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/userInfo")
    @ApiOperation("获取用户个人信息")
    public ApiResult<UserInfoResp>  getUserInfo(){
        return ApiResult.success(userService.getUserInfo(RequestHolder.get().getUid()));
    }
    @PostMapping("/public/summary/userInfo/batch")
    @ApiOperation("用户聚合信息")
    public ApiResult<List<SummaryInfoDTO>> getSummaryUserInfo(@Valid @RequestBody SummaryInfoReq req){
        return ApiResult.success(userService.getSummaryUserInfo(req));
    }
    @PostMapping("/public/badges/batch")
    @ApiOperation("徽章聚合信息")
    public ApiResult<List<ItemInfoDTO>> getItemInfo(@Valid @RequestBody ItemInfoReq req){
        return ApiResult.success(userService.getItemInfo(req));
    }

    @PutMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult<Void> modifyName(@Valid @RequestBody ModifyNameReq req){
        userService.modifyName(RequestHolder.get().getUid(),req.getName());
        return ApiResult.success();

    }

    @GetMapping("/badges")
    @ApiOperation("可选徽章列表预览")
    public ApiResult<List<BadgeResp>> badges(){
        return ApiResult.success(userService.badges(RequestHolder.get().getUid()));
    }

    @PutMapping("/badge")
    @ApiOperation("佩戴徽章")
    public ApiResult<Void> wearingBadge(@Valid @RequestBody WearingBadgeReq wearingBadgeReq){
        userService.wearingBadge(RequestHolder.get().getUid(), wearingBadgeReq.getItemId());
        return ApiResult.success();
    }

}

