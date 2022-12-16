package com.buscalibre.app2.events;

import com.buscalibre.app2.models.Country;
import com.buscalibre.app2.models.MessageList;

public class MessageSelectedEvent {

    private final MessageList messageList;
    private final Boolean isReadSelected;

    public MessageSelectedEvent(MessageList messageList, Boolean isReadSelected) {
        this.messageList = messageList;
        this.isReadSelected = isReadSelected;
    }

    public MessageList getMessageList() {
        return messageList;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
