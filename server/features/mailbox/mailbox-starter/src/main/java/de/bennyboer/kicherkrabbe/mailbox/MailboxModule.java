package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailbox.api.StatusDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsReadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsUnreadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.*;
import de.bennyboer.kicherkrabbe.mailbox.mail.Content;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailId;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailService;
import de.bennyboer.kicherkrabbe.mailbox.mail.Subject;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMail;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.MailLookupRepo;
import de.bennyboer.kicherkrabbe.mailbox.transformer.LookupMailTransformer;
import de.bennyboer.kicherkrabbe.mailbox.transformer.OriginTransformer;
import de.bennyboer.kicherkrabbe.mailbox.transformer.SenderTransformer;
import de.bennyboer.kicherkrabbe.mailbox.transformer.StatusTransformer;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.mailbox.Actions.*;

public class MailboxModule {

    private final MailService mailService;

    private final MailLookupRepo mailLookupRepo;

    private final PermissionsService permissionsService;

    public MailboxModule(
            MailService mailService,
            MailLookupRepo mailLookupRepo,
            PermissionsService permissionsService
    ) {
        this.mailService = mailService;
        this.mailLookupRepo = mailLookupRepo;
        this.permissionsService = permissionsService;
    }

    private boolean isInitialized = false;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent ignoredEvent) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        initialize()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        return allowSystemUserToReceiveMails();
    }

    public Mono<ReceiveMailResponse> receiveMail(ReceiveMailRequest request, Agent agent) {
        var origin = OriginTransformer.toInternal(request.origin);
        var sender = SenderTransformer.toInternal(request.sender);
        var subject = Subject.of(request.subject);
        var content = Content.of(request.content);

        return assertAgentIsAllowedTo(agent, RECEIVE)
                .then(mailService.receive(origin, sender, subject, content, agent))
                .map(idAndVersion -> {
                    var result = new ReceiveMailResponse();
                    result.mailId = idAndVersion.getId().getValue();
                    result.version = idAndVersion.getVersion().getValue();
                    return result;
                });
    }

    public Mono<QueryMailResponse> getMail(String mailId, Agent agent) {
        var id = MailId.of(mailId);

        return assertAgentIsAllowedTo(agent, READ, id)
                .then(mailLookupRepo.findById(id))
                .map(mail -> {
                    var result = new QueryMailResponse();
                    result.mail = LookupMailTransformer.toApi(mail);
                    return result;
                });
    }

    public Mono<QueryMailsResponse> getMails(
            String searchTerm,
            @Nullable StatusDTO status,
            long skip,
            long limit,
            Agent agent
    ) {
        var internalStatus = Optional.ofNullable(status)
                .map(StatusTransformer::toInternal)
                .orElse(null);

        return assertAgentIsAllowedTo(agent, READ)
                .then(mailLookupRepo.query(searchTerm, internalStatus, skip, limit))
                .map(page -> {
                    var result = new QueryMailsResponse();
                    result.total = page.getTotal();
                    result.mails = LookupMailTransformer.toApi(page.getMails());
                    return result;
                });
    }

    public Mono<MarkMailAsReadResponse> markMailAsRead(String mailId, MarkMailAsReadRequest request, Agent agent) {
        var id = MailId.of(mailId);
        var version = Version.of(request.version);

        return assertAgentIsAllowedTo(agent, MARK_AS_READ, id)
                .then(mailService.markAsRead(id, version, agent))
                .map(Version::getValue)
                .map(newVersion -> {
                    var result = new MarkMailAsReadResponse();
                    result.version = newVersion;
                    return result;
                });
    }

    public Mono<MarkMailAsUnreadResponse> markMailAsUnread(
            String mailId,
            MarkMailAsUnreadRequest request,
            Agent agent
    ) {
        var id = MailId.of(mailId);
        var version = Version.of(request.version);

        return assertAgentIsAllowedTo(agent, MARK_AS_UNREAD, id)
                .then(mailService.markAsUnread(id, version, agent))
                .map(Version::getValue)
                .map(newVersion -> {
                    var result = new MarkMailAsUnreadResponse();
                    result.version = newVersion;
                    return result;
                });
    }

    public Mono<DeleteMailResponse> deleteMail(String mailId, long version, Agent agent) {
        var id = MailId.of(mailId);
        var versionToDelete = Version.of(version);

        return assertAgentIsAllowedTo(agent, DELETE, id)
                .then(mailService.delete(id, versionToDelete, agent))
                .map(Version::getValue)
                .map(newVersion -> {
                    var result = new DeleteMailResponse();
                    result.version = newVersion;
                    return result;
                });
    }

    public Mono<Void> allowSystemUserToReceiveMails() {
        var systemHolder = Holder.group(HolderId.system());

        var receiveMails = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(RECEIVE)
                .onType(getResourceType());

        return permissionsService.addPermission(receiveMails);
    }

    public Mono<Void> allowUserToReadAndManageMails(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));

        var readMails = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getResourceType());
        var manageMails = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(MANAGE)
                .onType(getResourceType());

        return permissionsService.addPermissions(readMails, manageMails);
    }

    public Mono<Void> allowUsersThatAreAllowedToManageMailsToManageMail(String mailId) {
        return permissionsService.findPermissionsByResourceTypeAndAction(getResourceType(), MANAGE)
                .map(Permission::getHolder)
                .collect(Collectors.toSet())
                .flatMap(holders -> {
                    var newPermissions = holders.stream()
                            .flatMap(holder -> buildPermissionsForHolderToManageMail(
                                    holder,
                                    MailId.of(mailId)
                            ).stream())
                            .collect(Collectors.toSet());

                    return permissionsService.addPermissions(newPermissions);
                });
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));
        return permissionsService.removePermissionsByHolder(userHolder);
    }

    public Mono<Void> removePermissionsForMail(String mailId) {
        var mailResource = Resource.of(getResourceType(), ResourceId.of(mailId));
        return permissionsService.removePermissionsByResource(mailResource);
    }

    public Mono<Void> updateMailInLookup(String mailId) {
        return mailService.getOrThrow(MailId.of(mailId))
                .flatMap(inquiry -> mailLookupRepo.update(LookupMail.of(
                        inquiry.getId(),
                        inquiry.getVersion(),
                        inquiry.getOrigin(),
                        inquiry.getSender(),
                        inquiry.getSubject(),
                        inquiry.getContent(),
                        inquiry.getReceivedAt(),
                        inquiry.getStatus(),
                        inquiry.getReadAt().orElse(null)
                )));
    }

    public Mono<Void> removeMailFromLookup(String mailId) {
        return mailLookupRepo.remove(MailId.of(mailId));
    }

    private Set<Permission> buildPermissionsForHolderToManageMail(Holder holder, MailId mailId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(mailId.getValue()));

        var readMail = Permission.builder()
                .holder(holder)
                .isAllowedTo(READ)
                .on(resource);
        var markMailAsRead = Permission.builder()
                .holder(holder)
                .isAllowedTo(MARK_AS_READ)
                .on(resource);
        var markMailAsUnread = Permission.builder()
                .holder(holder)
                .isAllowedTo(MARK_AS_UNREAD)
                .on(resource);
        var deleteMail = Permission.builder()
                .holder(holder)
                .isAllowedTo(DELETE)
                .on(resource);

        return Set.of(readMail, markMailAsRead, markMailAsUnread, deleteMail);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable MailId mailId) {
        Permission permission = toPermission(agent, action, mailId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable MailId mailId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(mailId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(mailId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Holder toHolder(Agent agent) {
        if (agent.isSystem()) {
            return Holder.group(HolderId.system());
        } else if (agent.isAnonymous()) {
            return Holder.group(HolderId.anonymous());
        } else {
            return Holder.user(HolderId.of(agent.getId().getValue()));
        }
    }

    private ResourceType getResourceType() {
        return ResourceType.of("MAIL");
    }

}
