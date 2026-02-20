package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.VersionedReadModel;
import de.bennyboer.kicherkrabbe.offers.FabricComposition;
import de.bennyboer.kicherkrabbe.offers.Links;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.ProductNumber;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LookupProduct implements VersionedReadModel<ProductId> {

    ProductId id;

    Version version;

    ProductNumber number;

    Links links;

    FabricComposition fabricComposition;

    public static LookupProduct of(
            ProductId id,
            Version version,
            ProductNumber number,
            Links links,
            FabricComposition fabricComposition
    ) {
        notNull(id, "Product ID must be given");
        notNull(version, "Version must be given");
        notNull(number, "Product number must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");

        return new LookupProduct(id, version, number, links, fabricComposition);
    }

}
