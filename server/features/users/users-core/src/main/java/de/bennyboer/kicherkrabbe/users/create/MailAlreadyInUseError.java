package de.bennyboer.kicherkrabbe.users.create;

public class MailAlreadyInUseError extends Exception {

    String mail;

    public MailAlreadyInUseError(String mail) {
        super("Mail already in use: " + mail);
        this.mail = mail;
    }

}
