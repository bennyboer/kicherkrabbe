package de.bennyboer.kicherkrabbe.patching;

import de.bennyboer.kicherkrabbe.commons.Preconditions;
import de.bennyboer.kicherkrabbe.patching.persistence.mongo.MongoPatchingMetaRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class PatchingEngine {

    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);
    private static final int MAX_POLL_ATTEMPTS = 150;

    private final List<DatabasePatch> patches;
    private final MongoPatchingMetaRepo metaRepo;
    private final ReactiveMongoTemplate template;
    private final InstanceId instanceId;
    private final Clock clock;

    public PatchingEngine(
            List<DatabasePatch> patches,
            MongoPatchingMetaRepo metaRepo,
            ReactiveMongoTemplate template,
            InstanceId instanceId,
            Clock clock
    ) {
        this.metaRepo = metaRepo;
        this.template = template;
        this.instanceId = instanceId;
        this.clock = clock;

        this.patches = patches.stream()
                .sorted(Comparator.comparingInt(DatabasePatch::getVersion))
                .toList();

        validatePatches();
    }

    public Mono<Void> run() {
        if (patches.isEmpty()) {
            log.info("No database patches registered, skipping patching");
            return Mono.empty();
        }

        log.info("Starting database patching with {} registered patches (versions {} to {})",
                patches.size(),
                patches.getFirst().getVersion(),
                patches.getLast().getVersion());

        return tryAcquireAndPatch()
                .onErrorResume(PatchingInProgressException.class, ignored -> waitForOtherInstance());
    }

    private Mono<Void> tryAcquireAndPatch() {
        return metaRepo.tryAcquireLock(instanceId, LOCK_TIMEOUT, clock)
                .flatMap(meta -> applyPendingPatches(meta)
                        .doOnSuccess(ignored -> log.info("Database patching completed successfully"))
                        .then(metaRepo.releaseLock(instanceId))
                        .onErrorResume(e -> metaRepo.releaseLock(instanceId)
                                .onErrorComplete()
                                .then(Mono.error(e))));
    }

    private Mono<Void> applyPendingPatches(PatchingMeta meta) {
        int currentVersion = meta.getVersion();

        var pendingPatches = patches.stream()
                .filter(p -> p.getVersion() > currentVersion)
                .toList();

        if (pendingPatches.isEmpty()) {
            log.info("Database is already at version {}, no patches to apply", currentVersion);
            return Mono.empty();
        }

        log.info("Applying {} pending patches (current version: {})", pendingPatches.size(), currentVersion);
        return Flux.fromIterable(pendingPatches)
                .concatMap(this::applyPatch)
                .then();
    }

    private Mono<Void> applyPatch(DatabasePatch patch) {
        log.info("Applying patch version {}", patch.getVersion());

        return patch.apply(template)
                .then(metaRepo.updateVersion(patch.getVersion(), instanceId))
                .doOnSuccess(v -> log.info("Successfully applied patch version {}", patch.getVersion()));
    }

    private Mono<Void> waitForOtherInstance() {
        log.info("Another instance is running patches, waiting for completion");

        int requiredVersion = patches.getLast().getVersion();

        return pollUntilComplete(requiredVersion, 0);
    }

    private Mono<Void> pollUntilComplete(int requiredVersion, int attempt) {
        if (attempt >= MAX_POLL_ATTEMPTS) {
            return Mono.error(new PatchingTimeoutException());
        }

        return Mono.delay(POLL_INTERVAL)
                .then(metaRepo.findMeta())
                .switchIfEmpty(Mono.fromSupplier(() -> PatchingMeta.of(0, null)))
                .flatMap(meta -> {
                    if (meta.getVersion() >= requiredVersion && meta.getLockedBy().isEmpty()) {
                        log.info("Other instance completed patching, database at version {}", meta.getVersion());
                        return Mono.empty();
                    }

                    if (meta.getLockedBy().isEmpty() && meta.getVersion() < requiredVersion) {
                        log.info("Lock released but version {} < required {}, attempting to acquire lock",
                                meta.getVersion(), requiredVersion);
                        return tryAcquireAndPatch()
                                .onErrorResume(PatchingInProgressException.class,
                                        ignored -> pollUntilComplete(requiredVersion, attempt + 1));
                    }

                    return pollUntilComplete(requiredVersion, attempt + 1);
                });
    }

    private void validatePatches() {
        Preconditions.check(
                patches.stream().allMatch(p -> p.getVersion() >= 1),
                "All patch versions must be >= 1"
        );

        var versions = patches.stream()
                .map(DatabasePatch::getVersion)
                .toList();

        Preconditions.check(
                IntStream.range(0, versions.size() - 1)
                        .allMatch(i -> versions.get(i) < versions.get(i + 1)),
                "Patch versions must be strictly increasing"
        );
    }

}
