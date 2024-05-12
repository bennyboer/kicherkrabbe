package de.bennyboer.kicherkrabbe.fabrics.http.api.requests;

import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class CreateFabricRequest {

    public String name;

    public String imageId;

    public Set<String> colorIds;

    public Set<String> topicIds;

    public Set<FabricTypeAvailabilityDTO> availability;

}
