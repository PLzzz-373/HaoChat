package com.gugugu.haochat.user.domain.vo.resp.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BadgeResp {
    @ApiModelProperty(value = "徽章id")
    private Long id;
    @ApiModelProperty(value = "徽章图标")
    private String img;
    @ApiModelProperty(value = "徽章描述")
    private String describe;
    @ApiModelProperty(value = "是否拥有 0否 1是")
    private Integer obtain;
    @ApiModelProperty(value = "是否佩戴 0否 1是")
    private Integer wearing;
}
