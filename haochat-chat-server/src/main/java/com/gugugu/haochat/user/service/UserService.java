package com.gugugu.haochat.user.service;

import com.gugugu.haochat.user.domain.dto.ItemInfoDTO;
import com.gugugu.haochat.user.domain.dto.SummaryInfoDTO;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.vo.req.user.BlackReq;
import com.gugugu.haochat.user.domain.vo.req.user.ItemInfoReq;
import com.gugugu.haochat.user.domain.vo.req.user.SummaryInfoReq;
import com.gugugu.haochat.user.domain.vo.resp.user.BadgeResp;
import com.gugugu.haochat.user.domain.vo.resp.user.UserInfoResp;

import java.util.List;


public interface UserService{

    Long register(User insert);

    UserInfoResp getUserInfo(Long uid);

    void modifyName(Long uid, String name);

    List<BadgeResp> badges(Long uid);

    void wearingBadge(Long uid, Long itemId);

    List<SummaryInfoDTO> getSummaryUserInfo(SummaryInfoReq req);

    List<ItemInfoDTO> getItemInfo(ItemInfoReq req);

    void black(BlackReq req);
}
