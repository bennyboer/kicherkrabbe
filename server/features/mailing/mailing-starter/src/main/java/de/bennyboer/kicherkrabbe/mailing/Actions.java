package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action READ = Action.of("READ");

    public static final Action UPDATE_MAILGUN_API_TOKEN = Action.of("UPDATE_MAILGUN_API_TOKEN");

    public static final Action CLEAR_MAILGUN_API_TOKEN = Action.of("CLEAR_MAILGUN_API_TOKEN");

    public static final Action SEND_MAILS = Action.of("SEND_MAILS");

}
