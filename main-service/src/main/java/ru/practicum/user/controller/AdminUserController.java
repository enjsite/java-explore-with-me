package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.model.dto.NewUserRequestDto;
import ru.practicum.user.model.dto.UserDto;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addAdminUser(@RequestBody @Valid NewUserRequestDto newUserRequestDto) {

        log.info(String.format("Добавление нового пользователя %s", newUserRequestDto));
        return userService.addUser(newUserRequestDto);
    }

    @GetMapping
    public List<UserDto> getAdminUsers(@RequestParam(name = "ids", required = false) List<Long> ids,
                                       @RequestParam(name = "from", defaultValue = "0", required = false) Integer from,
                                       @RequestParam(name = "size", defaultValue = "10", required = false) Integer size) {

        log.info("Получение информации о пользователях.");
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdminUser(@PathVariable Long userId) {

        log.info(String.format("Удаление пользователя с id %d", userId));
        userService.deleteUserById(userId);
    }
}