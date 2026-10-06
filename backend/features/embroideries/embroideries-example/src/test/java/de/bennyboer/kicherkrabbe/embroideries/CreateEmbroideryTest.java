package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateEmbroideryTest extends EmbroideriesModuleTest {

    @Test
    void shouldCreateEmbroideryAsUser() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some categories are available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");
        markCategoryAsAvailable("FLOWERS_ID", "Flowers");

        // when: the user creates an embroidery
        String embroideryId = createEmbroidery("Crab", "IMAGE_ID", Set.of("ANIMALS_ID", "FLOWERS_ID"), agent);

        // then: the embroidery is created
        var embroideries = getEmbroideries(agent);
        assertThat(embroideries).hasSize(1);
        var embroidery = embroideries.getFirst();
        assertThat(embroidery.getId()).isEqualTo(EmbroideryId.of(embroideryId));
        assertThat(embroidery.getVersion()).isEqualTo(Version.zero());
        assertThat(embroidery.getName()).isEqualTo(EmbroideryName.of("Crab"));
        assertThat(embroidery.getImage()).isEqualTo(ImageId.of("IMAGE_ID"));
        assertThat(embroidery.getCategories()).containsExactlyInAnyOrder(
                EmbroideryCategoryId.of("ANIMALS_ID"),
                EmbroideryCategoryId.of("FLOWERS_ID")
        );
        assertThat(embroidery.isPublished()).isFalse();
        assertThat(embroidery.isFeatured()).isFalse();
        assertThat(embroidery.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateEmbroideryWithoutCategories() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates an embroidery without categories
        createEmbroidery("Crab", "IMAGE_ID", Set.of(), agent);

        // then: the embroidery is created without categories
        var embroideries = getEmbroideries(agent);
        assertThat(embroideries).hasSize(1);
        assertThat(embroideries.getFirst().getCategories()).isEmpty();
    }

    @Test
    void shouldNotCreateEmbroideryGivenInvalidInput() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user tries to create an embroidery with a blank name; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("", "IMAGE_ID", Set.of(), agent))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create an embroidery without a name; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery(null, "IMAGE_ID", Set.of(), agent))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create an embroidery with a blank image; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("Crab", "", Set.of(), agent))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create an embroidery without an image; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("Crab", null, Set.of(), agent))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create an embroidery without categories; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("Crab", "IMAGE_ID", null, agent))
                .isInstanceOf(IllegalArgumentException.class);

        // then: no embroidery is created
        assertThat(getEmbroideries(agent)).isEmpty();
    }

    @Test
    void shouldNotCreateEmbroideryWhenCategoriesAreMissing() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: only one of the categories is available
        markCategoryAsAvailable("ANIMALS_ID", "Animals");

        // when: the user tries to create an embroidery with a missing category; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("Crab", "IMAGE_ID", Set.of("ANIMALS_ID", "FLOWERS_ID"), agent))
                .matches(e -> e.getCause() instanceof CategoriesMissingError
                        && ((CategoriesMissingError) e.getCause()).getMissingCategories()
                        .equals(Set.of(EmbroideryCategoryId.of("FLOWERS_ID"))));

        // then: no embroidery is created
        assertThat(getEmbroideries(agent)).isEmpty();
    }

    @Test
    void shouldNotCreateEmbroideryWhenUserIsNotAllowed() {
        // when: a user that is not allowed to create embroideries tries to create one; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("Crab", "IMAGE_ID", Set.of(), Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotCreateEmbroideryWhenUserPermissionsHaveBeenRemoved() {
        // given: a user that was allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the permissions of the user have been removed
        removePermissionsForUser("USER_ID");

        // when: the user tries to create an embroidery; then: an error is raised
        assertThatThrownBy(() -> createEmbroidery("Crab", "IMAGE_ID", Set.of(), agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleEmbroideries() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple embroideries
        createSampleEmbroidery(agent, "Crab");
        createSampleEmbroidery(agent, "Fox");
        createSampleEmbroidery(agent, "Rose");

        // then: all embroideries are created
        assertThat(getEmbroideries(agent)).hasSize(3);
    }

    @Test
    void shouldNotCreateEmbroideryWhenAliasIsAlreadyInUse() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: an embroidery is created
        String embroideryId = createSampleEmbroidery(agent, "Little Crab");

        // when: the user tries to create an embroidery whose name results in the same alias; then: an error is raised
        assertThatThrownBy(() -> createSampleEmbroidery(agent, "Little-Crab"))
                .matches(e -> e.getCause() instanceof AliasAlreadyInUseError
                        && ((AliasAlreadyInUseError) e.getCause()).getConflictingEmbroideryId()
                        .equals(EmbroideryId.of(embroideryId))
                        && ((AliasAlreadyInUseError) e.getCause()).getAlias()
                        .equals(EmbroideryAlias.of("little-crab")));
    }

}
