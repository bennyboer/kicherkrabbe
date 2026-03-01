package de.bennyboer.kicherkrabbe.patching.persistence.mongo;

import de.bennyboer.kicherkrabbe.patching.InstanceId;
import de.bennyboer.kicherkrabbe.patching.PatchingInProgressException;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MongoTest
public class MongoPatchingMetaRepoTest {

    @Autowired
    ReactiveMongoTemplate template;

    MongoPatchingMetaRepo repo;

    Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    InstanceId instanceA = InstanceId.of("instance-a");
    InstanceId instanceB = InstanceId.of("instance-b");

    Duration lockTimeout = Duration.ofMinutes(5);

    @BeforeEach
    void setUp() {
        template.dropCollection("patching_meta").block();
        repo = new MongoPatchingMetaRepo(template);
    }

    @Test
    void shouldAcquireLockOnFirstAttempt() {
        var meta = repo.tryAcquireLock(instanceA, lockTimeout, clock).block();

        assertThat(meta).isNotNull();
        assertThat(meta.getVersion()).isEqualTo(0);
        assertThat(meta.getLockedBy()).contains(instanceA);
    }

    @Test
    void shouldRaiseErrorWhenLockAlreadyHeld() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();

        assertThatThrownBy(() -> repo.tryAcquireLock(instanceB, lockTimeout, clock).block())
                .isInstanceOf(PatchingInProgressException.class);
    }

    @Test
    void shouldAcquireExpiredLock() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();

        var expiredClock = Clock.fixed(
                Instant.parse("2024-01-01T00:00:00Z").plus(lockTimeout).plusSeconds(1),
                ZoneOffset.UTC
        );

        var meta = repo.tryAcquireLock(instanceB, lockTimeout, expiredClock).block();

        assertThat(meta).isNotNull();
        assertThat(meta.getLockedBy()).contains(instanceB);
    }

    @Test
    void shouldReleaseLock() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();

        repo.releaseLock(instanceA).block();

        var meta = repo.tryAcquireLock(instanceB, lockTimeout, clock).block();
        assertThat(meta).isNotNull();
        assertThat(meta.getLockedBy()).contains(instanceB);
    }

    @Test
    void shouldUpdateVersion() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();

        repo.updateVersion(1, instanceA).block();

        var meta = repo.findMeta().block();
        assertThat(meta).isNotNull();
        assertThat(meta.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldPreserveVersionAcrossLockCycles() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();
        repo.updateVersion(3, instanceA).block();
        repo.releaseLock(instanceA).block();

        var meta = repo.tryAcquireLock(instanceB, lockTimeout, clock).block();

        assertThat(meta).isNotNull();
        assertThat(meta.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldFindMeta() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();
        repo.updateVersion(2, instanceA).block();

        var meta = repo.findMeta().block();

        assertThat(meta).isNotNull();
        assertThat(meta.getVersion()).isEqualTo(2);
        assertThat(meta.getLockedBy()).contains(instanceA);
    }

    @Test
    void shouldFailToUpdateVersionWhenLockNotHeld() {
        repo.tryAcquireLock(instanceA, lockTimeout, clock).block();

        assertThatThrownBy(() -> repo.updateVersion(1, instanceB).block())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to update version - lock not held");
    }

    @Test
    void shouldReturnEmptyWhenNoMetaExists() {
        var meta = repo.findMeta().block();

        assertThat(meta).isNull();
    }

}
