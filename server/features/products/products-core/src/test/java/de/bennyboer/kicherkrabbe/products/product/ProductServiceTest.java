package de.bennyboer.kicherkrabbe.products.product;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.products.product.samples.SampleProduct;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.products.product.FabricType.*;
import static de.bennyboer.kicherkrabbe.products.product.LinkType.FABRIC;
import static de.bennyboer.kicherkrabbe.products.product.LinkType.PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final ProductService productService = new ProductService(repo, eventPublisher, Clock.systemUTC());

    @Test
    void shouldCreateProduct() {
        // when: creating a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // then: the product is created
        var product = get(id);
        assertThat(product.getId()).isEqualTo(id);
        assertThat(product.getVersion()).isEqualTo(Version.zero());
        assertThat(product.getNumber()).isEqualTo(ProductNumber.of("00001"));
        assertThat(product.getImages()).containsExactlyInAnyOrder(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2"));
        assertThat(product.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
        )));
        assertThat(product.getFabricComposition()).isEqualTo(FabricComposition.of(Set.of(
                FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
        )));
        assertThat(product.getNotes()).isEqualTo(Notes.of(
                Note.of("This product contains stuff."),
                Note.of("This product does not need much care!"),
                Note.of("This product is not very dangerous.")
        ));
        assertThat(product.getProducedAt()).isEqualTo(Instant.parse("2024-12-10T12:30:00.00Z"));
        assertThat(product.isDeleted()).isFalse();
    }

    @Test
    void shouldNotAllowAddingLinkToNotYetCreatedProduct() {
        // given: a product that has not yet been created
        var id = ProductId.of("PRODUCT_ID");

        // when: adding a link to the product; then an error is raised
        assertThatThrownBy(() -> addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("Pattern")
        ))).matches(e -> e instanceof IllegalArgumentException && e.getMessage()
                .equals("Cannot apply command to not yet created aggregate"));
    }

    @Test
    void shouldAddLink() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: adding a link
        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID_2"),
                LinkName.of("Pattern 2")
        ));

        // then: the link is added
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                Link.of(PATTERN, LinkId.of("PATTERN_ID_2"), LinkName.of("Pattern 2")),
                Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
        )));
    }

    @Test
    void shouldNotAddLinkGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        var version = addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID_2"),
                LinkName.of("Pattern 2")
        ));

        // when: a link is added with an outdated version; then an error is raised
        assertThatThrownBy(() -> addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID_3"),
                LinkName.of("Pattern 3")
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateLink() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: adding a link
        var version = updateLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("New name")
        ));

        // then: the link is updated
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("New name")),
                Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
        )));
    }

    @Test
    void shouldNotUpdateLinkGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        updateLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("New name")
        ));

        // when: a link is updated with an outdated version; then an error is raised
        assertThatThrownBy(() -> updateLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID"),
                LinkName.of("New name 2")
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRemoveLink() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: removing a link
        var version = removeLink(id, Version.zero(), PATTERN, LinkId.of("PATTERN_ID"));

        // then: the link is removed
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getLinks()).isEqualTo(Links.of(Set.of(
                Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
        )));
    }

    @Test
    void shouldNotRemoveLinkGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        removeLink(id, Version.zero(), PATTERN, LinkId.of("PATTERN_ID"));

        // when: removing a link with an outdated version; then an error is raised
        assertThatThrownBy(() -> removeLink(id, Version.zero(), PATTERN, LinkId.of("PATTERN_ID")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateProducedAtDate() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: updating the produced at date
        var version = updateProducedAt(id, Version.zero(), Instant.parse("2024-12-10T13:00:00.00Z"));

        // then: the produced at date is updated
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getProducedAt()).isEqualTo(Instant.parse("2024-12-10T13:00:00.00Z"));
    }

    @Test
    void shouldNotUpdateProducedAtDateGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        updateProducedAt(id, Version.zero(), Instant.parse("2024-12-10T13:00:00.00Z"));

        // when: updating the produced at date with an outdated version; then an error is raised
        assertThatThrownBy(() -> updateProducedAt(id, Version.zero(), Instant.parse("2024-12-10T13:30:00.00Z")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateNotes() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: updating the notes
        var version = updateNotes(id, Version.zero(), Notes.of(
                Note.of("A"),
                Note.of("B"),
                Note.of("C")
        ));

        // then: the notes are updated
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getNotes()).isEqualTo(Notes.of(
                Note.of("A"),
                Note.of("B"),
                Note.of("C")
        ));
    }

    @Test
    void shouldNotUpdateNotesGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        updateNotes(id, Version.zero(), Notes.of(
                Note.of("A"),
                Note.of("B"),
                Note.of("C")
        ));

        // when: updating the notes with an outdated version; then an error is raised
        assertThatThrownBy(() -> updateNotes(id, Version.zero(), Notes.of(
                Note.of("D"),
                Note.of("E"),
                Note.of("F")
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateImages() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: updating the images
        var version = updateImages(id, Version.zero(), List.of(
                ImageId.of("IMAGE_ID_3"),
                ImageId.of("IMAGE_ID_4")
        ));

        // then: the images are updated
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getImages()).containsExactlyInAnyOrder(
                ImageId.of("IMAGE_ID_3"),
                ImageId.of("IMAGE_ID_4")
        );
    }

    @Test
    void shouldNotUpdateImagesGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        updateImages(id, Version.zero(), List.of(
                ImageId.of("IMAGE_ID_3"),
                ImageId.of("IMAGE_ID_4")
        ));

        // when: updating the images with an outdated version; then an error is raised
        assertThatThrownBy(() -> updateImages(id, Version.zero(), List.of(
                ImageId.of("IMAGE_ID_5"),
                ImageId.of("IMAGE_ID_6")
        ))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateFabricComposition() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: updating the fabric composition
        var version = updateFabricComposition(id, Version.zero(), FabricComposition.of(Set.of(
                FabricCompositionItem.of(POLYESTER, LowPrecisionFloat.of(7000)),
                FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(3000))
        )));

        // then: the fabric composition is updated
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(version);
        assertThat(product.getFabricComposition()).isEqualTo(FabricComposition.of(Set.of(
                FabricCompositionItem.of(POLYESTER, LowPrecisionFloat.of(7000)),
                FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(3000))
        )));
    }

    @Test
    void shouldNotUpdateFabricCompositionGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        updateFabricComposition(id, Version.zero(), FabricComposition.of(Set.of(
                FabricCompositionItem.of(POLYESTER, LowPrecisionFloat.of(7000)),
                FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(3000))
        )));

        // when: updating the fabric composition with an outdated version; then an error is raised
        assertThatThrownBy(() -> updateFabricComposition(id, Version.zero(), FabricComposition.of(Set.of(
                FabricCompositionItem.of(POLYESTER, LowPrecisionFloat.of(7000)),
                FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(3000))
        )))).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDeleteProduct() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: deleting the product
        var version = delete(id, Version.zero());

        // then: the product is deleted
        var product = get(id);
        assertThat(product).isNull();

        // and: there can be no more events for the product
        assertThatThrownBy(() -> updateProducedAt(id, version, Instant.parse("2024-12-10T13:00:00.00Z")))
                .matches(e -> e instanceof IllegalArgumentException && e.getMessage()
                        .equals("Cannot apply command to deleted aggregate"));
    }

    @Test
    void shouldNotDeleteProductGivenAnOutdatedVersion() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );
        addLink(id, Version.zero(), Link.of(
                PATTERN,
                LinkId.of("PATTERN_ID_2"),
                LinkName.of("Pattern 2")
        ));

        // when: deleting the product with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a product
        var id = create(
                ProductNumber.of("00001"),
                List.of(ImageId.of("IMAGE_ID_1"), ImageId.of("IMAGE_ID_2")),
                Links.of(Set.of(
                        Link.of(PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern")),
                        Link.of(FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Fabric"))
                )),
                FabricComposition.of(Set.of(
                        FabricCompositionItem.of(COTTON, LowPrecisionFloat.of(8000)),
                        FabricCompositionItem.of(ELASTANE, LowPrecisionFloat.of(2000))
                )),
                Notes.of(
                        Note.of("This product contains stuff."),
                        Note.of("This product does not need much care!"),
                        Note.of("This product is not very dangerous.")
                ),
                Instant.parse("2024-12-10T12:30:00.00Z")
        );

        // when: updating the produced at date 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = updateProducedAt(id, version, Instant.parse("2024-12-10T13:00:00.00Z"));
        }

        // then: the products produced at date is updated
        var product = get(id);
        assertThat(product.getVersion()).isEqualTo(Version.of(202));
        assertThat(product.getProducedAt()).isEqualTo(Instant.parse("2024-12-10T13:00:00.00Z"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Product.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private ProductId create(
            ProductNumber number,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Notes notes,
            Instant producedAt
    ) {
        return productService.create(
                number,
                images,
                links,
                fabricComposition,
                notes,
                producedAt,
                Agent.system()
        ).block().getId();
    }

    private ProductId create(SampleProduct sample) {
        return create(
                sample.getNumber(),
                sample.getImageIds(),
                sample.getLinks(),
                sample.getFabricComposition(),
                sample.getNotes(),
                sample.getProducedAt()
        );
    }

    private ProductId createSampleProduct() {
        return create(SampleProduct.builder().build());
    }

    private Product get(ProductId id) {
        return productService.get(id).block();
    }

    private Version addLink(ProductId id, Version version, Link link) {
        return productService.addLink(id, version, link, Agent.system()).block();
    }

    private Version updateLink(ProductId id, Version version, Link link) {
        return productService.updateLink(id, version, link, Agent.system()).block();
    }

    private Version removeLink(ProductId id, Version version, LinkType linkType, LinkId linkId) {
        return productService.removeLink(id, version, linkType, linkId, Agent.system()).block();
    }

    private Version updateImages(ProductId id, Version version, List<ImageId> images) {
        return productService.updateImages(id, version, images, Agent.system()).block();
    }

    private Version updateFabricComposition(ProductId id, Version version, FabricComposition fabricComposition) {
        return productService.updateFabricComposition(id, version, fabricComposition, Agent.system()).block();
    }

    private Version updateNotes(ProductId id, Version version, Notes notes) {
        return productService.updateNotes(id, version, notes, Agent.system()).block();
    }

    private Version updateProducedAt(ProductId id, Version version, Instant producedAt) {
        return productService.updateProducedAt(id, version, producedAt, Agent.system()).block();
    }

    private Version delete(ProductId id, Version version) {
        return productService.delete(id, version, Agent.system()).block();
    }

}
