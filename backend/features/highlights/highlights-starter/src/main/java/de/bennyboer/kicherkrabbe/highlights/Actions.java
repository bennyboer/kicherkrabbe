package de.bennyboer.kicherkrabbe.highlights;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action CREATE = Action.of("CREATE");

    public static final Action READ = Action.of("READ");

    public static final Action UPDATE = Action.of("UPDATE");

    public static final Action UPDATE_IMAGE = Action.of("UPDATE_IMAGE");

    public static final Action ADD_LINK = Action.of("ADD_LINK");

    public static final Action REMOVE_LINK = Action.of("REMOVE_LINK");

    public static final Action PUBLISH = Action.of("PUBLISH");

    public static final Action UNPUBLISH = Action.of("UNPUBLISH");

    public static final Action UPDATE_SORT_ORDER = Action.of("UPDATE_SORT_ORDER");

    public static final Action DELETE = Action.of("DELETE");

    public static final Action UPDATE_LINKS = Action.of("UPDATE_LINKS");

}
