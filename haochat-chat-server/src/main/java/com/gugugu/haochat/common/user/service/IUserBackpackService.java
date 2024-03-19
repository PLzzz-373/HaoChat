package com.gugugu.haochat.common.user.service;

import com.gugugu.haochat.common.user.domain.entity.UserBackpack;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gugugu.haochat.common.user.domain.enums.IdempotentEnum;

/**
 * <p>
 * 用户背包表 服务类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-14
 */
public interface IUserBackpackService{
    //发放物品接口
    void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId);
}
