package de.bennyboer.kicherkrabbe.fabrics.http.api.requests;

import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricKindDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreateFabricRequest {

    public String name;

    public FabricKindDTO kind;

    public String imageId;

    public List<String> exampleImageIds;

    public Set<String> colorIds;

    public Set<String> topicIds;

    public Set<FabricTypeAvailabilityDTO> availability;

}
