package de.bennyboer.kicherkrabbe.fabrics.persistence.colors;

import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ColorRepoTest {

    private ColorRepo repo;

    protected abstract ColorRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveColor() {
        // given: a color to save
        var color = Color.of(ColorId.of("COLOR_ID"), ColorName.of("Color Name"), 255, 0, 0);

        // when: saving the color
        save(color);

        // then: the color is saved
        var saved = findById(color.getId());
        assertThat(saved).isEqualTo(color);
    }

    @Test
    void shouldFindColorById() {
        // given: some colors
        var color1 = Color.of(ColorId.of("COLOR_ID_1"), ColorName.of("Color Name 1"), 255, 0, 0);
        var color2 = Color.of(ColorId.of("COLOR_ID_2"), ColorName.of("Color Name 2"), 0, 255, 0);
        save(color1);
        save(color2);

        // when: finding the first color by id
        var found1 = findById(color1.getId());

        // then: the first color is found
        assertThat(found1).isEqualTo(color1);

        // when: finding the second color by id
        var found2 = findById(color2.getId());

        // then: the second color is found
        assertThat(found2).isEqualTo(color2);
    }

    @Test
    void shouldRemoveColorById() {
        // given: some colors
        var color1 = Color.of(ColorId.of("COLOR_ID_1"), ColorName.of("Color Name 1"), 255, 0, 0);
        var color2 = Color.of(ColorId.of("COLOR_ID_2"), ColorName.of("Color Name 2"), 0, 255, 0);
        save(color1);
        save(color2);

        // when: removing the first color by id
        removeById(color1.getId());

        // then: the first color is removed
        var found1 = findById(color1.getId());
        assertThat(found1).isNull();

        // and: the second color is still there
        var found2 = findById(color2.getId());
        assertThat(found2).isEqualTo(color2);

        // when: removing the second color by id
        removeById(color2.getId());

        // then: the second color is removed
        var found3 = findById(color2.getId());
        assertThat(found3).isNull();
    }

    @Test
    void shouldFindColorsByIds() {
        // given: some colors
        var color1 = Color.of(ColorId.of("COLOR_ID_1"), ColorName.of("Color Name 1"), 255, 0, 0);
        var color2 = Color.of(ColorId.of("COLOR_ID_2"), ColorName.of("Color Name 2"), 0, 255, 0);
        var color3 = Color.of(ColorId.of("COLOR_ID_3"), ColorName.of("Color Name 3"), 0, 0, 255);
        save(color1);
        save(color2);
        save(color3);

        // when: finding the colors by ids
        var found = findByIds(List.of(color1.getId(), color3.getId()));

        // then: the colors are found
        assertThat(found).containsExactlyInAnyOrder(color1, color3);
    }

    private void save(Color color) {
        repo.save(color).block();
    }

    private Color findById(ColorId id) {
        return repo.findByIds(List.of(id)).blockFirst();
    }

    private void removeById(ColorId id) {
        repo.removeById(id).block();
    }

    private List<Color> findByIds(Collection<ColorId> ids) {
        return repo.findByIds(ids).collectList().block();
    }

}
