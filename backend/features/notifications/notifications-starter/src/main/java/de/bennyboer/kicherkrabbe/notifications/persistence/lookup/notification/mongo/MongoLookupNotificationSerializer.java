package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import de.bennyboer.kicherkrabbe.notifications.channel.mail.EMail;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.Telegram;
import de.bennyboer.kicherkrabbe.notifications.channel.telegram.TelegramChatId;
import de.bennyboer.kicherkrabbe.notifications.notification.*;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotification;

import java.util.Set;
import java.util.stream.Collectors;

public class MongoLookupNotificationSerializer
        implements ReadModelSerializer<LookupNotification, MongoLookupNotification> {

    @Override
    public MongoLookupNotification serialize(LookupNotification readModel) {
        var result = new MongoLookupNotification();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.origin = serializeOrigin(readModel.getOrigin());
        result.target = serializeTarget(readModel.getTarget());
        result.channels = serializeChannels(readModel.getChannels());
        result.title = readModel.getTitle().getValue();
        result.message = readModel.getMessage().getValue();
        result.sentAt = readModel.getSentAt();

        return result;
    }

    @Override
    public LookupNotification deserialize(MongoLookupNotification serialized) {
        var id = NotificationId.of(serialized.id);
        var version = Version.of(serialized.version);
        var origin = deserializeOrigin(serialized.origin);
        var target = deserializeTarget(serialized.target);
        var channels = deserializeChannels(serialized.channels);
        var title = Title.of(serialized.title);
        var message = Message.of(serialized.message);
        var sentAt = serialized.sentAt;

        return LookupNotification.of(
                id,
                version,
                origin,
                target,
                channels,
                title,
                message,
                sentAt
        );
    }

    private Set<MongoChannel> serializeChannels(Set<Channel> channels) {
        return channels.stream()
                .map(this::serializeChannel)
                .collect(Collectors.toSet());
    }

    private Set<Channel> deserializeChannels(Set<MongoChannel> channels) {
        return channels.stream()
                .map(this::deserializeChannel)
                .collect(Collectors.toSet());
    }

    private MongoChannel serializeChannel(Channel channel) {
        var result = new MongoChannel();
        result.type = serializeChannelType(channel.getType());
        result.mail = channel.getMail()
                .map(EMail::getValue)
                .orElse(null);
        result.telegram = channel.getTelegram()
                .map(this::serializeTelegram)
                .orElse(null);
        return result;
    }

    private Channel deserializeChannel(MongoChannel channel) {
        var type = deserializeChannelType(channel.type);

        return switch (type) {
            case EMAIL -> Channel.mail(EMail.of(channel.mail));
            case TELEGRAM -> Channel.telegram(deserializeTelegram(channel.telegram));
            case UNKNOWN -> throw new IllegalArgumentException("Unknown channel type");
        };
    }

    private MongoTelegram serializeTelegram(Telegram telegram) {
        var result = new MongoTelegram();
        result.chatId = telegram.getChatId().getValue();
        return result;
    }

    private Telegram deserializeTelegram(MongoTelegram telegram) {
        return Telegram.of(TelegramChatId.of(telegram.chatId));
    }

    private MongoChannelType serializeChannelType(ChannelType type) {
        return switch (type) {
            case EMAIL -> MongoChannelType.EMAIL;
            case TELEGRAM -> MongoChannelType.TELEGRAM;
            case UNKNOWN -> throw new IllegalArgumentException("Unknown channel type");
        };
    }

    private ChannelType deserializeChannelType(MongoChannelType type) {
        return switch (type) {
            case EMAIL -> ChannelType.EMAIL;
            case TELEGRAM -> ChannelType.TELEGRAM;
        };
    }

    private MongoTarget serializeTarget(Target target) {
        var result = new MongoTarget();
        result.type = serializeTargetType(target.getType());
        result.id = target.getId().getValue();
        return result;
    }

    private Target deserializeTarget(MongoTarget target) {
        var type = deserializeTargetType(target.type);
        var id = TargetId.of(target.id);

        return Target.of(type, id);
    }

    private MongoTargetType serializeTargetType(TargetType type) {
        return switch (type) {
            case SYSTEM -> MongoTargetType.SYSTEM;
        };
    }

    private TargetType deserializeTargetType(MongoTargetType type) {
        return switch (type) {
            case SYSTEM -> TargetType.SYSTEM;
        };
    }

    private MongoOrigin serializeOrigin(Origin origin) {
        var result = new MongoOrigin();
        result.type = serializeOriginType(origin.getType());
        result.id = origin.getId().getValue();
        return result;
    }

    private Origin deserializeOrigin(MongoOrigin origin) {
        var type = deserializeOriginType(origin.type);
        var id = OriginId.of(origin.id);

        return Origin.of(type, id);
    }

    private MongoOriginType serializeOriginType(OriginType type) {
        return switch (type) {
            case MAIL -> MongoOriginType.MAIL;
        };
    }

    private OriginType deserializeOriginType(MongoOriginType type) {
        return switch (type) {
            case MAIL -> OriginType.MAIL;
        };
    }

}
