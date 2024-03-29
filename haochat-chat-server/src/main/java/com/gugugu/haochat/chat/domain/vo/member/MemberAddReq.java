package com.gugugu.haochat.chat.domain.vo.member;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAddReq {
    @NotNull
    @ApiModelProperty("房间id")
    private Long roomId;

    @NotNull
    @Size(min = 1, max = 50)
    @ApiModelProperty("邀请的uid")
    private List<Long> uidList;
}
