package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AssetContent {

    ContentType contentType;

    Flux<DataBuffer> buffers;

    public static AssetContent of(ContentType contentType, Flux<DataBuffer> buffers) {
        notNull(contentType, "Content type must be given");
        notNull(buffers, "Buffers must be given");

        return new AssetContent(contentType, buffers);
    }

}
