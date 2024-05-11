package de.bennyboer.kicherkrabbe.fabrics.http.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateFabricAvailabilityRequest {

    public long version;

    public Set<FabricTypeAvailabilityDTO> availability;

}
