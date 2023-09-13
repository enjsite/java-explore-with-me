package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;
import ru.practicum.EndpointHitDto;
import ru.practicum.model.Hit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {
    HitMapper INSTANCE = Mappers.getMapper(HitMapper.class);

    @Mapping(source = "app.name", target = "app")
    EndpointHitDto toEndpointHitDto(Hit hit);

    @Mapping(source = "app", target = "app.name")
    Hit toHit(EndpointHitDto endpointHitDto);
}
