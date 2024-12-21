package de.bennyboer.kicherkrabbe.mailbox.mail;

public class MailNotReadException extends RuntimeException {

    public MailNotReadException() {
        super("Mail is not read.");
    }

}
