package de.bennyboer.kicherkrabbe.assets.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class AssetDTO {

    String id;

    long version;

    String contentType;

    long fileSize;

    String createdAt;

    List<AssetReferenceDTO> references;

}
