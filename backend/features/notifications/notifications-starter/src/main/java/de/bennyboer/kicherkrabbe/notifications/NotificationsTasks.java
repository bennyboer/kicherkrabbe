package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationsTasks {

    private final NotificationsModule module;

    private final ReactiveTransactionManager transactionManager;

    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void cleanupOldNotifications() {
        var transactionalOperator = TransactionalOperator.create(transactionManager);

        module.cleanupOldNotifications(Agent.system())
                .count()
                .doOnNext(count -> {
                    if (count > 0) {
                        log.info("Cleaned up {} old notifications", count);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Failed to clean up old notifications", e);
                    return Mono.empty();
                })
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

}

