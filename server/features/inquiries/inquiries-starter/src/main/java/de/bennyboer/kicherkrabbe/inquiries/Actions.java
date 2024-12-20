package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action SEND = Action.of("SEND");

    public static final Action READ = Action.of("READ");

    public static final Action QUERY_STATUS = Action.of("QUERY_STATUS");

    public static final Action ENABLE_OR_DISABLE_INQUIRIES = Action.of("ENABLE_OR_DISABLE_INQUIRIES");

    public static final Action UPDATE_RATE_LIMITS = Action.of("UPDATE_RATE_LIMITS");

    public static final Action DELETE = Action.of("DELETE");

}
