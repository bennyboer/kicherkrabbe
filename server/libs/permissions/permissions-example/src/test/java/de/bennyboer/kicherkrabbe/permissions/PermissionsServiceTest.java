package de.bennyboer.kicherkrabbe.permissions;

import de.bennyboer.kicherkrabbe.permissions.events.PermissionEvent;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class PermissionsServiceTest {

    protected final Set<PermissionEvent> seenEvents = new HashSet<>();

    protected PermissionsRepo permissionsRepo;

    protected PermissionsService service;

    protected final Holder holder = Holder.user(HolderId.of("USER_ID"));

    protected final ResourceType resourceType = ResourceType.of("RESOURCE_TYPE");

    protected final ResourceId resourceId = ResourceId.of("RESOURCE_ID");

    protected final Resource resource = Resource.of(resourceType, resourceId);

    protected final Action testAction = Action.of("TEST_ACTION");

    protected abstract PermissionsRepo createRepo();

    @BeforeEach
    void setup() {
        permissionsRepo = createRepo();
        service = new PermissionsService(
                permissionsRepo, event -> {
            seenEvents.add(event);
            return Mono.empty();
        }
        );
    }

    @Test
    void shouldAddPermission() {
        // given: a permission to add
        Permission permission = Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .on(resource);

        // when: adding the permission
        addPermission(permission);

        // then: the permission is added
        assertThat(hasPermission(permission)).isTrue();

        // and: an event is published
        assertThat(seenEvents).containsExactly(PermissionEvent.added(permission));
    }

    @Test
    void shouldNotHavePermission() {
        // given: a permission to check
        Permission permission = Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .on(resource);

        // when: checking the permission
        boolean hasPermission = hasPermission(permission);

        // then: the permission is not present
        assertThat(hasPermission).isFalse();
    }

    @Test
    void shouldNotAllowHavingAPermissionTwice() {
        // given: a permission to add
        Permission permission = Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .on(resource);

        // and: the permission is added
        addPermission(permission);

        // when: adding the permission again
        addPermission(permission);

        // then: the permission is still present only once
        assertThat(findPermissionsByHolder(holder))
                .containsExactly(permission);
    }

    @Test
    void shouldHavePermissionOnNoSpecificResource() {
        // given: a permission on no specific resource
        Permission permission = Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .onType(resourceType);
        addPermission(permission);

        // when: checking the permission
        boolean hasPermission = hasPermission(permission);

        // then: the permission is present
        assertThat(hasPermission).isTrue();

        // when: checking the permission for a specific resource
        hasPermission = hasPermission(Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .on(resource));

        // then: the permission is not present
        assertThat(hasPermission).isFalse();
    }

    @Test
    void shouldAddMultiplePermissions() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: a set of permissions to add
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // when: adding the permissions
        addPermissions(permissions);

        // then: holder 1 has permissions to perform the test action on resource 1 and 2
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1))).isTrue();
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource2))).isTrue();

        // and: holder 2 has permissions to perform the test action on resource 2 but not on resource 1
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource2))).isTrue();
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource1))).isFalse();

        // and: an event is published
        assertThat(seenEvents).contains(PermissionEvent.added(permissions));
    }

    @Test
    void shouldNotFailOnAddingMultiplePermissionsWithOneDuplicate() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: a set of permissions to add
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // and: the first permission is already added
        addPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1));
        seenEvents.clear();

        // when: adding the permissions
        addPermissions(permissions);

        // then: there are only three permissions recorded at total
        assertThat(findPermissionsByHolder(holder1)).hasSize(2);
        assertThat(findPermissionsByHolder(holder2)).hasSize(1);

        // and: an event is published for the two new permissions only
        assertThat(seenEvents).hasSize(1);
        assertThat(seenEvents).contains(PermissionEvent.added(Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));

        // then: holder 1 has permissions to perform the test action on resource 1 and 2
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1))).isTrue();
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource2))).isTrue();

        // and: holder 2 has permissions to perform the test action on resource 2 but not on resource 1
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource2))).isTrue();
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource1))).isFalse();
    }

    @Test
    void shouldFindPermissionsByHolder() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: a set of permissions to add
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // and: the permissions are added
        addPermissions(permissions);

        // when: finding permissions for holder 1
        var foundPermissions = findPermissionsByHolder(holder1);

        // then: the permissions for holder 1 are found
        assertThat(foundPermissions).hasSize(2);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // when: finding permissions for holder 2
        foundPermissions = findPermissionsByHolder(holder2);

        // then: the permissions for holder 2 are found
        assertThat(foundPermissions).hasSize(1);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );
    }

    @Test
    void shouldFindPermissionsByHolderAndResourceType() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource3)
        );

        addPermissions(permissions);

        // when: finding permissions for holder 1 and resource type 1
        var foundPermissions = findPermissionsByHolderAndResourceType(
                holder1,
                ResourceType.of("RESOURCE_TYPE_1")
        );

        // then: the permissions for holder 1 and resource type 1 are found
        assertThat(foundPermissions).hasSize(1);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1)
        );

        // when: finding permissions for holder 2 and resource type 1
        foundPermissions = findPermissionsByHolderAndResourceType(holder2, ResourceType.of("RESOURCE_TYPE_1"));

        // then: the permissions for holder 2 and resource type 1 are found
        assertThat(foundPermissions).hasSize(1);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource3)
        );
    }

    @Test
    void shouldFindPermissionsByHolderAndResourceTypeAndAction() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        var action1 = Action.of("ACTION_1");
        var action2 = Action.of("ACTION_2");

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action1)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action2)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(action2)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(action1)
                        .on(resource3)
        );

        addPermissions(permissions);

        // when: finding permissions for holder 1 and resource type 1 and action 1
        var foundPermissions = findPermissionsByHolderAndResourceTypeAndAction(
                holder1,
                ResourceType.of("RESOURCE_TYPE_1"),
                action1
        );

        // then: the permissions for holder 1 and resource type 1 and action 1 are found
        assertThat(foundPermissions).hasSize(1);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action1)
                        .on(resource1));
    }

    @Test
    void shouldFindPermissionsByResourceTypeAndAction() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        var action1 = Action.of("ACTION_1");
        var action2 = Action.of("ACTION_2");

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action1)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action2)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action2)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(action2)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(action1)
                        .on(resource3)
        );

        addPermissions(permissions);

        // when: finding permissions for holder 1 and resource type 1 and action 1
        var foundPermissions = findPermissionsByResourceTypeAndAction(
                ResourceType.of("RESOURCE_TYPE_1"),
                action1
        );

        // then: the permissions for are found
        assertThat(foundPermissions).hasSize(2);
        assertThat(foundPermissions).containsExactlyInAnyOrder(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(action1)
                        .on(resource1),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(action1)
                        .on(resource3)
        );
    }

    @Test
    void shouldFindPermissionsByHolderAndResource() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));
        var resource3 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_3"));

        // given: some permissions
        var permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource3)
        );

        addPermissions(permissions);

        // when: finding permissions for holder 1 and resource 1
        var foundPermissions = findPermissionsByHolderAndResource(holder1, resource1);

        // then: the permissions for holder 1 and resource 1 are found
        assertThat(foundPermissions).hasSize(1);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1)
        );

        // when: finding permissions for holder 2 and resource 1
        foundPermissions = findPermissionsByHolderAndResource(holder2, resource1);

        // then: no permissions for holder 2 and resource 1 are found
        assertThat(foundPermissions).isEmpty();

        // when: finding permissions for holder 2 and resource 2
        foundPermissions = findPermissionsByHolderAndResource(holder2, resource2);

        // then: the permissions for holder 2 and resource 2 are found
        assertThat(foundPermissions).hasSize(1);
        assertThat(foundPermissions).contains(
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );
    }

    @Test
    void shouldRemovePermission() {
        // given: a permission to remove
        Permission permission = Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .on(resource);

        // and: the permission is added
        addPermission(permission);

        // when: removing the permission
        removePermission(permission);

        // then: the permission is removed
        assertThat(hasPermission(permission)).isFalse();

        // and: an event is published
        assertThat(seenEvents).contains(PermissionEvent.removed(permission));
    }

    @Test
    void shouldRemovePermissions() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // and: the permissions are added
        addPermissions(permissions);

        // when: removing two of the permissions
        removePermissions(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        // then: the two permissions are removed
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1))).isFalse();
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource2))).isFalse();

        // and: an event is published that the two permissions are removed
        assertThat(seenEvents).contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));
    }

    @Test
    void shouldNotPublishEventWhenPermissionToBeRemovedIsAbsent() {
        // given: a permission to remove
        Permission permission = Permission.builder()
                .holder(holder)
                .isAllowedTo(testAction)
                .on(resource);

        // when: removing the permission
        removePermission(permission);

        // then: no event is published
        assertThat(seenEvents).isEmpty();
    }

    @Test
    void shouldRemovePermissionsByHolder() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        addPermissions(permissions);

        // when: removing permissions for holder 1
        removePermissionsByHolder(holder1);

        // then: the permissions for holder 1 are removed
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1))).isFalse();
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource2))).isFalse();

        // and: the permissions for holder 2 are still present
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource2))).isTrue();

        // and: an event is published
        assertThat(seenEvents).contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));
    }

    @Test
    void shouldRemovePermissionsByResource() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        addPermissions(permissions);

        // when: removing permissions for resource 2
        removePermissionsByResource(resource2);

        // then: the permissions for resource 2 are removed
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource2))).isFalse();
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource2))).isFalse();

        // and: the permissions for resource 1 are still present
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1))).isTrue();

        // and: an event is published
        assertThat(seenEvents).contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));
    }

    @Test
    void shouldRemovePermissionsByHolderAndResource() {
        var holder1 = Holder.user(HolderId.of("USER_ID_1"));
        var holder2 = Holder.user(HolderId.of("USER_ID_2"));

        var resource1 = Resource.of(ResourceType.of("RESOURCE_TYPE_1"), ResourceId.of("RESOURCE_ID_1"));
        var resource2 = Resource.of(ResourceType.of("RESOURCE_TYPE_2"), ResourceId.of("RESOURCE_ID_2"));

        // given: some permissions
        Set<Permission> permissions = Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource1),
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2),
                Permission.builder()
                        .holder(holder2)
                        .isAllowedTo(testAction)
                        .on(resource2)
        );

        addPermissions(permissions);

        // when: removing permissions for holder 1 and resource 2
        removePermissionsByHolderAndResource(holder1, resource2);

        // then: the permissions for holder 1 and resource 2 are removed
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource2))).isFalse();

        // and: the permissions for holder 1 and resource 1 are still present
        assertThat(hasPermission(Permission.builder()
                .holder(holder1)
                .isAllowedTo(testAction)
                .on(resource1))).isTrue();

        // and: the permissions for holder 2 and resource 2 are still present
        assertThat(hasPermission(Permission.builder()
                .holder(holder2)
                .isAllowedTo(testAction)
                .on(resource2))).isTrue();

        // and: an event is published
        assertThat(seenEvents).contains(PermissionEvent.removed(Set.of(
                Permission.builder()
                        .holder(holder1)
                        .isAllowedTo(testAction)
                        .on(resource2)
        )));
    }

    private void addPermission(Permission permission) {
        service.addPermission(permission).block();
    }

    private void addPermissions(Set<Permission> permissions) {
        service.addPermissions(permissions).block();
    }

    private void removePermission(Permission permission) {
        service.removePermission(permission).block();
    }

    private void removePermissions(Permission... permissions) {
        service.removePermissions(permissions).block();
    }

    private void removePermissionsByHolder(Holder holder) {
        service.removePermissionsByHolder(holder).block();
    }

    private void removePermissionsByResource(Resource resource) {
        service.removePermissionsByResource(resource).block();
    }

    private void removePermissionsByHolderAndResource(Holder holder, Resource resource) {
        service.removePermissionsByHolderAndResource(holder, resource).block();
    }

    private boolean hasPermission(Permission permission) {
        return service.hasPermission(permission).block();
    }

    private List<Permission> findPermissionsByHolder(Holder holder) {
        return service.findPermissionsByHolder(holder).collectList().block();
    }

    private List<Permission> findPermissionsByHolderAndResourceType(Holder holder, ResourceType resourceType) {
        return service.findPermissionsByHolderAndResourceType(holder, resourceType).collectList().block();
    }

    private List<Permission> findPermissionsByHolderAndResourceTypeAndAction(
            Holder holder,
            ResourceType resourceType,
            Action action
    ) {
        return service.findPermissionsByHolderAndResourceTypeAndAction(holder, resourceType, action)
                .collectList()
                .block();
    }

    private List<Permission> findPermissionsByResourceTypeAndAction(ResourceType resourceType, Action action) {
        return service.findPermissionsByResourceTypeAndAction(resourceType, action)
                .collectList()
                .block();
    }

    private List<Permission> findPermissionsByHolderAndResource(Holder holder, Resource resource) {
        return service.findPermissionsByHolderAndResource(holder, resource).collectList().block();
    }

}
