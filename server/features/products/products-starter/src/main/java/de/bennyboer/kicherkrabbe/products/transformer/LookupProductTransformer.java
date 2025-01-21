package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.ProductDTO;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.product.ImageId;

public class LookupProductTransformer {

    public static ProductDTO toApi(LookupProduct product) {
        var result = new ProductDTO();

        result.id = product.getId().getValue();
        result.version = product.getVersion().getValue();
        result.number = product.getNumber().getValue();
        result.images = product.getImages()
                .stream()
                .map(ImageId::getValue)
                .toList();
        result.links = LinksTransformer.toApi(product.getLinks());
        result.fabricComposition = FabricCompositionTransformer.toApi(product.getFabricComposition());
        result.notes = NotesTransformer.toApi(product.getNotes());
        result.producedAt = product.getProducedAt();
        result.createdAt = product.getCreatedAt();

        return result;
    }

}
