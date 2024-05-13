package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.ColorName;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ColorLookupRepoTest {

    private ColorLookupRepo repo;

    protected abstract ColorLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateColor() {
        // given: a color to update
        var color = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the color
        update(color);

        // then: the color is updated
        var colors = find(Set.of(color.getId()));
        assertThat(colors).containsExactly(color);
    }

    @Test
    void shouldRemoveColor() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME1"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME2"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(color1);
        update(color2);

        // when: removing a color
        remove(color1.getId());

        // then: the color is removed
        var colors = find(Set.of(color1.getId(), color2.getId()));
        assertThat(colors).containsExactly(color2);
    }

    @Test
    void shouldFindColors() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME1"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME2"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(color1);
        update(color2);

        // when: finding colors
        var colors = find(Set.of(color1.getId(), color2.getId()));

        // then: the colors are found sorted by creation date
        assertThat(colors).containsExactly(color2, color1);
    }

    @Test
    void shouldFindColorsBySearchTerm() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("Red"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("Green"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var color3 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("Blue"),
                0,
                0,
                255,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(color1);
        update(color2);
        update(color3);

        // when: finding colors by search term
        var colorIds = Set.of(color1.getId(), color2.getId(), color3.getId());
        var colors = find(colorIds, "ee");

        // then: the colors are found by search term
        assertThat(colors).containsExactly(color2);

        // when: finding colors by another search term
        colors = find(colorIds, "bl");

        // then: the colors are found by another search term
        assertThat(colors).containsExactly(color3);

        // when: finding colors by another search term
        colors = find(colorIds, "    ");

        // then: the colors are found by another search term
        assertThat(colors).containsExactly(color2, color3, color1);

        // when: finding colors by another search term
        colors = find(colorIds, "blblblbll");

        // then: the colors are found by another search term
        assertThat(colors).isEmpty();
    }

    @Test
    void shouldFindColorsWithPaging() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME1"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME2"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var color3 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("NAME3"),
                0,
                0,
                255,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(color1);
        update(color2);
        update(color3);

        // when: finding colors with paging
        var colorIds = Set.of(color1.getId(), color2.getId(), color3.getId());
        var colors = find(colorIds, 1, 1);

        // then: the colors are found with paging
        assertThat(colors).containsExactly(color2);

        // when: finding colors with paging
        colors = find(colorIds, 2, 1);

        // then: the colors are found with paging
        assertThat(colors).containsExactly(color1);

        // when: finding colors with paging
        colors = find(colorIds, 3, 1);

        // then: the colors are found with paging
        assertThat(colors).isEmpty();

        // when: finding colors with paging
        colors = find(colorIds, 0, 2);

        // then: the colors are found with paging
        assertThat(colors).containsExactly(color3, color2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("Lightblue"),
                200,
                200,
                255,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("Green"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var color3 = LookupColor.of(
                ColorId.create(),
                Version.zero(),
                ColorName.of("Blue"),
                0,
                0,
                255,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(color1);
        update(color2);
        update(color3);

        // when: finding colors with search term and paging
        var colorIds = Set.of(color1.getId(), color2.getId(), color3.getId());
        var page = findPage(colorIds, "bl", 0, 1);

        // then: the colors are found with search term and paging
        assertThat(page.getResults()).containsExactly(color3);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding colors with search term and paging
        page = findPage(colorIds, "ee", 1, 1);

        // then: the colors are found with search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);

        // when: finding colors with search term and paging
        page = findPage(colorIds, "bl", 0, 5);

        // then: the colors are found with search term and paging
        assertThat(page.getResults()).containsExactly(color3, color1);
        assertThat(page.getTotal()).isEqualTo(2);
    }

    private void update(LookupColor color) {
        repo.update(color).block();
    }

    private void remove(ColorId colorId) {
        repo.remove(colorId).block();
    }

    private List<LookupColor> find(Collection<ColorId> colorIds) {
        return find(colorIds, "", 0, Integer.MAX_VALUE);
    }

    private List<LookupColor> find(Collection<ColorId> colorIds, String searchTerm) {
        return find(colorIds, searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupColor> find(Collection<ColorId> colorIds, long skip, long limit) {
        return find(colorIds, "", skip, limit);
    }

    private List<LookupColor> find(Collection<ColorId> colorIds, String searchTerm, long skip, long limit) {
        return repo.find(colorIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupColorPage findPage(Collection<ColorId> colorIds, String searchTerm, long skip, long limit) {
        return repo.find(colorIds, searchTerm, skip, limit).block();
    }

}
