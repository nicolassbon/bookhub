package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationEntityMapper {

  Notification toDomain(NotificationEntity entity);

  NotificationEntity toEntity(Notification domain);
}
