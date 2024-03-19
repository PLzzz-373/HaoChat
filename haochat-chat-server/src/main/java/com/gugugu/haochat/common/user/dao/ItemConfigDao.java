package com.gugugu.haochat.common.user.dao;

import com.gugugu.haochat.common.user.domain.entity.ItemConfig;
import com.gugugu.haochat.common.user.mapper.ItemConfigMapper;
import com.gugugu.haochat.common.user.service.IItemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 功能物品配置表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-14
 */
@Service
public class ItemConfigDao extends ServiceImpl<ItemConfigMapper, ItemConfig>  {

    public List<ItemConfig> getByType(Integer itemType) {
        return lambdaQuery()
                .eq(ItemConfig::getType, itemType)
                .list();
    }
}
