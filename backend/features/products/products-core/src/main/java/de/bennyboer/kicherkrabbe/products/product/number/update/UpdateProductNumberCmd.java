package de.bennyboer.kicherkrabbe.products.product.number.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.products.product.ProductNumber;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateProductNumberCmd implements Command {

    ProductNumber number;

    public static UpdateProductNumberCmd of(ProductNumber number) {
        notNull(number, "Product number must be given");

        return new UpdateProductNumberCmd(number);
    }

}
