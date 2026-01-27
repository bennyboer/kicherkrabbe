package de.bennyboer.kicherkrabbe.mailing.settings;

import lombok.Getter;

@Getter
public class MailgunApiTokenMissingException extends RuntimeException {

    public MailgunApiTokenMissingException() {
        super("Mailgun API token is missing");
    }

}
