package de.bennyboer.kicherkrabbe.eventsourcing.example.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EventSourcingReadModelTests {

    private final SampleAggregateReadModelRepo repo = createRepo();

    protected abstract SampleAggregateReadModelRepo createRepo();

    @Test
    void shouldInsertReadModel() {
        // given: a read model to insert
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(1),
                "Title",
                "Description"
        );

        // when: inserting the read model
        repo.update(readModel).block();

        // then: the read model is inserted
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved).isEqualTo(readModel);
    }

    @Test
    void shouldUpdateReadModelWithNewerVersion() {
        // given: an existing read model
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(1),
                "Title",
                "Description"
        );
        repo.update(readModel).block();

        // when: updating the read model with a newer version
        SampleAggregateReadModel updatedReadModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(2),
                "New Title",
                "New Description"
        );
        repo.update(updatedReadModel).block();

        // then: the read model is updated
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved).isEqualTo(updatedReadModel);
    }

    @Test
    void shouldRemoveReadModel() {
        // given: an existing read model
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(1),
                "Title",
                "Description"
        );
        repo.update(readModel).block();

        // when: removing the read model
        repo.remove("MODEL_ID").block();

        // then: the read model is removed
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved).isNull();
    }

    @Test
    void shouldRejectStaleWriteWithOlderVersion() {
        // given: an existing read model at version 5
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(5),
                "Title v5",
                "Description v5"
        );
        repo.update(readModel).block();

        // when: trying to update with an older version (stale write)
        SampleAggregateReadModel staleReadModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(3),
                "Title v3 - STALE",
                "Description v3 - STALE"
        );
        repo.update(staleReadModel).block();

        // then: the stale write is rejected, original data is preserved
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved.getTitle()).isEqualTo("Title v5");
        assertThat(saved.getVersion()).isEqualTo(Version.of(5));
    }

    @Test
    void shouldRejectWriteWithSameVersion() {
        // given: an existing read model at version 5
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(5),
                "Title v5",
                "Description v5"
        );
        repo.update(readModel).block();

        // when: trying to update with the same version
        SampleAggregateReadModel sameVersionReadModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(5),
                "Title v5 - SAME VERSION",
                "Description v5 - SAME VERSION"
        );
        repo.update(sameVersionReadModel).block();

        // then: the same version write is rejected, original data is preserved
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved.getTitle()).isEqualTo("Title v5");
    }

    @Test
    void shouldAcceptWriteWithNewerVersion() {
        // given: an existing read model at version 5
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(5),
                "Title v5",
                "Description v5"
        );
        repo.update(readModel).block();

        // when: updating with a newer version
        SampleAggregateReadModel newerReadModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(7),
                "Title v7",
                "Description v7"
        );
        repo.update(newerReadModel).block();

        // then: the newer version is accepted
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved.getTitle()).isEqualTo("Title v7");
        assertThat(saved.getVersion()).isEqualTo(Version.of(7));
    }

    @Test
    void shouldHandleOutOfOrderUpdatesCorrectly() {
        // This test simulates the race condition where event handlers process events out of order

        // given: no read model exists yet

        // when: event for version 7 arrives and is processed first (due to race condition)
        SampleAggregateReadModel version7 = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(7),
                "Title v7",
                "Description v7"
        );
        repo.update(version7).block();

        // and: event for version 5 arrives and is processed second (stale)
        SampleAggregateReadModel version5 = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(5),
                "Title v5 - STALE",
                "Description v5 - STALE"
        );
        repo.update(version5).block();

        // and: event for version 6 arrives and is processed third (also stale)
        SampleAggregateReadModel version6 = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(6),
                "Title v6 - STALE",
                "Description v6 - STALE"
        );
        repo.update(version6).block();

        // then: only the highest version (7) should be stored
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved.getTitle()).isEqualTo("Title v7");
        assertThat(saved.getVersion()).isEqualTo(Version.of(7));

        // when: a legitimate newer version arrives
        SampleAggregateReadModel version8 = SampleAggregateReadModel.of(
                "MODEL_ID",
                Version.of(8),
                "Title v8",
                "Description v8"
        );
        repo.update(version8).block();

        // then: it should be accepted
        saved = repo.get("MODEL_ID").block();
        assertThat(saved.getTitle()).isEqualTo("Title v8");
        assertThat(saved.getVersion()).isEqualTo(Version.of(8));
    }

}
