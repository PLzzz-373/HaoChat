package com.gugugu.haochat.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gugugu.haochat.common.domain.enums.HotFlagEnum;
import com.gugugu.haochat.common.domain.enums.RoomTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "room")
@Data
public class Room implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间类型 1群聊 2单聊
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 是否全员展示 0否 1是
     */
    @TableField(value = "hot_flag")
    private Integer hotFlag;

    /**
     * 额外信息（根据不同类型房间有不同存储的东西）
     */
    @TableField(value = "ext_json")
    private Object extJson;

    /**
     * 会话内消息最后更新的时间(只有普通会话需要维护，全员会话不需要维护)
     */
    @TableField(value = "active_time")
    private Date activeTime;

    /**
     * 会话最新消息id
     */
    @TableField(value = "last_msg_id")
    private Long lastMsgId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public boolean isHotRoom() {
        return HotFlagEnum.of(this.hotFlag) == HotFlagEnum.YES;
    }

    @JsonIgnore
    public boolean isRoomFriend() {
        return RoomTypeEnum.of(this.type) == RoomTypeEnum.FRIEND;
    }

    @JsonIgnore
    public boolean isRoomGroup() {
        return RoomTypeEnum.of(this.type) == RoomTypeEnum.GROUP;
    }
}
