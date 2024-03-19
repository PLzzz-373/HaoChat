package com.gugugu.haochat.common.event;

import com.gugugu.haochat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class UserOnlineEvent extends ApplicationEvent {
    private User user;
    public UserOnlineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
