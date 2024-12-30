package de.bennyboer.kicherkrabbe.telegram.settings;

import lombok.Getter;

@Getter
public class BotApiTokenMissingException extends RuntimeException {

    public BotApiTokenMissingException() {
        super("Bot API token is missing");
    }

}
