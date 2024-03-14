package com.gugugu.haochat.common.user.domain.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class UserInfoResp {
    @ApiModelProperty(value = "uid")
    private Long id;
    @ApiModelProperty(value = "用户名称")
    private String name;
    @ApiModelProperty(value = "用户头像")
    private String avatar;
    @ApiModelProperty(value = "用户性别")
    private Integer sex;
    @ApiModelProperty(value = "剩余改名次数")
    private Integer modifyNameChance;
}