package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action CREATE = Action.of("CREATE");

    public static final Action READ = Action.of("READ");

    public static final Action READ_PUBLISHED = Action.of("READ_PUBLISHED");

    public static final Action RENAME = Action.of("RENAME");

    public static final Action PUBLISH = Action.of("PUBLISH");

    public static final Action UNPUBLISH = Action.of("UNPUBLISH");

    public static final Action FEATURE = Action.of("FEATURE");

    public static final Action UNFEATURE = Action.of("UNFEATURE");

    public static final Action UPDATE_IMAGE = Action.of("UPDATE_IMAGE");

    public static final Action UPDATE_CATEGORIES = Action.of("UPDATE_CATEGORIES");

    public static final Action DELETE = Action.of("DELETE");

}
