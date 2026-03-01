package de.bennyboer.kicherkrabbe.patching;

import de.bennyboer.kicherkrabbe.patching.persistence.mongo.MongoPatchingMetaRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MongoTest
public class PatchingEngineTest {

    @Autowired
    ReactiveMongoTemplate template;

    MongoPatchingMetaRepo metaRepo;

    Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    InstanceId instanceId = InstanceId.of("test-instance");

    @BeforeEach
    void setUp() {
        template.dropCollection("patching_meta").block();
        template.dropCollection("test_collection").block();
        metaRepo = new MongoPatchingMetaRepo(template);
    }

    @Test
    void shouldApplyAllPatches() {
        var applied = new ArrayList<Integer>();

        var patches = List.of(
                testPatch(1, () -> applied.add(1)),
                testPatch(2, () -> applied.add(2)),
                testPatch(3, () -> applied.add(3))
        );

        var engine = new PatchingEngine(patches, metaRepo, template, instanceId, clock);
        engine.run().block();

        assertThat(applied).containsExactly(1, 2, 3);

        var meta = metaRepo.findMeta().block();
        assertThat(meta).isNotNull();
        assertThat(meta.getVersion()).isEqualTo(3);
        assertThat(meta.getLockedBy()).isEmpty();
    }

    @Test
    void shouldSkipAlreadyAppliedPatches() {
        var applied = new ArrayList<Integer>();

        var firstRun = List.of(
                testPatch(1, () -> applied.add(1)),
                testPatch(2, () -> applied.add(2))
        );

        new PatchingEngine(firstRun, metaRepo, template, instanceId, clock).run().block();
        applied.clear();

        var secondRun = List.of(
                testPatch(1, () -> applied.add(1)),
                testPatch(2, () -> applied.add(2)),
                testPatch(3, () -> applied.add(3))
        );

        new PatchingEngine(secondRun, metaRepo, template, instanceId, clock).run().block();

        assertThat(applied).containsExactly(3);

        var meta = metaRepo.findMeta().block();
        assertThat(meta.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldDoNothingWhenNoPatchesRegistered() {
        var engine = new PatchingEngine(List.of(), metaRepo, template, instanceId, clock);
        engine.run().block();

        var meta = metaRepo.findMeta().block();
        assertThat(meta).isNull();
    }

    @Test
    void shouldReleaseLockAfterSuccess() {
        var patches = List.of(testPatch(1, () -> {
        }));

        new PatchingEngine(patches, metaRepo, template, instanceId, clock).run().block();

        var meta = metaRepo.findMeta().block();
        assertThat(meta.getLockedBy()).isEmpty();
    }

    @Test
    void shouldReleaseLockAfterFailure() {
        var patches = List.of(
                testPatch(1, () -> {
                    throw new RuntimeException("Patch failed");
                })
        );

        var engine = new PatchingEngine(patches, metaRepo, template, instanceId, clock);

        assertThatThrownBy(() -> engine.run().block())
                .hasMessage("Patch failed");

        var meta = metaRepo.findMeta().block();
        assertThat(meta.getLockedBy()).isEmpty();
    }

    @Test
    void shouldBumpVersionAfterEachPatch() {
        var versionAfterEach = new ArrayList<Integer>();

        var patches = List.of(
                testPatch(1, () -> versionAfterEach.add(metaRepo.findMeta().block().getVersion())),
                testPatch(2, () -> versionAfterEach.add(metaRepo.findMeta().block().getVersion()))
        );

        new PatchingEngine(patches, metaRepo, template, instanceId, clock).run().block();

        assertThat(versionAfterEach).containsExactly(0, 1);
    }

    @Test
    void shouldRejectDuplicateVersions() {
        var patches = List.of(
                testPatch(1, () -> {
                }),
                testPatch(1, () -> {
                })
        );

        assertThatThrownBy(() -> new PatchingEngine(patches, metaRepo, template, instanceId, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patch versions must be strictly increasing");
    }

    @Test
    void shouldRejectVersionZero() {
        var patches = List.of(testPatch(0, () -> {
        }));

        assertThatThrownBy(() -> new PatchingEngine(patches, metaRepo, template, instanceId, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All patch versions must be >= 1");
    }

    @Test
    void shouldSortPatchesByVersion() {
        var applied = new ArrayList<Integer>();

        var patches = List.of(
                testPatch(3, () -> applied.add(3)),
                testPatch(1, () -> applied.add(1)),
                testPatch(2, () -> applied.add(2))
        );

        new PatchingEngine(patches, metaRepo, template, instanceId, clock).run().block();

        assertThat(applied).containsExactly(1, 2, 3);
    }

    @Test
    void shouldApplyPatchThatModifiesDatabase() {
        var collectionCreated = new AtomicBoolean(false);

        var patches = List.<DatabasePatch>of(
                new DatabasePatch() {
                    @Override
                    public int getVersion() {
                        return 1;
                    }

                    @Override
                    public Mono<Void> apply(ReactiveMongoTemplate t) {
                        return t.createCollection("test_collection")
                                .doOnSuccess(ignored -> collectionCreated.set(true))
                                .then();
                    }
                }
        );

        new PatchingEngine(patches, metaRepo, template, instanceId, clock).run().block();

        assertThat(collectionCreated.get()).isTrue();

        var exists = template.collectionExists("test_collection").block();
        assertThat(exists).isTrue();
    }

    private DatabasePatch testPatch(int version, Runnable action) {
        return new DatabasePatch() {
            @Override
            public int getVersion() {
                return version;
            }

            @Override
            public Mono<Void> apply(ReactiveMongoTemplate t) {
                return Mono.fromRunnable(action);
            }
        };
    }

}
