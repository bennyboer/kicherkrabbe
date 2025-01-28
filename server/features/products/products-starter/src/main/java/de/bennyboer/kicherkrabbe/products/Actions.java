package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.permissions.Action;

public class Actions {

    public static Action READ = Action.of("READ");

    public static Action CREATE = Action.of("CREATE");

    public static Action UPDATE = Action.of("UPDATE");

    public static Action ADD_LINKS = Action.of("ADD_LINKS");

    public static Action UPDATE_LINKS = Action.of("UPDATE_LINKS");

    public static Action REMOVE_LINKS = Action.of("REMOVE_LINKS");

    public static Action UPDATE_PRODUCED_AT_DATE = Action.of("UPDATE_PRODUCED_AT_DATE");

    public static Action UPDATE_NOTES = Action.of("UPDATE_NOTES");

    public static Action UPDATE_FABRIC_COMPOSITION = Action.of("UPDATE_FABRIC_COMPOSITION");

    public static Action UPDATE_IMAGES = Action.of("UPDATE_IMAGES");

    public static Action DELETE = Action.of("DELETE");

}
