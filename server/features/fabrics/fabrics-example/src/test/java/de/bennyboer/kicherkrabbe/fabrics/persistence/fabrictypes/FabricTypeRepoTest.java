package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class FabricTypeRepoTest {

    private FabricTypeRepo repo;

    protected abstract FabricTypeRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveFabricType() {
        // given: a fabric type to save
        var fabricType = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID"), FabricTypeName.of("FabricType Name"));

        // when: saving the fabric type
        save(fabricType);

        // then: the fabric type is saved
        var saved = findById(fabricType.getId());
        assertThat(saved).isEqualTo(fabricType);
    }

    @Test
    void shouldFindFabricTypeById() {
        // given: some fabric types
        var fabricType1 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_1"), FabricTypeName.of("FabricType Name 1"));
        var fabricType2 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_2"), FabricTypeName.of("FabricType Name 2"));
        save(fabricType1);
        save(fabricType2);

        // when: finding the first fabric type by id
        var found1 = findById(fabricType1.getId());

        // then: the first fabric type is found
        assertThat(found1).isEqualTo(fabricType1);

        // when: finding the second fabric type by id
        var found2 = findById(fabricType2.getId());

        // then: the second fabric type is found
        assertThat(found2).isEqualTo(fabricType2);
    }

    @Test
    void shouldRemoveFabricTypeById() {
        // given: some fabric types
        var fabricType1 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_1"), FabricTypeName.of("Fabric type Name 1"));
        var fabricType2 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_2"), FabricTypeName.of("Fabric type Name 2"));
        save(fabricType1);
        save(fabricType2);

        // when: removing the first fabric type by id
        removeById(fabricType1.getId());

        // then: the first fabric type is removed
        var found1 = findById(fabricType1.getId());
        assertThat(found1).isNull();

        // and: the second fabric type is still there
        var found2 = findById(fabricType2.getId());
        assertThat(found2).isEqualTo(fabricType2);

        // when: removing the second fabric type by id
        removeById(fabricType2.getId());

        // then: the second fabric type is removed
        var found3 = findById(fabricType2.getId());
        assertThat(found3).isNull();
    }

    @Test
    void shouldFindFabricTypesByIds() {
        // given: some fabric types
        var fabricType1 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_1"), FabricTypeName.of("Fabric type Name 1"));
        var fabricType2 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_2"), FabricTypeName.of("Fabric type Name 2"));
        var fabricType3 = FabricType.of(FabricTypeId.of("FABRIC_TYPE_ID_3"), FabricTypeName.of("Fabric type Name 3"));
        save(fabricType1);
        save(fabricType2);
        save(fabricType3);

        // when: finding the fabric types by ids
        var found = findByIds(List.of(fabricType1.getId(), fabricType3.getId()));

        // then: the fabric types are found
        assertThat(found).containsExactlyInAnyOrder(fabricType1, fabricType3);
    }

    private void save(FabricType fabricType) {
        repo.save(fabricType).block();
    }

    private FabricType findById(FabricTypeId id) {
        return repo.findByIds(List.of(id)).blockFirst();
    }

    private void removeById(FabricTypeId id) {
        repo.removeById(id).block();
    }

    private List<FabricType> findByIds(Collection<FabricTypeId> ids) {
        return repo.findByIds(ids).collectList().block();
    }

}
