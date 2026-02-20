package de.bennyboer.kicherkrabbe.offers.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.ImageId;
import de.bennyboer.kicherkrabbe.offers.Notes;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    ProductId productId;

    List<ImageId> images;

    Notes notes;

    Money price;

    public static CreateCmd of(
            ProductId productId,
            List<ImageId> images,
            Notes notes,
            Money price
    ) {
        notNull(productId, "Product ID must be given");
        notNull(images, "Images must be given");
        notNull(notes, "Notes must be given");
        notNull(price, "Price must be given");

        return new CreateCmd(productId, images, notes, price);
    }

}
