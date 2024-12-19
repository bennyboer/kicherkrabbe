package de.bennyboer.kicherkrabbe.inquiries.persistence.requests;

import de.bennyboer.kicherkrabbe.inquiries.EMail;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class RequestRepoTest {

    private RequestRepo repo;

    protected abstract RequestRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldInsertRequest() {
        // given: a request to insert
        var request = Request.of(
                RequestId.of("REQUEST_ID"),
                EMail.of("john.doe@kicherkrabbe.com"),
                "192.168.1.1",
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: inserting the request
        insert(request);

        // then: the request is inserted
        var actualRequest = findById(request.getId());
        assertThat(actualRequest).isEqualTo(request);
    }

    @Test
    void shouldCountRecentRequests() {
        // given: some requests
        var request1 = Request.of(
                RequestId.of("REQUEST_ID_1"),
                EMail.of("john.doe@kicherkrabbe.com"),
                null,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var request2 = Request.of(
                RequestId.of("REQUEST_ID_2"),
                EMail.of("jane.doe@kicherkrabbe.com"),
                "192.168.1.1",
                Instant.parse("2024-03-12T13:30:00.00Z")
        );
        var request3 = Request.of(
                RequestId.of("REQUEST_ID_3"),
                EMail.of("max.mustermann@kicherkrabbe.com"),
                "192.168.1.2",
                Instant.parse("2024-03-12T14:30:00.00Z")
        );

        insert(request1);
        insert(request2);
        insert(request3);

        // when: counting recent requests
        var count = countRecent(Instant.parse("2024-03-12T13:00:00.00Z"));

        // then: the count is correct
        assertThat(count).isEqualTo(2);

        // when: counting recent requests with another time
        count = countRecent(Instant.parse("2024-03-12T14:00:00.00Z"));

        // then: the count is correct
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCountRecentRequestsByMail() {
        // given: some requests
        var request1 = Request.of(
                RequestId.of("REQUEST_ID_1"),
                EMail.of("john.doe@kicherkrabbe.com"),
                null,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var request2 = Request.of(
                RequestId.of("REQUEST_ID_2"),
                EMail.of("jane.doe@kicherkrabbe.com"),
                "192.168.1.1",
                Instant.parse("2024-03-12T13:30:00.00Z")
        );
        var request3 = Request.of(
                RequestId.of("REQUEST_ID_3"),
                EMail.of("max.mustermann@kicherkrabbe.com"),
                "192.168.1.2",
                Instant.parse("2024-03-12T14:30:00.00Z")
        );
        var request4 = Request.of(
                RequestId.of("REQUEST_ID_4"),
                EMail.of("max.mustermann@kicherkrabbe.com"),
                "192.168.1.2",
                Instant.parse("2024-03-12T14:45:00.00Z")
        );

        insert(request1);
        insert(request2);
        insert(request3);
        insert(request4);

        // when: counting recent requests by mail
        var count = countRecentByMail(
                EMail.of("max.mustermann@kicherkrabbe.com"),
                Instant.parse("2024-03-12T14:00:00.00Z")
        );

        // then: the count is correct
        assertThat(count).isEqualTo(2);

        // when: counting recent requests by mail with another time
        count = countRecentByMail(
                EMail.of("max.mustermann@kicherkrabbe.com"),
                Instant.parse("2024-03-12T14:30:01.00Z")
        );

        // then: the count is correct
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCountRecentRequestsByIpAddress() {
        // given: some requests
        var request1 = Request.of(
                RequestId.of("REQUEST_ID_1"),
                EMail.of("john.doe@kicherkrabbe.com"),
                null,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var request2 = Request.of(
                RequestId.of("REQUEST_ID_2"),
                EMail.of("jane.doe@kicherkrabbe.com"),
                "192.168.1.1",
                Instant.parse("2024-03-12T13:30:00.00Z")
        );
        var request3 = Request.of(
                RequestId.of("REQUEST_ID_3"),
                EMail.of("max.mustermann@kicherkrabbe.com"),
                "192.168.1.2",
                Instant.parse("2024-03-12T14:30:00.00Z")
        );
        var request4 = Request.of(
                RequestId.of("REQUEST_ID_4"),
                EMail.of("max.mustermann@kicherkrabbe.com"),
                "192.168.1.2",
                Instant.parse("2024-03-12T14:45:00.00Z")
        );

        insert(request1);
        insert(request2);
        insert(request3);
        insert(request4);

        // when: counting recent requests by IP address
        var count = countRecentByIpAddress("192.168.1.2", Instant.parse("2024-03-12T14:00:00.00Z"));

        // then: the count is correct
        assertThat(count).isEqualTo(2);

        // when: counting recent requests by IP address with another time
        count = countRecentByIpAddress("192.168.1.2", Instant.parse("2024-03-12T14:30:01.00Z"));

        // then: the count is correct
        assertThat(count).isEqualTo(1);
    }

    private void insert(Request request) {
        repo.insert(request).block();
    }

    private Request findById(RequestId id) {
        return repo.findById(id).block();
    }

    private long countRecent(Instant since) {
        return repo.countRecent(since).block();
    }

    private long countRecentByMail(EMail mail, Instant since) {
        return repo.countRecentByMail(mail, since).block();
    }

    private long countRecentByIpAddress(String ipAddress, Instant since) {
        return repo.countRecentByIpAddress(ipAddress, since).block();
    }

}
