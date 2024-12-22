package de.bennyboer.kicherkrabbe.mailbox.transformer;

import de.bennyboer.kicherkrabbe.mailbox.api.MailDTO;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMail;

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
        result.origin = OriginTransformer.toApi(mail.getOrigin());
        result.sender = SenderTransformer.toApi(mail.getSender());
        result.subject = mail.getSubject().getValue();
        result.content = mail.getContent().getValue();
        result.receivedAt = mail.getReceivedAt();
        result.readAt = mail.getReadAt().orElse(null);

        return result;
    }

}
