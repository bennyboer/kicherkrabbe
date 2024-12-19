package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.inquiries.*;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.LookupInquiry;

import java.util.Optional;

public class MongoLookupInquirySerializer implements ReadModelSerializer<LookupInquiry, MongoLookupInquiry> {

    @Override
    public MongoLookupInquiry serialize(LookupInquiry readModel) {
        var result = new MongoLookupInquiry();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.requestId = readModel.getRequestId().getValue();
        result.sender = serializeSender(readModel.getSender());
        result.subject = readModel.getSubject().getValue();
        result.message = readModel.getMessage().getValue();
        result.fingerprint = serializeFingerprint(readModel.getFingerprint());
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupInquiry deserialize(MongoLookupInquiry serialized) {
        var id = InquiryId.of(serialized.id);
        var version = Version.of(serialized.version);
        var requestId = RequestId.of(serialized.requestId);
        var sender = deserializeSender(serialized.sender);
        var subject = Subject.of(serialized.subject);
        var message = Message.of(serialized.message);
        var fingerprint = deserializeFingerprint(serialized.fingerprint);
        var createdAt = serialized.createdAt;

        return LookupInquiry.of(
                id,
                version,
                requestId,
                sender,
                subject,
                message,
                fingerprint,
                createdAt
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

    private MongoFingerprint serializeFingerprint(Fingerprint fingerprint) {
        var result = new MongoFingerprint();

        result.ipAddress = fingerprint.getIpAddress().orElse(null);

        return result;
    }

    private Fingerprint deserializeFingerprint(MongoFingerprint serialized) {
        var ipAddress = Optional.ofNullable(serialized.ipAddress).orElse(null);

        return Fingerprint.of(ipAddress);
    }

}
