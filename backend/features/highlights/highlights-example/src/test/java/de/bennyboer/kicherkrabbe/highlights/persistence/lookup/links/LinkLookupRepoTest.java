package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.highlights.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class LinkLookupRepoTest {

    private LinkLookupRepo repo;

    protected abstract LinkLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateLink() {
        var link = LookupLink.of(
                "PATTERN-PATTERN_ID",
                Version.zero(),
                LinkType.PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Test Pattern")
        );

        update(link);

        var foundLink = findOne(LinkType.PATTERN, LinkId.of("PATTERN_ID"));
        assertThat(foundLink).isEqualTo(link.toLink());
    }

    @Test
    void shouldRemoveLink() {
        var link1 = LookupLink.of(
                "PATTERN-PATTERN_ID",
                Version.zero(),
                LinkType.PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Test Pattern")
        );
        var link2 = LookupLink.of(
                "FABRIC-FABRIC_ID",
                Version.zero(),
                LinkType.FABRIC,
                LinkId.of("FABRIC_ID"),
                LinkName.of("Test Fabric")
        );
        update(link1);
        update(link2);

        remove(LinkType.PATTERN, LinkId.of("PATTERN_ID"));

        assertThat(findOne(LinkType.PATTERN, LinkId.of("PATTERN_ID"))).isNull();
        assertThat(findOne(LinkType.FABRIC, LinkId.of("FABRIC_ID"))).isEqualTo(link2.toLink());
    }

    @Test
    void shouldFindLinksWithPagingAndSearchTerm() {
        var link1 = LookupLink.of(
                "PATTERN-PATTERN_ID_1",
                Version.zero(),
                LinkType.PATTERN,
                LinkId.of("PATTERN_ID_1"),
                LinkName.of("Alpha Pattern")
        );
        var link2 = LookupLink.of(
                "PATTERN-PATTERN_ID_2",
                Version.zero(),
                LinkType.PATTERN,
                LinkId.of("PATTERN_ID_2"),
                LinkName.of("Beta Pattern")
        );
        var link3 = LookupLink.of(
                "FABRIC-FABRIC_ID",
                Version.zero(),
                LinkType.FABRIC,
                LinkId.of("FABRIC_ID"),
                LinkName.of("Cotton Fabric")
        );
        update(link1);
        update(link2);
        update(link3);

        var page = find("", 0, 10);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getLinks()).hasSize(3);
        assertThat(page.getLinks().get(0).getName().getValue()).isEqualTo("Alpha Pattern");
        assertThat(page.getLinks().get(1).getName().getValue()).isEqualTo("Beta Pattern");
        assertThat(page.getLinks().get(2).getName().getValue()).isEqualTo("Cotton Fabric");

        page = find("Pattern", 0, 10);

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getLinks()).hasSize(2);

        page = find("", 1, 1);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getLinks()).hasSize(1);
        assertThat(page.getLinks().get(0).getName().getValue()).isEqualTo("Beta Pattern");
    }

    @Test
    void shouldFindOneLink() {
        var link = LookupLink.of(
                "PATTERN-PATTERN_ID",
                Version.zero(),
                LinkType.PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Test Pattern")
        );
        update(link);

        var foundLink = findOne(LinkType.PATTERN, LinkId.of("PATTERN_ID"));

        assertThat(foundLink).isEqualTo(link.toLink());

        foundLink = findOne(LinkType.FABRIC, LinkId.of("PATTERN_ID"));

        assertThat(foundLink).isNull();

        foundLink = findOne(LinkType.PATTERN, LinkId.of("NONEXISTENT"));

        assertThat(foundLink).isNull();
    }

    private void update(LookupLink link) {
        repo.update(link).block();
    }

    private void remove(LinkType type, LinkId id) {
        repo.remove(type, id).block();
    }

    private Link findOne(LinkType type, LinkId linkId) {
        return repo.findOne(type, linkId).block();
    }

    private LinkPage find(String searchTerm, long skip, long limit) {
        return repo.find(searchTerm, skip, limit).block();
    }

}
