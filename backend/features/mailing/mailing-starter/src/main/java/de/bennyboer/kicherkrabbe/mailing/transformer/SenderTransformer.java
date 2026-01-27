package de.bennyboer.kicherkrabbe.mailing.transformer;

import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.mail.Sender;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class SenderTransformer {

    public static SenderDTO toApi(Sender sender) {
        var result = new SenderDTO();

        result.mail = sender.getMail().getValue();

        return result;
    }

    public static Sender toInternal(SenderDTO sender) {
        notNull(sender, "Sender must be given");

        var mail = EMail.of(sender.mail);

        return Sender.of(mail);
    }

}
