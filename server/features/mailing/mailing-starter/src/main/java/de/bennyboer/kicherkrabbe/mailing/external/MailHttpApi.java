package de.bennyboer.kicherkrabbe.mailing.external;

import de.bennyboer.kicherkrabbe.mailing.settings.EMail;
import de.bennyboer.kicherkrabbe.mailing.settings.Settings;
import de.bennyboer.kicherkrabbe.mailing.settings.Subject;
import de.bennyboer.kicherkrabbe.mailing.settings.Text;
import reactor.core.publisher.Mono;

public class MailHttpApi implements MailApi {

    @Override
    public Mono<Void> sendMail(EMail from, EMail to, Subject subject, Text text, Settings settings) {
        return Mono.empty(); // TODO
    }

}
