package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.samples.SampleLookupPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class PatternLookupRepoTest {

    private PatternLookupRepo repo;

    protected abstract PatternLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdatePattern() {
        // given: a pattern to update
        var pattern = SampleLookupPattern.builder().build().toModel();

        // when: updating the pattern
        update(pattern);

        // then: the pattern is updated
        var patterns = find(Set.of(pattern.getId()));
        assertThat(patterns).containsExactly(pattern);
    }

    @Test
    void shouldRemovePattern() {
        // given: some patterns
        var pattern1 = SampleLookupPattern.builder().build().toModel();
        var pattern2 = SampleLookupPattern.builder().build().toModel();
        update(pattern1);
        update(pattern2);

        // when: removing a pattern
        remove(pattern1.getId());

        // then: the pattern is removed
        var patterns = find(Set.of(pattern1.getId(), pattern2.getId()));
        assertThat(patterns).containsExactly(pattern2);
    }

    @Test
    void shouldFindPatterns() {
        // given: some patterns
        var pattern1 = SampleLookupPattern.builder()
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);

        // when: finding patterns
        var patterns = find(Set.of(pattern1.getId(), pattern2.getId()));

        // then: the patterns are found sorted by creation date
        assertThat(patterns).containsExactly(pattern2, pattern1);
    }

    @Test
    void shouldFindPatternsBySearchTerm() {
        // given: some patterns with different names
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A little hat"))
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        // when: finding patterns by search term
        var patternIds = Set.of(pattern1.getId(), pattern2.getId(), pattern3.getId());
        var patterns = find(patternIds, "o");

        // then: the patterns are found by search term
        assertThat(patterns).containsExactly(pattern2);

        // when: finding patterns by another search term
        patterns = find(patternIds, "r");

        // then: the patterns are found by another search term
        assertThat(patterns).containsExactly(pattern2, pattern1);

        // when: finding patterns by blank search term
        patterns = find(patternIds, "    ");

        // then: all patterns are found
        assertThat(patterns).containsExactly(pattern3, pattern2, pattern1);

        // when: finding patterns by non-matching search term
        patterns = find(patternIds, "blblblbll");

        // then: no patterns are found
        assertThat(patterns).isEmpty();
    }

    @Test
    void shouldFindPatternsWithPaging() {
        // given: some patterns
        var pattern1 = SampleLookupPattern.builder()
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        // when: finding patterns with paging
        var patternIds = Set.of(pattern1.getId(), pattern2.getId(), pattern3.getId());

        // then: the patterns are found with paging
        assertThat(find(patternIds, 1, 1)).containsExactly(pattern2);
        assertThat(find(patternIds, 2, 1)).containsExactly(pattern1);
        assertThat(find(patternIds, 3, 1)).isEmpty();
        assertThat(find(patternIds, 0, 2)).containsExactly(pattern3, pattern2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some patterns with different names
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("Summerdress"))
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("Some trousers"))
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A little hat"))
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        // when: finding patterns with search term and paging
        var patternIds = Set.of(pattern1.getId(), pattern2.getId(), pattern3.getId());
        var page = findPage(patternIds, Set.of(), "r", 0, 1);

        // then: the patterns are found with search term and paging
        assertThat(page.getResults()).containsExactly(pattern2);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding patterns with another search term and paging
        page = findPage(patternIds, Set.of(), "hat", 1, 1);

        // then: the patterns are found with another search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindPublishedPattern() {
        // given: a published and an unpublished pattern
        var pattern1 = SampleLookupPattern.builder().published(true).build().toModel();
        var pattern2 = SampleLookupPattern.builder().published(false).build().toModel();
        update(pattern1);
        update(pattern2);

        // when: finding the published pattern
        var foundPattern1 = findPublished(pattern1.getId());

        // then: the published pattern is found
        assertThat(foundPattern1).isEqualTo(pattern1);

        // when: finding the unpublished pattern
        var foundPattern2 = findPublished(pattern2.getId());

        // then: the unpublished pattern is not found
        assertThat(foundPattern2).isNull();
    }

    @Test
    void shouldFindPublishedPatterns() {
        // given: some patterns with different names
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("C"))
                .published(true)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("B"))
                .published(false)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var pattern3 = SampleLookupPattern.builder()
                .name(PatternName.of("A"))
                .published(true)
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        // when: finding all published patterns ordered by name ascending
        var result = findPublished("", Set.of(), Set.of(), true, 0, 10);

        // then: all published patterns are found ordered by name ascending
        assertThat(result.getResults()).containsExactly(pattern3, pattern1);

        // when: finding published patterns by search term
        result = findPublished("a", Set.of(), Set.of(), false, 0, 10);

        // then: all published patterns are found by search term
        assertThat(result.getResults()).containsExactly(pattern3);

        // when: finding published patterns with paging
        result = findPublished("", Set.of(), Set.of(), true, 0, 1);

        // then: all published patterns are found with paging
        assertThat(result.getResults()).containsExactly(pattern3);

        // when: finding published patterns with paging
        result = findPublished("", Set.of(), Set.of(), true, 1, 1);

        // then: all published patterns are found with paging
        assertThat(result.getResults()).containsExactly(pattern1);
    }

    @Test
    void shouldFindPublishedPatternsByCategory() {
        // given: some published patterns with different categories
        var categoryId1 = PatternCategoryId.of("CATEGORY_ID_1");
        var categoryId2 = PatternCategoryId.of("CATEGORY_ID_2");

        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("A"))
                .category(categoryId1)
                .published(true)
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("B"))
                .category(categoryId2)
                .published(true)
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);

        // when: finding published patterns with categories filter
        var result = findPublished("", Set.of(categoryId1), Set.of(), true, 0, 10);

        // then: only patterns with the specified category are found
        assertThat(result.getResults()).containsExactly(pattern1);
    }

    @Test
    void shouldFindPublishedPatternsBySize() {
        // given: some published patterns with different sizes
        var pattern1 = SampleLookupPattern.builder()
                .name(PatternName.of("A"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(86, 92L, null, Money.euro(2900)))
                ))
                .published(true)
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .name(PatternName.of("B"))
                .variant(PatternVariant.of(
                        PatternVariantName.of("Normal"),
                        Set.of(PricedSizeRange.of(98, 104L, null, Money.euro(2900)))
                ))
                .published(true)
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);

        // when: finding published patterns with size filter
        var result = findPublished("", Set.of(), Set.of(92L), true, 0, 10);

        // then: only patterns with the specified size are found
        assertThat(result.getResults()).containsExactly(pattern1);

        // when: finding published patterns with a non-matching size filter
        result = findPublished("", Set.of(), Set.of(170L), true, 0, 10);

        // then: no patterns are found
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    void shouldFindPatternsByCategory() {
        // given: some patterns with different categories
        var categoryId1 = PatternCategoryId.of("CATEGORY_ID_1");
        var categoryId2 = PatternCategoryId.of("CATEGORY_ID_2");

        var pattern1 = SampleLookupPattern.builder().category(categoryId1).build().toModel();
        var pattern2 = SampleLookupPattern.builder().category(categoryId2).build().toModel();
        var pattern3 = SampleLookupPattern.builder().category(categoryId2).category(categoryId1).build().toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        // when: finding patterns by category
        var patterns = findByCategory(categoryId2);

        // then: the patterns are found by category
        assertThat(patterns).containsExactlyInAnyOrder(pattern2, pattern3);

        // when: finding patterns by another category
        patterns = findByCategory(categoryId1);

        // then: the patterns are found by another category
        assertThat(patterns).containsExactlyInAnyOrder(pattern1, pattern3);

        // when: finding patterns by a category that is not used
        patterns = findByCategory(PatternCategoryId.of("CATEGORY_ID_4"));

        // then: no patterns are found
        assertThat(patterns).isEmpty();
    }

    @Test
    void shouldFindUniqueCategories() {
        // given: some patterns with different categories
        var categoryId1 = PatternCategoryId.of("CATEGORY_ID_1");
        var categoryId2 = PatternCategoryId.of("CATEGORY_ID_2");

        var pattern1 = SampleLookupPattern.builder().category(categoryId1).build().toModel();
        var pattern2 = SampleLookupPattern.builder().category(categoryId2).build().toModel();
        var pattern3 = SampleLookupPattern.builder().category(categoryId2).category(categoryId1).build().toModel();
        update(pattern1);
        update(pattern2);
        update(pattern3);

        // when: finding unique categories
        var categories = findUniqueCategories();

        // then: the unique categories are found
        assertThat(categories).containsExactlyInAnyOrder(categoryId1, categoryId2);

        // when: removing all patterns and finding unique categories
        remove(pattern1.getId());
        remove(pattern2.getId());
        remove(pattern3.getId());

        categories = findUniqueCategories();

        // then: no unique categories are found
        assertThat(categories).isEmpty();
    }

    @Test
    void shouldFindPatternByAlias() {
        // given: some patterns with different aliases
        var pattern1 = SampleLookupPattern.builder()
                .alias(PatternAlias.of("summerdress"))
                .build()
                .toModel();
        var pattern2 = SampleLookupPattern.builder()
                .alias(PatternAlias.of("trousers"))
                .build()
                .toModel();
        update(pattern1);
        update(pattern2);

        // when: finding pattern by alias
        var foundPattern1 = findByAlias(PatternAlias.of("summerdress"));

        // then: the pattern is found by alias
        assertThat(foundPattern1).isEqualTo(pattern1);

        // when: finding pattern by alias
        var foundPattern2 = findByAlias(PatternAlias.of("trousers"));

        // then: the pattern is found by alias
        assertThat(foundPattern2).isEqualTo(pattern2);

        // when: finding pattern by alias that does not exist
        var foundPattern3 = findByAlias(PatternAlias.of("non-existing-alias"));

        // then: the pattern is not found by alias
        assertThat(foundPattern3).isNull();
    }

    private List<PatternCategoryId> findUniqueCategories() {
        return repo.findUniqueCategories().collectList().block();
    }

    private List<LookupPattern> findByCategory(PatternCategoryId categoryId) {
        return repo.findByCategory(categoryId).collectList().block();
    }

    private LookupPattern findPublished(PatternId id) {
        return repo.findPublished(id).block();
    }

    private LookupPattern findByAlias(PatternAlias alias) {
        return repo.findByAlias(alias).block();
    }

    private LookupPatternPage findPublished(
            String searchTerm,
            Set<PatternCategoryId> categories,
            Set<Long> sizes,
            boolean ascending,
            long skip,
            long limit
    ) {
        return repo.findPublished(searchTerm, categories, sizes, ascending, skip, limit).block();
    }

    private void update(LookupPattern pattern) {
        repo.update(pattern).block();
    }

    private void remove(PatternId patternId) {
        repo.remove(patternId).block();
    }

    private List<LookupPattern> find(Set<PatternId> patternIds) {
        return find(patternIds, Set.of(), "", 0, Integer.MAX_VALUE);
    }

    private List<LookupPattern> find(Set<PatternId> patternIds, String searchTerm) {
        return find(patternIds, Set.of(), searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupPattern> find(Set<PatternId> patternIds, long skip, long limit) {
        return find(patternIds, Set.of(), "", skip, limit);
    }

    private List<LookupPattern> find(
            Set<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(patternIds, categories, searchTerm, skip, limit).block().getResults();
    }

    private LookupPatternPage findPage(
            Set<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.find(patternIds, categories, searchTerm, skip, limit).block();
    }

}
