package com.gugugu.haochat.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserOfflineEvent extends ApplicationEvent {

    private final Long uid;

    public UserOfflineEvent(Object source, Long uid) {
        super(source);
        this.uid = uid;
    }
}
