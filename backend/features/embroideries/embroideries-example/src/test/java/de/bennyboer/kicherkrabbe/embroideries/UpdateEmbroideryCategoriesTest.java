package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.samples.SampleEmbroidery;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateEmbroideryCategoriesTest extends EmbroideriesModuleTest {

    @Test
    void shouldUpdateEmbroideryCategories() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("FLOWERS_ID", "Flowers");
        markCategoryAsAvailable("SEA_ID", "Sea");

        // and: the user creates an embroidery with a category
        String embroideryId = createEmbroidery(SampleEmbroidery.builder().category("ANIMALS_ID").build(), agent);

        // when: the user updates the categories of the embroidery
        updateEmbroideryCategories(embroideryId, 0L, Set.of("FLOWERS_ID", "SEA_ID"), agent);

        // then: the categories are updated
        var embroidery = getEmbroidery(embroideryId, agent);
        assertThat(embroidery.getVersion()).isEqualTo(Version.of(1));
        assertThat(embroidery.getCategories()).containsExactlyInAnyOrder(
                EmbroideryCategoryId.of("FLOWERS_ID"),
                EmbroideryCategoryId.of("SEA_ID")
        );

        // when: the user removes all categories
        updateEmbroideryCategories(embroideryId, 1L, Set.of(), agent);

        // then: the embroidery has no categories
        assertThat(getEmbroidery(embroideryId, agent).getCategories()).isEmpty();
    }

    @Test
    void shouldNotUpdateEmbroideryCategoriesWhenCategoriesAreMissing() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: a category is available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user tries to update the categories with a missing category; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryCategories(
                embroideryId,
                0L,
                Set.of("ANIMALS_ID", "UNKNOWN_ID"),
                agent
        )).matches(e -> e.getCause() instanceof CategoriesMissingError
                && ((CategoriesMissingError) e.getCause()).getMissingCategories()
                .equals(Set.of(EmbroideryCategoryId.of("UNKNOWN_ID"))));
    }

    @Test
    void shouldNotUpdateEmbroideryCategoriesGivenNoCategories() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user tries to update the categories to null; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryCategories(embroideryId, 0L, null, agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotUpdateEmbroideryCategoriesWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        String embroideryId = createSampleEmbroidery(Agent.user(AgentId.of("OTHER_USER_ID")));

        // when: a user that is not allowed tries to update the categories; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryCategories(
                embroideryId,
                0L,
                Set.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateEmbroideryCategoriesGivenAnOutdatedVersion() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery and updates its categories
        String embroideryId = createSampleEmbroidery(agent);
        updateEmbroideryCategories(embroideryId, 0L, Set.of(), agent);

        // when: the user tries to update the categories with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryCategories(embroideryId, 0L, Set.of(), agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
