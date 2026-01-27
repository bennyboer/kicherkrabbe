package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action CREATE = Action.of("CREATE");
    
    public static final Action READ = Action.of("READ");

    public static final Action READ_PUBLISHED = Action.of("READ_PUBLISHED");

    public static final Action RENAME = Action.of("RENAME");

    public static final Action PUBLISH = Action.of("PUBLISH");

    public static final Action UNPUBLISH = Action.of("UNPUBLISH");

    public static final Action UPDATE_IMAGE = Action.of("UPDATE_IMAGE");

    public static final Action UPDATE_COLORS = Action.of("UPDATE_COLORS");

    public static final Action UPDATE_TOPICS = Action.of("UPDATE_TOPICS");

    public static final Action UPDATE_AVAILABILITY = Action.of("UPDATE_AVAILABILITY");

    public static final Action DELETE = Action.of("DELETE");

}
