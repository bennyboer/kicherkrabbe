package de.bennyboer.kicherkrabbe.mailing.transformer;

import de.bennyboer.kicherkrabbe.mailing.api.MailDTO;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMail;

import java.util.List;

public class LookupMailTransformer {

    public static List<MailDTO> toApi(List<LookupMail> mails) {
        return mails.stream()
                .map(LookupMailTransformer::toApi)
                .toList();
    }

    public static MailDTO toApi(LookupMail mail) {
        var result = new MailDTO();

        result.id = mail.getId().getValue();
        result.version = mail.getVersion().getValue();
        result.sender = SenderTransformer.toApi(mail.getSender());
        result.receivers = ReceiverTransformer.toApi(mail.getReceivers());
        result.subject = mail.getSubject().getValue();
        result.text = mail.getText().getValue();
        result.sentAt = mail.getSentAt();

        return result;
    }

}
