package de.bennyboer.kicherkrabbe.mailing.external;

import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import de.bennyboer.kicherkrabbe.mailing.settings.Settings;
import de.bennyboer.kicherkrabbe.mailing.settings.Subject;
import de.bennyboer.kicherkrabbe.mailing.settings.Text;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingMailApi implements MailApi {

    private final List<SentMail> sentMails = new ArrayList<>();

    @Override
    public Mono<Void> sendMail(EMail from, EMail to, Subject subject, Text text, Settings settings) {
        return Mono.fromSupplier(() -> {
            var messageSentViaBot = SentMail.of(from, to, subject, text);
            sentMails.add(messageSentViaBot);
            return null;
        });
    }

    public List<SentMail> getSentMails() {
        return Collections.unmodifiableList(sentMails);
    }

    public void reset() {
        sentMails.clear();
    }

}
