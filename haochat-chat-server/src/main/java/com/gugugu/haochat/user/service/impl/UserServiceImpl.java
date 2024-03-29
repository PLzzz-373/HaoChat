package com.gugugu.haochat.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.gugugu.haochat.common.event.UserBlackEvent;
import com.gugugu.haochat.common.event.UserRegisterEvent;
import com.gugugu.haochat.user.dao.BlackDao;
import com.gugugu.haochat.user.dao.ItemConfigDao;
import com.gugugu.haochat.user.dao.UserBackpackDao;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.dto.ItemInfoDTO;
import com.gugugu.haochat.user.domain.dto.SummaryInfoDTO;
import com.gugugu.haochat.user.domain.entity.Black;
import com.gugugu.haochat.user.domain.entity.ItemConfig;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.entity.UserBackpack;
import com.gugugu.haochat.user.domain.enums.BlackTypeEnum;
import com.gugugu.haochat.user.domain.enums.ItemEnum;
import com.gugugu.haochat.user.domain.enums.ItemTypeEnum;
import com.gugugu.haochat.user.domain.vo.req.user.BlackReq;
import com.gugugu.haochat.user.domain.vo.req.user.ItemInfoReq;
import com.gugugu.haochat.user.domain.vo.req.user.SummaryInfoReq;
import com.gugugu.haochat.user.domain.vo.resp.user.BadgeResp;
import com.gugugu.haochat.user.domain.vo.resp.user.UserInfoResp;
import com.gugugu.haochat.user.service.UserService;
import com.gugugu.haochat.user.service.adapter.UserAdapter;
import com.gugugu.haochat.user.service.cache.ItemCache;
import com.gugugu.haochat.common.utils.AssertUtil;
import com.gugugu.haochat.user.service.cache.UserCache;
import com.gugugu.haochat.user.service.cache.UserSummaryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    @Autowired
    private UserCache userCache;
    @Autowired
    private UserSummaryCache userSummaryCache;
    @Autowired
    private BlackDao blackDao;
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
            userCache.userInfoChange(uid);
        }

    }

    @Override
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

    @Override
    public List<SummaryInfoDTO> getSummaryUserInfo(SummaryInfoReq req) {
        //与前端同步的uid
        List<Long> uidList =  getNeedSyncUidList(req.getReqList());
        //加载用户信息
        Map<Long, SummaryInfoDTO> batch = userSummaryCache.getBatch(uidList);
        return req.getReqList()
                .stream()
                .map(a->batch.containsKey(a.getUid()) ? batch.get(a.getUid()) : SummaryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId());
            if (Objects.nonNull(a.getLastModifyTime()) && a.getLastModifyTime() >= itemConfig.getUpdateTime().getTime()) {
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void black(BlackReq req) {
        Long uid = req.getUid();
        Black user = new Black();
        user.setTarget(uid.toString());
        user.setType(BlackTypeEnum.UID.getType());
        blackDao.save(user);
        User byId = userDao.getById(uid);
        blackIp(byId.getIpInfo().getCreateIp());
        blackIp(byId.getIpInfo().getUpdateIp());
        applicationEventPublisher.publishEvent(new UserBlackEvent(this, byId));

    }

    private void blackIp(String ip) {

        if(StrUtil.isBlank(ip)){
            return;
        }
        try {
            Black user = new Black();
            user.setTarget(ip);
            user.setType(BlackTypeEnum.IP.getType());
            blackDao.save(user);


        }catch (Exception e){
            log.error("duplicate black ip:{}", ip);
        }
    }

    private List<Long> getNeedSyncUidList(List<SummaryInfoReq.infoReq> reqList) {
        List<Long> needSyncUidList = new ArrayList<>();
        List<Long> userModifyTime = userCache.getUserModifyTime(reqList.stream().map(SummaryInfoReq.infoReq::getUid).collect(Collectors.toList()));
        for (int i = 0; i < reqList.size(); i++) {
            SummaryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            if(Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())){
                needSyncUidList.add(infoReq.getUid());
            }

        }
        return needSyncUidList;
    }


}
