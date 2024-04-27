package com.gugugu.haochat.common.oss;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OssType {
    /**
     * 腾讯 COS
     */
    COS("tencent", 1),

    /**
     * 华为 OBS
     */
    OBS("obs", 2),

    /**
     * 阿里巴巴 SSO
     */
    ALIBABA("alibaba", 3),
    ;

    /**
     * 名称
     */
    final String name;
    /**
     * 类型
     */
    final int type;

}
