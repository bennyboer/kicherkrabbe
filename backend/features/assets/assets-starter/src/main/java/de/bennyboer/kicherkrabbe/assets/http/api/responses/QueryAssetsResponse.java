package de.bennyboer.kicherkrabbe.assets.http.api.responses;

import de.bennyboer.kicherkrabbe.assets.http.api.AssetDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryAssetsResponse {

    long skip;

    long limit;

    long total;

    List<AssetDTO> assets;

}
