package com.gugugu.haochat.common.user.service.impl;

import com.gugugu.haochat.common.event.UserRegisterEvent;
import com.gugugu.haochat.common.exception.BusinessException;
import com.gugugu.haochat.common.user.dao.ItemConfigDao;
import com.gugugu.haochat.common.user.dao.UserBackpackDao;
import com.gugugu.haochat.common.user.dao.UserDao;
import com.gugugu.haochat.common.user.domain.entity.ItemConfig;
import com.gugugu.haochat.common.user.domain.entity.User;
import com.gugugu.haochat.common.user.domain.entity.UserBackpack;
import com.gugugu.haochat.common.user.domain.enums.ItemEnum;
import com.gugugu.haochat.common.user.domain.enums.ItemTypeEnum;
import com.gugugu.haochat.common.user.domain.vo.resp.BadgeResp;
import com.gugugu.haochat.common.user.domain.vo.resp.UserInfoResp;
import com.gugugu.haochat.common.user.service.UserService;
import com.gugugu.haochat.common.user.service.adapter.UserAdapter;
import com.gugugu.haochat.common.user.service.cache.ItemCache;
import com.gugugu.haochat.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserBackpackDao userBackpackDao;
    @Autowired
    private ItemCache itemCache;
    @Autowired
    private ItemConfigDao itemConfigDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Long register(User insert) {
        userDao.save(insert);
        //用户注册事件
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this,insert));
        return insert.getId();
    }

    @Override
    public UserInfoResp getUserInfo(Long uid) {
        User user = userDao.getById(uid);
        Integer modifyNameCount = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());

        return UserAdapter.buildUserInfo(user, modifyNameCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyName(Long uid, String name) {
        User oldUser = userDao.getByName(name);
        AssertUtil.isEmpty(oldUser, "名字已经被抢占了，请更改用户名");
        UserBackpack modifyNameItem = userBackpackDao.getFirstValidItem(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isEmpty(modifyNameItem, "改名卡不够了");
        //使用改名卡
        boolean success = userBackpackDao.useItem(modifyNameItem);
        if(success){
            //改名
            userDao.modifyName(uid, name);
            //删除缓存
        }

    }

    @Override
    @Cacheable
    public List<BadgeResp> badges(Long uid) {
        //查询所有徽章
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        //查询用户拥有的徽章
        List<Long> collect = itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList());
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uid, collect);
        //用户当前佩戴的徽章
        User user = userDao.getById(uid);

        return UserAdapter.buildBadgeResp(itemConfigs, backpacks, user);
    }

    @Override
    public void wearingBadge(Long uid, Long itemId) {
        //确保有徽章
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid, itemId);
        AssertUtil.isNotEmpty(firstValidItem,"您还没有这个徽章");
        //确保物品类型是徽章
        ItemConfig itemConfig = itemConfigDao.getById(firstValidItem.getItemId());
        AssertUtil.equal(itemConfig.getType(),ItemTypeEnum.BADGE.getType(),"只有徽章才能佩戴");
        userDao.wearingBadge(uid, itemId);
    }


}
