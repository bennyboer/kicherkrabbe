package de.bennyboer.kicherkrabbe.products.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.products.product.Link;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkName;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.bennyboer.kicherkrabbe.products.product.LinkType.FABRIC;
import static de.bennyboer.kicherkrabbe.products.product.LinkType.PATTERN;
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
        // given: a link to update
        var link = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        );

        // when: updating the link
        update(link, Version.of(1));

        // then: the link is updated
        var actualLink = findOne(PATTERN, link.getId());
        assertThat(actualLink).isEqualTo(link);

        // when: finding the link with a wrong type
        actualLink = findOne(LinkType.FABRIC, link.getId());

        // then: the link is not found
        assertThat(actualLink).isNull();
    }

    @Test
    void shouldRemoveLink() {
        // given: some links
        var link1 = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        );
        var link2 = Link.of(
                LinkType.FABRIC,
                LinkId.of("FABRIC_ID"),
                LinkName.of("Fabric")
        );
        update(link1, Version.of(1));
        update(link2, Version.of(1));

        // when: removing a link
        remove(PATTERN, link1.getId());

        // then: the link is removed
        assertThat(findOne(PATTERN, link1.getId())).isNull();
        assertThat(findOne(FABRIC, link2.getId())).isEqualTo(link2);

        // when: removing a link with a wrong type
        remove(PATTERN, link2.getId());

        // then: the link is not removed
        assertThat(findOne(FABRIC, link2.getId())).isEqualTo(link2);
    }

    @Test
    void shouldQueryLinks() {
        // given: some links
        var link1 = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        );
        var link2 = Link.of(
                LinkType.FABRIC,
                LinkId.of("FABRIC_ID"),
                LinkName.of("Fabric")
        );
        update(link1, Version.of(1));
        update(link2, Version.of(1));

        // when: querying all links
        var page = find("", 0, 10);

        // then: all links are found ordered by name
        assertThat(page.getTotal()).isEqualTo(2);
        var linkIds = page.getLinks()
                .stream()
                .map(Link::getId)
                .toList();
        assertThat(linkIds).containsExactly(link2.getId(), link1.getId());

        // when: querying links with paging
        page = find("", 1, 1);

        // then: only the first link is found
        assertThat(page.getTotal()).isEqualTo(2);
        linkIds = page.getLinks()
                .stream()
                .map(Link::getId)
                .toList();
        assertThat(linkIds).containsExactly(link1.getId());

        // when: querying links by search term
        page = find("Fabric", 0, 10);

        // then: only the second link is found
        assertThat(page.getTotal()).isEqualTo(1);
        linkIds = page.getLinks()
                .stream()
                .map(Link::getId)
                .toList();
        assertThat(linkIds).containsExactly(link2.getId());
    }

    @Test
    void shouldRejectStaleWrites() {
        // given: a link with version 5
        var link = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern v5")
        );
        update(link, Version.of(5));

        // when: trying to update with an older version (stale write)
        var staleLink = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern v3 - STALE")
        );
        update(staleLink, Version.of(3));

        // then: the stale write is rejected, original data is preserved
        var actualLink = findOne(PATTERN, link.getId());
        assertThat(actualLink.getName().getValue()).isEqualTo("Pattern v5");

        // when: trying to update with the same version
        var sameVersionLink = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern v5 - SAME VERSION")
        );
        update(sameVersionLink, Version.of(5));

        // then: the same version write is also rejected
        actualLink = findOne(PATTERN, link.getId());
        assertThat(actualLink.getName().getValue()).isEqualTo("Pattern v5");

        // when: updating with a newer version
        var newerLink = Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern v7")
        );
        update(newerLink, Version.of(7));

        // then: the newer version is accepted
        actualLink = findOne(PATTERN, link.getId());
        assertThat(actualLink.getName().getValue()).isEqualTo("Pattern v7");
    }

    private void update(Link link, Version version) {
        repo.update(LookupLink.create(link, version)).block();
    }

    private void remove(LinkType type, LinkId linkId) {
        repo.remove(type, linkId).block();
    }

    private Link findOne(LinkType type, LinkId linkId) {
        return repo.findOne(type, linkId).block();
    }

    private LinkPage find(
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(searchTerm, skip, limit).block();
    }

}
