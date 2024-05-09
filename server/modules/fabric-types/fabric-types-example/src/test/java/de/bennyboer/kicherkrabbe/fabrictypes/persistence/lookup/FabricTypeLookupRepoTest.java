package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class FabricTypeLookupRepoTest {

    private FabricTypeLookupRepo repo;

    protected abstract FabricTypeLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateFabricType() {
        // given: a fabric type to update
        var fabricType = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Jersey"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the fabric type
        update(fabricType);

        // then: the fabric type is updated
        var fabricTypes = find(Set.of(fabricType.getId()));
        assertThat(fabricTypes).containsExactly(fabricType);
    }

    @Test
    void shouldRemoveFabricType() {
        // given: some fabric types
        var fabricType1 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Jersey"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var fabricType2 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("French-Terry"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(fabricType1);
        update(fabricType2);

        // when: removing a fabric type
        remove(fabricType1.getId());

        // then: the fabric type is removed
        var fabricTypes = find(Set.of(fabricType1.getId(), fabricType2.getId()));
        assertThat(fabricTypes).containsExactly(fabricType2);
    }

    @Test
    void shouldFindFabricTypes() {
        // given: some fabric types
        var fabricType1 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Jersey"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabricType2 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("French-Terry"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(fabricType1);
        update(fabricType2);

        // when: finding fabric types
        var fabricTypes = find(Set.of(fabricType1.getId(), fabricType2.getId()));

        // then: the fabric types are found sorted by creation date
        assertThat(fabricTypes).containsExactly(fabricType2, fabricType1);
    }

    @Test
    void shouldFindFabricTypesBySearchTerm() {
        // given: some fabric types
        var fabricType1 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Jersey"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabricType2 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("French-Terry"),
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabricType3 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Silk"),
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabricType1);
        update(fabricType2);
        update(fabricType3);

        // when: finding fabric types by search term
        var fabricTypeIds = Set.of(fabricType1.getId(), fabricType2.getId(), fabricType3.getId());
        var fabricTypes = find(fabricTypeIds, "s");

        // then: the fabric types are found by search term
        assertThat(fabricTypes).containsExactly(fabricType3, fabricType1);

        // when: finding fabric types by another search term
        fabricTypes = find(fabricTypeIds, "te");

        // then: the fabric types are found by another search term
        assertThat(fabricTypes).containsExactly(fabricType2);

        // when: finding fabric types by another search term
        fabricTypes = find(fabricTypeIds, "    ");

        // then: the fabric types are found by another search term
        assertThat(fabricTypes).containsExactly(fabricType2, fabricType3, fabricType1);

        // when: finding fabric types by another search term
        fabricTypes = find(fabricTypeIds, "blblblbll");

        // then: the fabric types are found by another search term
        assertThat(fabricTypes).isEmpty();
    }

    @Test
    void shouldFindFabricTypesWithPaging() {
        // given: some fabric types
        var fabricType1 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Jersey"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabricType2 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("French-Terry"),
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var fabricType3 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Silk"),
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabricType1);
        update(fabricType2);
        update(fabricType3);

        // when: finding fabric types with paging
        var fabricTypeIds = Set.of(fabricType1.getId(), fabricType2.getId(), fabricType3.getId());
        var fabricTypes = find(fabricTypeIds, 1, 1);

        // then: the fabric types are found with paging
        assertThat(fabricTypes).containsExactly(fabricType2);

        // when: finding fabric types with paging
        fabricTypes = find(fabricTypeIds, 2, 1);

        // then: the fabric types are found with paging
        assertThat(fabricTypes).containsExactly(fabricType1);

        // when: finding fabric types with paging
        fabricTypes = find(fabricTypeIds, 3, 1);

        // then: the fabric types are found with paging
        assertThat(fabricTypes).isEmpty();

        // when: finding fabric types with paging
        fabricTypes = find(fabricTypeIds, 0, 2);

        // then: the fabric types are found with paging
        assertThat(fabricTypes).containsExactly(fabricType3, fabricType2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some fabric types
        var fabricType1 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Jersey"),
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabricType2 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("French-Terry"),
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabricType3 = LookupFabricType.of(
                FabricTypeId.create(),
                Version.zero(),
                FabricTypeName.of("Silk"),
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabricType1);
        update(fabricType2);
        update(fabricType3);

        // when: finding fabric types with search term and paging
        var fabricTypeIds = Set.of(fabricType1.getId(), fabricType2.getId(), fabricType3.getId());
        var page = findPage(fabricTypeIds, "s", 0, 1);

        // then: the fabric types are found with search term and paging
        assertThat(page.getResults()).containsExactly(fabricType3);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding fabric types with search term and paging
        page = findPage(fabricTypeIds, "te", 1, 1);

        // then: the fabric types are found with search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    private void update(LookupFabricType fabricType) {
        repo.update(fabricType).block();
    }

    private void remove(FabricTypeId fabricTypeId) {
        repo.remove(fabricTypeId).block();
    }

    private List<LookupFabricType> find(Collection<FabricTypeId> fabricTypeIds) {
        return find(fabricTypeIds, "", 0, Integer.MAX_VALUE);
    }

    private List<LookupFabricType> find(Collection<FabricTypeId> fabricTypeIds, String searchTerm) {
        return find(fabricTypeIds, searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupFabricType> find(Collection<FabricTypeId> fabricTypeIds, long skip, long limit) {
        return find(fabricTypeIds, "", skip, limit);
    }

    private List<LookupFabricType> find(
            Collection<FabricTypeId> fabricTypeIds,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(fabricTypeIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupFabricTypePage findPage(
            Collection<FabricTypeId> fabricTypeIds,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(fabricTypeIds, searchTerm, skip, limit).block();
    }

}
