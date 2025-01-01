package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@AllArgsConstructor
public class MailingTasks {

    private final MailingModule module;

    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void cleanupOldMails() {
        module.cleanupOldMails(Agent.system())
                .count()
                .doOnNext(count -> {
                    if (count > 0) {
                        log.info("Cleaned up {} old mails", count);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Failed to clean up old mails", e);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

}

