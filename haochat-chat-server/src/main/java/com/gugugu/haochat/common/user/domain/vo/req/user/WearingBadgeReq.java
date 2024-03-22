package com.gugugu.haochat.common.user.domain.vo.req.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WearingBadgeReq {
    @ApiModelProperty("徽章Id")
    @NotNull
    private Long itemId;

}
