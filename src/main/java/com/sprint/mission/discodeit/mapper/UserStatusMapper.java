package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.UserStatus.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserStatusMapper {

    @Mapping(target = "userId", expression = "java(userStatus.getUser().getId())")
    UserStatusDto toDto(UserStatus userStatus);
}
