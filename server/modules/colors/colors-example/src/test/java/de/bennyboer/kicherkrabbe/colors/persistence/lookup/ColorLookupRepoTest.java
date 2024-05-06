package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.colors.ColorName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

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
                ColorName.of("NAME"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the color
        update(color);

        // then: the color is updated
        var colors = find();
        assertThat(colors).containsExactly(color);
    }

    @Test
    void shouldRemoveColor() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                ColorName.of("NAME1"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
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
        var colors = find();
        assertThat(colors).containsExactly(color2);
    }

    @Test
    void shouldFindColors() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                ColorName.of("NAME1"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                ColorName.of("NAME2"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(color1);
        update(color2);

        // when: finding colors
        var colors = find();

        // then: the colors are found sorted by creation date
        assertThat(colors).containsExactly(color2, color1);
    }

    @Test
    void shouldFindColorsBySearchTerm() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                ColorName.of("Red"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                ColorName.of("Green"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var color3 = LookupColor.of(
                ColorId.create(),
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
        var colors = find("ee");

        // then: the colors are found by search term
        assertThat(colors).containsExactly(color2);

        // when: finding colors by another search term
        colors = find("bl");

        // then: the colors are found by another search term
        assertThat(colors).containsExactly(color3);

        // when: finding colors by another search term
        colors = find("    ");

        // then: the colors are found by another search term
        assertThat(colors).containsExactly(color2, color3, color1);

        // when: finding colors by another search term
        colors = find("blblblbll");

        // then: the colors are found by another search term
        assertThat(colors).isEmpty();
    }

    @Test
    void shouldFindColorsWithPaging() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                ColorName.of("NAME1"),
                255,
                0,
                0,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                ColorName.of("NAME2"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var color3 = LookupColor.of(
                ColorId.create(),
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
        var colors = find(1, 1);

        // then: the colors are found with paging
        assertThat(colors).containsExactly(color2);

        // when: finding colors with paging
        colors = find(2, 1);

        // then: the colors are found with paging
        assertThat(colors).containsExactly(color1);

        // when: finding colors with paging
        colors = find(3, 1);

        // then: the colors are found with paging
        assertThat(colors).isEmpty();

        // when: finding colors with paging
        colors = find(0, 2);

        // then: the colors are found with paging
        assertThat(colors).containsExactly(color3, color2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some colors
        var color1 = LookupColor.of(
                ColorId.create(),
                ColorName.of("Lightblue"),
                200,
                200,
                255,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var color2 = LookupColor.of(
                ColorId.create(),
                ColorName.of("Green"),
                0,
                255,
                0,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var color3 = LookupColor.of(
                ColorId.create(),
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
        var colors = find("bl", 0, 1);

        // then: the colors are found with search term and paging
        assertThat(colors).containsExactly(color3);

        // when: finding colors with search term and paging
        colors = find("ee", 1, 1);

        // then: the colors are found with search term and paging
        assertThat(colors).isEmpty();

        // when: finding colors with search term and paging
        colors = find("bl", 0, 5);

        // then: the colors are found with search term and paging
        assertThat(colors).containsExactly(color3, color1);
    }

    private void update(LookupColor color) {
        repo.update(color).block();
    }

    private void remove(ColorId colorId) {
        repo.remove(colorId).block();
    }

    private List<LookupColor> find() {
        return find("", 0, Integer.MAX_VALUE);
    }

    private List<LookupColor> find(String searchTerm) {
        return find(searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupColor> find(long skip, long limit) {
        return find("", skip, limit);
    }

    private List<LookupColor> find(String searchTerm, long skip, long limit) {
        return repo.find(searchTerm, skip, limit).collectList().block();
    }

}
