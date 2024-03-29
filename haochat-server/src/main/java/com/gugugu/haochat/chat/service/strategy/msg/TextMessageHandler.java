package com.gugugu.haochat.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.gugugu.haochat.chat.dao.GroupMemberDAO;
import com.gugugu.haochat.chat.dao.MessageDAO;
import com.gugugu.haochat.chat.dao.RoomDAO;
import com.gugugu.haochat.chat.domain.entity.Message;
import com.gugugu.haochat.chat.domain.entity.Room;
import com.gugugu.haochat.chat.domain.vo.req.message.ChatMessageReq;
import com.gugugu.haochat.chat.domain.vo.req.message.MessageExtra;
import com.gugugu.haochat.chat.domain.vo.req.message.body.TextMessageReqBody;
import com.gugugu.haochat.chat.domain.vo.resp.message.ChatMessageResp;
import com.gugugu.haochat.chat.domain.vo.resp.message.body.TextMessageRespBody;
import com.gugugu.haochat.chat.service.RoomService;
import com.gugugu.haochat.common.discover.CommonUrlDiscover;
import com.gugugu.haochat.common.discover.domain.UrlInfo;
import com.gugugu.haochat.common.domain.enums.ChatGroupSpecialMemberEnum;
import com.gugugu.haochat.common.domain.enums.GroupRoleEnum;
import com.gugugu.haochat.common.domain.enums.MessageTypeEnum;
import com.gugugu.haochat.common.domain.enums.error.ChatErrorEnum;
import com.gugugu.haochat.common.utils.AssertUtil;
import com.gugugu.haochat.common.utils.sensitive.SensitiveWordBs;
import com.gugugu.haochat.user.dao.UserDAO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextMessageHandler extends AbstractMessageHandler<TextMessageRespBody> {

    public static final SensitiveWordBs SENSITIVE_WORD_BS = SensitiveWordBs.newInstance();

    @Resource
    private MessageDAO messageDao;

    @Resource
    private UserDAO userDao;

    @Resource
    private RoomService roomService;

    @Resource
    private GroupMemberDAO groupMemberDao;

    @Resource
    private RoomDAO roomDao;

    private static final CommonUrlDiscover URL_TITLE_DISCOVER = new CommonUrlDiscover();

    /**
     * 消息类型
     *
     * @return 消息类型
     */
    @Override
    MessageTypeEnum getMessageTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    /**
     * 校验消息——保存前校验
     *
     * @param chatMessageReq 请求消息体
     * @param uid            发送消息的用户ID
     */
    @Override
    public void checkMessage(ChatMessageReq chatMessageReq, Long uid) {
        // 1. 将消息体转换为文本消息体
        Object body = chatMessageReq.getBody();
        Long roomId = chatMessageReq.getRoomId();
        TextMessageReqBody textMessage = BeanUtil.toBean(body, TextMessageReqBody.class);
        // 1.1 检查敏感词
        String content = textMessage.getContent();
        AssertUtil.isFalse(SENSITIVE_WORD_BS.hasSensitiveWord(content), ChatErrorEnum.SENSITIVE_WORD.getMsg());
        // 2. 检查艾特消息
        List<Long> atUidList = textMessage.getAtUidList();
        boolean isAtUidListNotEmpty = CollectionUtil.isNotEmpty(atUidList);
        if (isAtUidListNotEmpty) {
            // 2.1 判断是否有重复艾特
            HashSet<Long> atUidSet = new HashSet<>(atUidList);
            AssertUtil.equal(atUidList.size(), atUidSet.size(), ChatErrorEnum.AT_USER_REPEAT.getMsg());
            // 2.2 判断艾特用户是否存在
            List<Long> notExistList = atUidList.stream().filter(id -> userDao.getById(id) == null).collect(Collectors.toList());
            AssertUtil.isFalse(notExistList.size() > 0, ChatErrorEnum.AT_USER_NOT_EXIST.getMsg());
            // 2.3 判断艾特的人是否在同一房间内（也就是说判断当前房间内是否存在被艾特的人）
            Long[] atUidArr = ArrayUtil.toArray(atUidList, Long.class);
            Boolean isSameRoom = roomService.checkRoomMembership(roomId, atUidArr);
            AssertUtil.isTrue(isSameRoom, ChatErrorEnum.NOT_IN_GROUP.getMsg());
            // 2.4 判断艾特用户中是否有全体用户
            boolean isContainAll = atUidList.contains(ChatGroupSpecialMemberEnum.ALL.getId());
            Room room = roomDao.getById(roomId);
            if (isContainAll && room.isRoomGroup()) {
                // 2.4.1 有且是群聊，则判断是否有权限
                List<GroupRoleEnum> authorities = new ArrayList<>();
                // 这是群中的两个可以艾特全体人员的权限
                authorities.add(GroupRoleEnum.MASTER);
                authorities.add(GroupRoleEnum.ADMIN);
                Boolean isAuth = groupMemberDao.hasAuthority(roomId, uid, authorities);
                AssertUtil.isTrue(isAuth, ChatErrorEnum.NO_AUTH.getMsg());
            }
            // 2.4.2 没有，则直接跳过
        }
    }

    /**
     * 保存消息
     *
     * @param message        消息
     * @param chatMessageReq 请求消息体
     */
    @Override
    public void saveMessage(Message message, ChatMessageReq chatMessageReq) {
        // 保存消息内容
        Object body = chatMessageReq.getBody();
        TextMessageReqBody textMessageReq = BeanUtil.toBean(body, TextMessageReqBody.class);
        String content = textMessageReq.getContent();
        Long id = message.getId();

        // 1. 额外消息处理
        MessageExtra extra = Optional.ofNullable(message.getExtra()).orElse(new MessageExtra());
        // 1.1 判断消息url跳转
        Map<String, UrlInfo> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(textMessageReq.getContent());
        if (CollectionUtil.isNotEmpty(urlContentMap)) {
            extra.setUrlContentMap(urlContentMap);
        }
        // 1.2 艾特消息保存
        if (CollectionUtil.isNotEmpty(textMessageReq.getAtUidList())) {
            extra.setAtUidList(textMessageReq.getAtUidList());
        }

        Message update = Message.builder()
                .id(id)
                .content(content)
                .extra(extra)
                .build();
        messageDao.updateById(update);
    }

    /**
     * 构建响应消息体
     *
     * @param message 消息对象
     * @param builder 构造器
     * @return 响应消息体
     */
    @Override
    public ChatMessageResp.Message buildChatMessageResp(Message message, ChatMessageResp.Message.MessageBuilder builder) {
        TextMessageRespBody textMessageRespBody = this.buildResponseBody(message);
        return builder.body(textMessageRespBody).build();
    }

    /**
     * 构建消息返回体对象
     *
     * @param message 消息体对象
     * @return 消息体对象
     */
    @Override
    public TextMessageRespBody buildResponseBody(Message message) {
        MessageExtra extra = Optional.ofNullable(message.getExtra()).orElse(new MessageExtra());
        TextMessageRespBody.TextMessageRespBodyBuilder builder = TextMessageRespBody.builder().content(message.getContent());

        // 1. 判断是否有艾特消息
        if (CollectionUtil.isNotEmpty(extra.getAtUidList())) {
            builder.atUidList(extra.getAtUidList());
        }

        // 2. 判断是否有url小卡片消息
        if (CollectionUtil.isNotEmpty(extra.getUrlContentMap())) {
            builder.urlContentMap(extra.getUrlContentMap());
        }

        return builder.build();
    }

    /**
     * 被回复时——展示的消息
     *
     * @param message 消息体
     * @return 被回复时——展示的消息
     */
    @Override
    public String showInReplyMessage(Message message) {
        return message.getContent();
    }

    /**
     * 会话列表——展示的消息
     *
     * @param message 消息体
     * @return 会话列表——展示的消息
     */
    @Override
    public String showInContactMessage(Message message) {
        return message.getContent();
    }
}
