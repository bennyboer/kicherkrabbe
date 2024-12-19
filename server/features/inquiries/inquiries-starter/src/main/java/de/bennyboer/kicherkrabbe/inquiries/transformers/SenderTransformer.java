package de.bennyboer.kicherkrabbe.inquiries.transformers;

import de.bennyboer.kicherkrabbe.inquiries.PhoneNumber;
import de.bennyboer.kicherkrabbe.inquiries.Sender;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;

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

}
