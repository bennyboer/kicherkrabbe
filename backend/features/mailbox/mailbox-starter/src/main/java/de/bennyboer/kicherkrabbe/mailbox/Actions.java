package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action RECEIVE = Action.of("RECEIVE");

    public static final Action READ = Action.of("READ");

    public static final Action MANAGE = Action.of("MANAGE");

    public static final Action MARK_AS_READ = Action.of("MARK_AS_READ");

    public static final Action MARK_AS_UNREAD = Action.of("MARK_AS_UNREAD");

    public static final Action DELETE = Action.of("DELETE");

}
