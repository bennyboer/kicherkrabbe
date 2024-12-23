package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final CategoryService categoryService = new CategoryService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateCategory() {
        // given: a name and group to create a category for
        var name = CategoryName.of("Top");
        var group = CLOTHING;

        // when: creating a category
        var id = create(name, group);

        // then: the category is created
        var category = get(id);
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getVersion()).isEqualTo(Version.zero());
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getGroup()).isEqualTo(group);
        assertThat(category.isNotDeleted()).isTrue();
    }

    @Test
    void shouldRenameCategory() {
        // given: a category
        var id = create(CategoryName.of("Dress"), CLOTHING);

        // when: renaming the category
        var updatedVersion = rename(id, Version.zero(), CategoryName.of("Skirt"));

        // then: the category is renamed
        var category = get(id);
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getVersion()).isEqualTo(updatedVersion);
        assertThat(category.getName()).isEqualTo(CategoryName.of("Skirt"));
        assertThat(category.getGroup()).isEqualTo(CLOTHING);
        assertThat(category.isNotDeleted()).isTrue();
    }

    @Test
    void shouldNotRenameCategoryGivenAnOutdatedVersion() {
        // given: a category
        var id = create(CategoryName.of("Dress"), CLOTHING);

        // and: the category is renamed
        rename(id, Version.zero(), CategoryName.of("Spring"));

        // when: renaming the category with an outdated version; then: an error is raised
        assertThatThrownBy(() -> rename(id, Version.zero(), CategoryName.of("Skirt")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRegroupCategory() {
        // given: a category
        var id = create(CategoryName.of("Dress"), CLOTHING);

        // when: regrouping the category to the NONE group
        var updatedVersion = regroup(id, Version.zero(), NONE);

        // then: the category is regrouped
        var category = get(id);
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getVersion()).isEqualTo(updatedVersion);
        assertThat(category.getName()).isEqualTo(CategoryName.of("Dress"));
        assertThat(category.getGroup()).isEqualTo(NONE);
        assertThat(category.isNotDeleted()).isTrue();
    }

    @Test
    void shouldNotRegroupCategoryGivenAnOutdatedVersion() {
        // given: a category
        var id = create(CategoryName.of("Dress"), CLOTHING);

        // and: the category is regrouped
        regroup(id, Version.zero(), NONE);

        // when: regrouping the category with an outdated version; then: an error is raised
        assertThatThrownBy(() -> regroup(id, Version.zero(), NONE))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteCategory() {
        // given: a category
        var id = create(CategoryName.of("Trousers"), CLOTHING);

        // when: deleting the category
        delete(id, Version.zero());

        // then: the category is deleted
        assertThat(get(id)).isNull();
    }

    @Test
    void shouldNotDeleteCategoryGivenAnOutdatedVersion() {
        // given: a category
        var id = create(CategoryName.of("Trousers"), CLOTHING);

        // and: the category is renamed
        rename(id, Version.zero(), CategoryName.of("Skirt"));

        // when: deleting the category with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a category
        var id = create(CategoryName.of("Dress"), CLOTHING);

        // when: renaming the category 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = rename(id, version, CategoryName.of("Dress " + i));
        }

        // then: the category is updated
        var category = get(id);
        assertThat(category.getVersion()).isEqualTo(Version.of(202));
        assertThat(category.getName()).isEqualTo(CategoryName.of("Dress 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Category.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    @Test
    void shouldRestoreFromSnapshot() {
        // given: a category
        var id = create(CategoryName.of("Dress"), CLOTHING);

        // when: renaming the category 99 times
        var version = Version.zero();
        for (int i = 0; i < 99; i++) {
            version = rename(id, version, CategoryName.of("Dress " + i));
        }

        // then: a snapshot event is present
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Category.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvent = events.getLast();
        assertThat(snapshotEvent.getMetadata().isSnapshot()).isTrue();
        assertThat(snapshotEvent.getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));

        // when: aggregating the category
        var category = categoryService.get(id).block();

        // then: the category is restored from the snapshot
        assertThat(category.getVersion()).isEqualTo(Version.of(100));
        assertThat(category.getName()).isEqualTo(CategoryName.of("Dress 98"));
        assertThat(category.getGroup()).isEqualTo(CLOTHING);
        assertThat(category.isNotDeleted()).isTrue();
    }

    private Category get(CategoryId id) {
        return categoryService.get(id).block();
    }

    private CategoryId create(CategoryName name, CategoryGroup group) {
        return categoryService.create(name, group, Agent.system()).block().getId();
    }

    private Version rename(CategoryId id, Version version, CategoryName name) {
        return categoryService.rename(id, version, name, Agent.system()).block();
    }

    private Version regroup(CategoryId id, Version version, CategoryGroup group) {
        return categoryService.regroup(id, version, group, Agent.system()).block();
    }

    private void delete(CategoryId id, Version version) {
        categoryService.delete(id, version, Agent.system()).block();
    }

}
