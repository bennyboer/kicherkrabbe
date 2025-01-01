package de.bennyboer.kicherkrabbe.mailing.external;

import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import de.bennyboer.kicherkrabbe.mailing.settings.Settings;
import reactor.core.publisher.Mono;

import java.util.Set;

public class MailHttpApi implements MailApi {

    @Override
    public Mono<Void> sendMail(Sender sender, Set<Receiver> receivers, Subject subject, Text text, Settings settings) {
        return Mono.empty(); // TODO
    }

}
