package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.mailing.mail.*;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMail;
import de.bennyboer.kicherkrabbe.mailing.settings.EMail;

import java.util.Set;
import java.util.stream.Collectors;

public class MongoLookupMailSerializer implements ReadModelSerializer<LookupMail, MongoLookupMail> {

    @Override
    public MongoLookupMail serialize(LookupMail readModel) {
        var result = new MongoLookupMail();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.sender = serializeSender(readModel.getSender());
        result.receivers = serializeReceivers(readModel.getReceivers());
        result.subject = readModel.getSubject().getValue();
        result.text = readModel.getText().getValue();
        result.sentAt = readModel.getSentAt();

        return result;
    }

    @Override
    public LookupMail deserialize(MongoLookupMail serialized) {
        var id = MailId.of(serialized.id);
        var version = Version.of(serialized.version);
        var sender = deserializeSender(serialized.sender);
        var receivers = deserializeReceivers(serialized.receivers);
        var subject = Subject.of(serialized.subject);
        var text = Text.of(serialized.text);
        var sentAt = serialized.sentAt;

        return LookupMail.of(
                id,
                version,
                sender,
                receivers,
                subject,
                text,
                sentAt
        );
    }

    private Set<MongoReceiver> serializeReceivers(Set<Receiver> receivers) {
        return receivers.stream()
                .map(this::serializeReceiver)
                .collect(Collectors.toSet());
    }

    private Set<Receiver> deserializeReceivers(Set<MongoReceiver> serialized) {
        return serialized.stream()
                .map(this::deserializeReceiver)
                .collect(Collectors.toSet());
    }

    private MongoReceiver serializeReceiver(Receiver receiver) {
        var result = new MongoReceiver();

        result.mail = receiver.getMail().getValue();

        return result;
    }

    private Receiver deserializeReceiver(MongoReceiver serialized) {
        var mail = EMail.of(serialized.mail);

        return Receiver.of(mail);
    }

    private MongoSender serializeSender(Sender sender) {
        var result = new MongoSender();

        result.mail = sender.getMail().getValue();

        return result;
    }

    private Sender deserializeSender(MongoSender serialized) {
        var mail = EMail.of(serialized.mail);

        return Sender.of(mail);
    }

}
