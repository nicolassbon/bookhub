package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.YearlyGoal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface YearlyGoalEntityMapper {

  YearlyGoal toDomain(YearlyGoalEntity entity);

  YearlyGoalEntity toEntity(YearlyGoal domain);
}
