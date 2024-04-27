package com.gugugu.haochat.common.emoji.domain.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmojiResp {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "表情url")
    private String expressionUrl;
}
