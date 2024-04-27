package de.bennyboer.kicherkrabbe.users.internal.create;

public class MailAlreadyInUseError extends Exception {

    String mail;

    public MailAlreadyInUseError(String mail) {
        super("Mail already in use: " + mail);
        this.mail = mail;
    }

}
