package de.bennyboer.kicherkrabbe.mailing.settings;

import lombok.Getter;

@Getter
public class MailgunApiTokenAlreadyClearedException extends RuntimeException {

    public MailgunApiTokenAlreadyClearedException() {
        super("Mailgun API token already cleared");
    }

}
