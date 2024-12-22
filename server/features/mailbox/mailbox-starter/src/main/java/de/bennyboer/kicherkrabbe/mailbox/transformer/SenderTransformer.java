package de.bennyboer.kicherkrabbe.mailbox.transformer;

import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.mail.EMail;
import de.bennyboer.kicherkrabbe.mailbox.mail.PhoneNumber;
import de.bennyboer.kicherkrabbe.mailbox.mail.Sender;
import de.bennyboer.kicherkrabbe.mailbox.mail.SenderName;

import java.util.Optional;

public class SenderTransformer {

    public static SenderDTO toApi(Sender sender) {
        var result = new SenderDTO();

        result.name = sender.getName().getValue();
        result.mail = sender.getMail().getValue();
        result.phone = sender.getPhone()
                .map(PhoneNumber::getValue)
                .orElse(null);

        return result;
    }

    public static Sender toInternal(SenderDTO sender) {
        var name = SenderName.of(sender.name);
        var mail = EMail.of(sender.mail);
        var phone = Optional.ofNullable(sender.phone)
                .map(PhoneNumber::of)
                .orElse(null);

        return Sender.of(name, mail, phone);
    }

}
