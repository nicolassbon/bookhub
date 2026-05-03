package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.Review;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewEntityMapper {

  Review toDomain(ReviewEntity entity);

  ReviewEntity toEntity(Review domain);
}
