package com.gugugu.haochat.common.emoji.domain.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmojiReq {
    @ApiModelProperty(value = "新增的表情url")
    private String expressionUrl;
}
