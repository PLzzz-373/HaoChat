package com.gugugu.haochat.chat.service.strategy.msg;

import com.gugugu.haochat.common.exception.CommonErrorEnum;
import com.gugugu.haochat.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

public class MsgHandlerFactory {
    private static final Map<Integer, AbstractMsgHandler> STRATEGY_MAP = new HashMap<>();

    public static void register(Integer code, AbstractMsgHandler strategy) {
        STRATEGY_MAP.put(code, strategy);
    }

    public static AbstractMsgHandler getStrategyNoNull(Integer code) {
        AbstractMsgHandler strategy = STRATEGY_MAP.get(code);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_VALID);
        return strategy;
    }
}
