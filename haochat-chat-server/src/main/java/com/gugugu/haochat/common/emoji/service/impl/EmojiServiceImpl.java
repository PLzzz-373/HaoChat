package com.gugugu.haochat.common.emoji.service.impl;

import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.domain.vo.req.IdReqVO;
import com.gugugu.haochat.common.domain.vo.resp.IdRespVO;
import com.gugugu.haochat.common.emoji.dao.UserEmojiDao;
import com.gugugu.haochat.common.emoji.domain.entity.UserEmoji;
import com.gugugu.haochat.common.emoji.domain.vo.req.EmojiReq;
import com.gugugu.haochat.common.emoji.domain.vo.resp.EmojiResp;
import com.gugugu.haochat.common.emoji.service.EmojiService;
import com.gugugu.haochat.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmojiServiceImpl implements EmojiService {
    @Autowired
    private UserEmojiDao userEmojiDao;
    @Override
    public List<EmojiResp> list(Long uid) {
        return userEmojiDao.listByUid(uid).
                stream()
                .map(a -> EmojiResp.builder()
                        .id(a.getId())
                        .expressionUrl(a.getExpressionUrl())
                        .build())
                .collect(Collectors.toList());

    }

    @Override
    public ApiResult<IdRespVO> insert(EmojiReq req, Long uid) {
        //检测表情数量是否超过50
        int count = userEmojiDao.countByUid(uid);
        AssertUtil.isFalse(count > 30, "最多只能添加50个表情");

        //检验表情是否存在
        Integer existsCount = userEmojiDao.lambdaQuery()
                .eq(UserEmoji::getExpressionUrl, req.getExpressionUrl())
                .eq(UserEmoji::getUid, uid)
                .count();
        AssertUtil.isFalse(existsCount > 0, "当前表情已经存在");
        UserEmoji insert = UserEmoji.builder().uid(uid).expressionUrl(req.getExpressionUrl()).build();
        userEmojiDao.save(insert);
        return ApiResult.success(IdRespVO.id(insert.getId()));
    }

    @Override
    public void remove(Long id, Long uid) {
        UserEmoji userEmoji = userEmojiDao.getById(id);
        AssertUtil.isNotEmpty(userEmoji, "表情不能为空");
        AssertUtil.equal(userEmoji.getUid(), uid, "不能删除别人的表情");
        userEmojiDao.removeById(id);
    }
}
