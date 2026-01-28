package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.mailbox.mail.*;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMail;

import java.util.Optional;

public class MongoLookupMailSerializer implements ReadModelSerializer<LookupMail, MongoLookupMail> {

    @Override
    public MongoLookupMail serialize(LookupMail readModel) {
        var result = new MongoLookupMail();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.origin = serializeOrigin(readModel.getOrigin());
        result.sender = serializeSender(readModel.getSender());
        result.subject = readModel.getSubject().getValue();
        result.content = readModel.getContent().getValue();
        result.receivedAt = readModel.getReceivedAt();
        result.status = serializeStatus(readModel.getStatus());
        result.readAt = readModel.getReadAt().orElse(null);

        return result;
    }

    @Override
    public LookupMail deserialize(MongoLookupMail serialized) {
        var id = MailId.of(serialized.id);
        var version = Version.of(serialized.version);
        var origin = deserializeOrigin(serialized.origin);
        var sender = deserializeSender(serialized.sender);
        var subject = Subject.of(serialized.subject);
        var content = Content.of(serialized.content);
        var receivedAt = serialized.receivedAt;
        var status = deserializeStatus(serialized.status);
        var readAt = Optional.ofNullable(serialized.readAt).orElse(null);

        return LookupMail.of(
                id,
                version,
                origin,
                sender,
                subject,
                content,
                receivedAt,
                status,
                readAt
        );
    }

    private MongoSender serializeSender(Sender sender) {
        var result = new MongoSender();

        result.name = sender.getName().getValue();
        result.mail = sender.getMail().getValue();
        result.phone = sender.getPhone()
                .map(PhoneNumber::getValue)
                .orElse(null);

        return result;
    }

    private Sender deserializeSender(MongoSender serialized) {
        var name = SenderName.of(serialized.name);
        var mail = EMail.of(serialized.mail);
        var phone = Optional.ofNullable(serialized.phone)
                .map(PhoneNumber::of)
                .orElse(null);

        return Sender.of(name, mail, phone);
    }

    private MongoOrigin serializeOrigin(Origin origin) {
        var result = new MongoOrigin();

        result.type = switch (origin.getType()) {
            case INQUIRY -> MongoOriginType.INQUIRY;
        };
        result.id = origin.getId().getValue();

        return result;
    }

    private Origin deserializeOrigin(MongoOrigin serialized) {
        var type = switch (serialized.type) {
            case INQUIRY -> OriginType.INQUIRY;
        };
        var id = OriginId.of(serialized.id);

        return Origin.of(type, id);
    }

    private MongoStatus serializeStatus(Status status) {
        return switch (status) {
            case READ -> MongoStatus.READ;
            case UNREAD -> MongoStatus.UNREAD;
        };
    }

    private Status deserializeStatus(MongoStatus serialized) {
        return switch (serialized) {
            case READ -> Status.READ;
            case UNREAD -> Status.UNREAD;
        };
    }

}
