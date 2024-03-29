package com.gugugu.haochat.user.domain.vo.req.friend;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendApplyReq {

    @NotBlank
    @ApiModelProperty("申请信息")
    private String msg;

    @NotNull
    @ApiModelProperty("好友uid")
    private Long targetUid;

}
