package ru.practicum.user.service;

import ru.practicum.user.model.dto.NewUserRequestDto;
import ru.practicum.user.model.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto addUser(NewUserRequestDto body);

    void deleteUserById(Long userId);
}
