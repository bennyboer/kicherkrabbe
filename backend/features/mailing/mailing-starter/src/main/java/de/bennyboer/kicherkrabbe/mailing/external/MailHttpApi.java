package de.bennyboer.kicherkrabbe.mailing.external;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.mail.Subject;
import de.bennyboer.kicherkrabbe.mailing.mail.Text;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunApiTokenMissingException;
import de.bennyboer.kicherkrabbe.mailing.settings.Settings;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class MailHttpApi implements MailApi {

    @Override
    public Mono<Void> sendMail(Sender sender, Set<Receiver> receivers, Subject subject, Text text, Settings settings) {
        if (settings.getMailgun().getApiToken().isEmpty()) {
            return Mono.error(new MailgunApiTokenMissingException());
        }

        String apiToken = settings.getMailgun()
                .getApiToken()
                .orElseThrow()
                .getValue();

        MailgunMessagesApi api = MailgunClient.config("https://api.mailgun.net/", apiToken)
                .createAsyncApi(MailgunMessagesApi.class);

        List<String> receiverMails = receivers.stream()
                .map(receiver -> receiver.getMail().getValue())
                .toList();

        var message = Message.builder()
                .from(sender.getMail().getValue())
                .to(receiverMails)
                .subject(subject.getValue())
                .text(text.getValue())
                .build();

        return Mono.fromFuture(api.sendMessageAsync("sandbox01b5b630b21c405a90559e7de7b756f8.mailgun.org", message))
                .then();
    }

}
