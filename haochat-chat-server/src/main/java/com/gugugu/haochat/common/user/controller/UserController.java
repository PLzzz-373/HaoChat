package com.gugugu.haochat.common.user.controller;


import com.gugugu.haochat.common.domain.dto.RequestInfo;
import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.interceptor.TokenInterceptor;
import com.gugugu.haochat.common.user.domain.vo.req.ModifyNameReq;
import com.gugugu.haochat.common.user.domain.vo.resp.UserInfoResp;
import com.gugugu.haochat.common.user.service.UserService;
import com.gugugu.haochat.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


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

    @PutMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult<Void> modifyName(@Valid @RequestBody ModifyNameReq req){
        userService.modifyName(RequestHolder.get().getUid(),req.getName());
        return ApiResult.success();

    }

}

