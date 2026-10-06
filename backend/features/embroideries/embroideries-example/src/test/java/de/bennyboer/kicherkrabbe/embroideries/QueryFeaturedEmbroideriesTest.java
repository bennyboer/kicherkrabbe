package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFeaturedEmbroideriesTest extends EmbroideriesModuleTest {

    @Test
    void shouldOnlyReturnPublishedAndFeaturedEmbroideries() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: an embroidery that is published and featured
        String publishedAndFeaturedId = createSampleEmbroidery(agent, "Crab");
        publishEmbroidery(publishedAndFeaturedId, 0L, agent);
        featureEmbroidery(publishedAndFeaturedId, 1L, agent);

        // and: an embroidery that is only published
        String publishedId = createSampleEmbroidery(agent, "Fox");
        publishEmbroidery(publishedId, 0L, agent);

        // and: an embroidery that is only featured
        String featuredId = createSampleEmbroidery(agent, "Rose");
        featureEmbroidery(featuredId, 0L, agent);

        // when: an anonymous user queries the featured embroideries
        var featured = getFeaturedEmbroideries(Agent.anonymous());

        // then: only the published and featured embroidery is returned
        assertThat(featured)
                .extracting(PublishedEmbroidery::getId)
                .containsExactly(EmbroideryId.of(publishedAndFeaturedId));

        // when: the embroidery is unfeatured
        unfeatureEmbroidery(publishedAndFeaturedId, 2L, agent);

        // then: no featured embroideries are returned anymore
        assertThat(getFeaturedEmbroideries(Agent.anonymous())).isEmpty();
    }

}
