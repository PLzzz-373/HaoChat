package com.gugugu.haochat.common.user.service.cache;

import com.gugugu.haochat.common.common.constant.RedisKey;
import com.gugugu.haochat.common.common.service.cache.AbstractRedisStringCache;
import com.gugugu.haochat.common.user.dao.UserBackpackDao;
import com.gugugu.haochat.common.user.domain.dto.SummaryInfoDTO;
import com.gugugu.haochat.common.user.domain.entity.*;
import com.gugugu.haochat.common.user.domain.enums.ItemTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserSummaryCache extends AbstractRedisStringCache<Long, SummaryInfoDTO> {
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private UserBackpackDao userBackpackDao;
    @Autowired
    private ItemCache itemCache;

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_SUMMARY_STRING, uid);
    }

    @Override
    protected Long getExpireSeconds() {
        return 10 * 60L;
    }

    @Override
    protected Map<Long, SummaryInfoDTO> load(List<Long> uidList) {//后续可优化徽章信息也异步加载
        //用户基本信息
        Map<Long, User> userMap = userInfoCache.getBatch(uidList);
        //用户徽章信息
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        List<Long> itemIds = itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList());
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uidList, itemIds);
        Map<Long, List<UserBackpack>> userBadgeMap = backpacks.stream().collect(Collectors.groupingBy(UserBackpack::getUid));
        //用户最后一次更新时间
        return uidList.stream().map(uid -> {
                    SummaryInfoDTO summeryInfoDTO = new SummaryInfoDTO();
                    User user = userMap.get(uid);
                    if (Objects.isNull(user)) {
                        return null;
                    }
                    List<UserBackpack> userBackpacks = userBadgeMap.getOrDefault(user.getId(), new ArrayList<>());
                    summeryInfoDTO.setUid(user.getId());
                    summeryInfoDTO.setName(user.getName());
                    summeryInfoDTO.setAvatar(user.getAvatar());
                    summeryInfoDTO.setLocPlace(Optional.ofNullable(user.getIpInfo()).map(IpInfo::getUpdateIpDetail).map(IpDetail::getCity).orElse(null));
                    summeryInfoDTO.setWearingItemId(user.getItemId());
                    summeryInfoDTO.setItemIds(userBackpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toList()));
                    return summeryInfoDTO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SummaryInfoDTO::getUid, Function.identity()));
    }
}