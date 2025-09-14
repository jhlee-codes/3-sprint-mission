package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "receiverId", expression = "java(notification.getReceiver().getId())")
    NotificationDto toDto(Notification notification);
}
