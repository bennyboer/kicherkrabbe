package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action CREATE = Action.of("CREATE");

    public static final Action READ = Action.of("READ");

    public static final Action RENAME = Action.of("RENAME");

    public static final Action PUBLISH = Action.of("PUBLISH");

    public static final Action UNPUBLISH = Action.of("UNPUBLISH");

    public static final Action UPDATE_ATTRIBUTION = Action.of("UPDATE_ATTRIBUTION");

    public static final Action UPDATE_CATEGORIES = Action.of("UPDATE_CATEGORIES");

    public static final Action UPDATE_IMAGES = Action.of("UPDATE_IMAGES");

    public static final Action UPDATE_VARIANTS = Action.of("UPDATE_VARIANTS");

    public static final Action UPDATE_EXTRAS = Action.of("UPDATE_EXTRAS");

    public static final Action DELETE = Action.of("DELETE");

}
