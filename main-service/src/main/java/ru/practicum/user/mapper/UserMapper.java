package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;
import ru.practicum.user.model.dto.NewUserRequestDto;
import ru.practicum.user.model.dto.UserDto;
import ru.practicum.user.model.dto.UserShortDto;
import ru.practicum.user.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    User userDtoToUser(NewUserRequestDto newUserRequestDto);

    UserDto userToUserDto(User user);

    UserShortDto userToUserShortDto(User user);
}