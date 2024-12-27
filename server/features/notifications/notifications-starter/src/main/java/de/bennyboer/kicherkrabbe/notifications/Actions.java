package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action READ = Action.of("READ");

    public static final Action SEND = Action.of("SEND");

    public static final Action DELETE = Action.of("DELETE");

    public static final Action ENABLE_SYSTEM_NOTIFICATIONS = Action.of("ENABLE_SYSTEM_NOTIFICATIONS");

    public static final Action DISABLE_SYSTEM_NOTIFICATIONS = Action.of("DISABLE_SYSTEM_NOTIFICATIONS");

    public static final Action UPDATE_SYSTEM_CHANNEL = Action.of("UPDATE_SYSTEM_CHANNEL");

    public static final Action ACTIVATE_SYSTEM_CHANNEL = Action.of("ACTIVATE_SYSTEM_CHANNEL");

    public static final Action DEACTIVATE_SYSTEM_CHANNEL = Action.of("DEACTIVATE_SYSTEM_CHANNEL");

}
