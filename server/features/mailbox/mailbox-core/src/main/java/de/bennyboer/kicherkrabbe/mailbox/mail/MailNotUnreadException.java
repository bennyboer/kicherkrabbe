package de.bennyboer.kicherkrabbe.mailbox.mail;

public class MailNotUnreadException extends RuntimeException {

    public MailNotUnreadException() {
        super("Mail is not unread.");
    }

}
