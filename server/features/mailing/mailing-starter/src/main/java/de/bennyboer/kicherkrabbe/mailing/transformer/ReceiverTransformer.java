package de.bennyboer.kicherkrabbe.mailing.transformer;

import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.mail.Receiver;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class ReceiverTransformer {

    public static Set<ReceiverDTO> toApi(Collection<Receiver> receivers) {
        return receivers.stream()
                .map(ReceiverTransformer::toApi)
                .collect(Collectors.toSet());
    }

    public static Set<Receiver> toInternal(Set<ReceiverDTO> receivers) {
        notNull(receivers, "Receivers must be given");

        return receivers.stream()
                .map(ReceiverTransformer::toInternal)
                .collect(Collectors.toSet());
    }

    public static ReceiverDTO toApi(Receiver receiver) {
        var result = new ReceiverDTO();

        result.mail = receiver.getMail().getValue();

        return result;
    }

    public static Receiver toInternal(ReceiverDTO receiver) {
        var mail = EMail.of(receiver.mail);

        return Receiver.of(mail);
    }

}
