package de.bennyboer.kicherkrabbe.mailing.external;

import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import de.bennyboer.kicherkrabbe.mailing.settings.Settings;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LoggingMailApi implements MailApi {

    private final List<SentMail> sentMails = new ArrayList<>();

    @Override
    public Mono<Void> sendMail(Sender sender, Set<Receiver> receivers, Subject subject, Text text, Settings settings) {
        return Mono.fromSupplier(() -> {
            var messageSentViaBot = SentMail.of(sender, receivers, subject, text);
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
