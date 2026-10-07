package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedEmbroideriesTest extends EmbroideriesModuleTest {

    @Test
    void shouldQueryPublishedEmbroideriesAsAnonymousUser() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("FLOWERS_ID", "Flowers");

        // and: some embroideries are created of which some are published
        String crabId = createEmbroidery(SampleEmbroidery.builder().name("Crab").category("ANIMALS_ID").build(), agent);
        createEmbroidery(SampleEmbroidery.builder().name("Fox").category("ANIMALS_ID").build(), agent);
        String roseId = createEmbroidery(SampleEmbroidery.builder().name("Rose").category("FLOWERS_ID").build(), agent);
        publishEmbroidery(crabId, 0L, agent);
        publishEmbroidery(roseId, 0L, agent);

        // when: an anonymous user queries the published embroideries sorted by name ascending
        var page = getPublishedEmbroideries("", Set.of(), true, 0, 10, Agent.anonymous());

        // then: only the published embroideries are returned
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults())
                .extracting(PublishedEmbroidery::getId)
                .containsExactly(EmbroideryId.of(crabId), EmbroideryId.of(roseId));

        // and: the published embroideries contain the public details
        var crab = page.getResults().getFirst();
        assertThat(crab.getName()).isEqualTo(EmbroideryName.of("Crab"));
        assertThat(crab.getAlias()).isEqualTo(EmbroideryAlias.of("crab"));
        assertThat(crab.getImage()).isEqualTo(ImageId.of("IMAGE_ID"));
        assertThat(crab.getCategories()).containsExactly(EmbroideryCategoryId.of("ANIMALS_ID"));

        // when: an anonymous user queries the published embroideries sorted by name descending
        page = getPublishedEmbroideries("", Set.of(), false, 0, 10, Agent.anonymous());

        // then: the published embroideries are returned in reverse order
        assertThat(page.getResults())
                .extracting(PublishedEmbroidery::getId)
                .containsExactly(EmbroideryId.of(roseId), EmbroideryId.of(crabId));

        // when: an anonymous user filters the published embroideries by category
        page = getPublishedEmbroideries("", Set.of("ANIMALS_ID"), true, 0, 10, Agent.anonymous());

        // then: only the published embroideries of that category are returned
        assertThat(page.getResults())
                .extracting(PublishedEmbroidery::getId)
                .containsExactly(EmbroideryId.of(crabId));

        // when: an anonymous user searches the published embroideries
        page = getPublishedEmbroideries("ros", Set.of(), true, 0, 10, Agent.anonymous());

        // then: only the matching published embroidery is returned
        assertThat(page.getResults())
                .extracting(PublishedEmbroidery::getId)
                .containsExactly(EmbroideryId.of(roseId));

        // when: an anonymous user queries the published embroideries with paging
        page = getPublishedEmbroideries("", Set.of(), true, 1, 1, Agent.anonymous());

        // then: the second published embroidery is returned
        assertThat(page.getResults())
                .extracting(PublishedEmbroidery::getId)
                .containsExactly(EmbroideryId.of(roseId));
        assertThat(page.getTotal()).isEqualTo(2);

        // and: the unpublished embroidery is never returned
        assertThat(getPublishedEmbroideries("fox", Set.of(), true, 0, 10, Agent.anonymous()).getResults()).isEmpty();
    }

}
