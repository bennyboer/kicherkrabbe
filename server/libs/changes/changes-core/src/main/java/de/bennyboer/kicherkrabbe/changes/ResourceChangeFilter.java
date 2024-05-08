package de.bennyboer.kicherkrabbe.changes;

public interface ResourceChangeFilter {

    boolean isRelevant(ResourceChange change);

}
