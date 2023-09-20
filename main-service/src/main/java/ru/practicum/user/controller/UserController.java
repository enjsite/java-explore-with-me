package ru.practicum.user.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.model.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.model.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    RequestService requestService;
    EventService eventService;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addUserEvent(@PathVariable Long userId,
                                     @RequestBody @Valid NewEventDto event) {

        log.info(String.format("Добавление нового события {%s} пользователем с id %d", event, userId));
        return eventService.addUserEvent(userId, event);
    }

    @GetMapping("/events")
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {

        log.info(String.format("Получение событий добавленных пользователем с id %d", userId));
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Long userId,
                                         @PathVariable Long eventId) {

        log.info(String.format("Получение информации о событии с id %d", eventId));
        return eventService.getUserEventById(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateUserEventById(@PathVariable @NotNull Long userId,
                                            @PathVariable @NotNull Long eventId,
                                            @RequestBody @Valid EventUpdateDto eventDto) {

        log.info(String.format("Изменение информации о событии с id %d", eventId));
        return eventService.updateUserEventById(userId, eventId, eventDto);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {

        log.info(String.format("Получение информации о заявках пользователя id %d на участие в чужих событиях", userId));
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addUserRequest(@PathVariable Long userId,
                                                  @RequestParam(name = "eventId") Long eventId) {

        log.info(String.format("Добавление запроса от пользователя id %d на участие в событии %d", userId, eventId));
        return requestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto updateUserRequest(@PathVariable Long userId,
                                                     @PathVariable Long requestId) {

        log.info(String.format("Отмена пользователем %d запроса на участие %d", userId, requestId));
        return requestService.cancelParticipationRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getUserEventsRequests(@PathVariable Long userId,
                                                               @PathVariable Long eventId) {

        log.info(String.format("Получение информации о запросах пользователя %d на участие в событии %d", userId, eventId));
        return requestService.getUserEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateUserEventRequests(@PathVariable Long userId,
                                                           @PathVariable Long eventId,
                                                           @RequestBody EventRequestStatusUpdateRequest requestsUpdate) {

        log.info(String.format("Изменение статуса заявки пользователя %d на участие в событии %d", userId, eventId));
        return requestService.updateEventRequests(userId, eventId, requestsUpdate);
    }
}