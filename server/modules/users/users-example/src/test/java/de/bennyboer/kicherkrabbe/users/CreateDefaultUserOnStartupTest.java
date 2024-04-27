package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateDefaultUserOnStartupTest extends UsersModuleTest {

    @Test
    void shouldCreateDefaultUserOnStartup() throws InterruptedException {
        // when: the application is started
        Thread.sleep(1000);

        // then: an user created event is logged
        List<EventWithMetadata> userCreatedEvents = findEventsByName(EventName.of("CREATED"));
        assertThat(userCreatedEvents).hasSize(1);
        EventWithMetadata userCreatedEvent = userCreatedEvents.get(0);
        String userId = userCreatedEvent.getMetadata().getAggregateId().getValue();

        // and: the user details are correct
        UserDetails userDetails = getUserDetails(userId);
        assertThat(userDetails.getUserId().getValue()).isEqualTo(userId);
        assertThat(userDetails.getName().getFirstName().getValue()).isEqualTo("Default");
        assertThat(userDetails.getName().getLastName().getValue()).isEqualTo("User");
        assertThat(userDetails.getMail().getValue()).isEqualTo("default@kicherkrabbe.com");
    }

}
