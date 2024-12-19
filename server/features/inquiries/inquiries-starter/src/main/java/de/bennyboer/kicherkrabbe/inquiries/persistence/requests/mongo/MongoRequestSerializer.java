package de.bennyboer.kicherkrabbe.inquiries.persistence.requests.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.inquiries.EMail;
import de.bennyboer.kicherkrabbe.inquiries.InquiryId;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.Request;

public class MongoRequestSerializer implements ReadModelSerializer<Request, MongoRequest> {

    @Override
    public MongoRequest serialize(Request request) {
        var result = new MongoRequest();

        result.id = request.getId().getValue();
        result.mail = request.getMail().getValue();
        result.ipAddress = request.getIpAddress().orElse(null);
        result.createdAt = request.getCreatedAt();

        return result;
    }

    @Override
    public Request deserialize(MongoRequest request) {
        var id = RequestId.of(request.id);
        var mail = EMail.of(request.mail);
        var ipAddress = request.ipAddress;
        var createdAt = request.createdAt;

        return Request.of(
                id,
                mail,
                ipAddress,
                createdAt
        );
    }

}
