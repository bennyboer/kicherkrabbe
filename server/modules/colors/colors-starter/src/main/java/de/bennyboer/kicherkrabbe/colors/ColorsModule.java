package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.colors.persistence.lookup.ColorLookupRepo;
import de.bennyboer.kicherkrabbe.colors.persistence.lookup.LookupColor;
import de.bennyboer.kicherkrabbe.permissions.*;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import static de.bennyboer.kicherkrabbe.colors.Actions.CREATE;

@AllArgsConstructor
public class ColorsModule {

    private final ColorService colorService;

    private final PermissionsService permissionsService;

    private final ColorLookupRepo colorLookupRepo;

    public Mono<Void> allowUserToCreateColors(String userId) {
        Holder userHolder = Holder.user(HolderId.of(userId));

        return permissionsService.addPermission(Permission.builder()
                .holder(userHolder)
                .isAllowedTo(CREATE)
                .onType(getResourceType()));
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        Holder userHolder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(userHolder);
    }

    public Mono<Void> updateColorInLookup(String colorId) {
        return colorService.get(ColorId.of(colorId))
                .flatMap(color -> colorLookupRepo.update(LookupColor.of(
                        color.getId(),
                        color.getName(),
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        color.getCreatedAt()
                )));
    }

    public Mono<Void> removeColorFromLookup(String colorId) {
        return colorLookupRepo.remove(ColorId.of(colorId));
    }

    private ResourceType getResourceType() {
        return ResourceType.of("COLOR");
    }

}
