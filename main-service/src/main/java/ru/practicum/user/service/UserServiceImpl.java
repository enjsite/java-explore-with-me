package ru.practicum.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.SQLConstraintViolationException;
import ru.practicum.user.model.dto.NewUserRequestDto;
import ru.practicum.user.model.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        if (ids == null) {
            return userRepository.findAll(pageable).stream()
                    .map(userMapper::userToUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, pageable).stream()
                    .map(userMapper::userToUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDto addUser(NewUserRequestDto newUserRequestDto) {

        User user = userMapper.userDtoToUser(newUserRequestDto);

        User userDtoToSave;

        try {
            userDtoToSave = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new SQLConstraintViolationException("Имя пользователя или email уже существуют.");
        }

        return userMapper.userToUserDto(userDtoToSave);
    }

    @Override
    public void deleteUserById(Long userId) {

        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException(String.format("Пользователь с id %d не найден.", userId));
        }
    }
}
