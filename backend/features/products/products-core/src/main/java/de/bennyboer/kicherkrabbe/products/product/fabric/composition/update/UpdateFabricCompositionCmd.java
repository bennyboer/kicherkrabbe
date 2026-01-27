package de.bennyboer.kicherkrabbe.products.product.fabric.composition.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.products.product.FabricComposition;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateFabricCompositionCmd implements Command {

    FabricComposition fabricComposition;

    public static UpdateFabricCompositionCmd of(FabricComposition fabricComposition) {
        notNull(fabricComposition, "Fabric composition must be given");

        return new UpdateFabricCompositionCmd(fabricComposition);
    }

}
