package de.bennyboer.kicherkrabbe.telegram.settings;

import lombok.Getter;

@Getter
public class BotApiTokenAlreadyClearedException extends RuntimeException {

    public BotApiTokenAlreadyClearedException() {
        super("Bot API token already cleared");
    }

}
