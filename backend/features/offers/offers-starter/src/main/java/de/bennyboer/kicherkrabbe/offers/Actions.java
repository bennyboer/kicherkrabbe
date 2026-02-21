package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static final Action CREATE = Action.of("CREATE");

    public static final Action READ = Action.of("READ");

    public static final Action READ_PUBLISHED = Action.of("READ_PUBLISHED");

    public static final Action PUBLISH = Action.of("PUBLISH");

    public static final Action UNPUBLISH = Action.of("UNPUBLISH");

    public static final Action RESERVE = Action.of("RESERVE");

    public static final Action UNRESERVE = Action.of("UNRESERVE");

    public static final Action ARCHIVE = Action.of("ARCHIVE");

    public static final Action UPDATE_IMAGES = Action.of("UPDATE_IMAGES");

    public static final Action UPDATE_NOTES = Action.of("UPDATE_NOTES");

    public static final Action UPDATE_PRICE = Action.of("UPDATE_PRICE");

    public static final Action ADD_DISCOUNT = Action.of("ADD_DISCOUNT");

    public static final Action REMOVE_DISCOUNT = Action.of("REMOVE_DISCOUNT");

    public static final Action UPDATE_TITLE = Action.of("UPDATE_TITLE");

    public static final Action UPDATE_SIZE = Action.of("UPDATE_SIZE");

    public static final Action UPDATE_CATEGORIES = Action.of("UPDATE_CATEGORIES");

    public static final Action DELETE = Action.of("DELETE");

}
