package ru.practicum.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.model.enums.EventStatus;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.request.model.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {

    EventRepository eventRepository;
    RequestRepository requestRepository;
    UserRepository userRepository;
    RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new RequestConflictException("Такой запрос на участие уже существует.");
        }

        User requester = userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Пользователь %d не найден", userId));
        });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Событие %d не найдено", eventId));
        });

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RequestConflictException("Мероприятие не опубликовано, зарегистрироваться нельзя.");
        }

        if (Objects.equals(userId, event.getInitiator().getId())) {
            throw new RequestConflictException("Организаторам мероприятий не разрешается запрашивать участие в собственных мероприятиях.");
        }

        if ((event.getParticipantLimit() != 0L) && (event.getConfirmedRequests() >= event.getParticipantLimit())) {
            throw new RequestConflictException("Достигнут лимит участников.");
        }

        ParticipationRequest requestToSave = new ParticipationRequest(requester, event,
                !event.getRequestModeration() || event.getParticipantLimit() == 0L ?
                        ParticipationStatus.CONFIRMED : ParticipationStatus.PENDING, LocalDateTime.now());

        ParticipationRequest participationRequest = requestRepository.save(requestToSave);

        if (participationRequest.getStatus() == ParticipationStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return requestMapper.requestToDto(participationRequest);
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {

        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(() -> {
            throw new RequestConflictException(String.format("Запрос на участие %d не найден", requestId));
        });

        if (request.getStatus() == ParticipationStatus.CONFIRMED) {
            throw new RequestConflictException(String.format("Запрос на участие %d уже подтвержден", requestId));
        }

        request.setStatus(ParticipationStatus.CANCELED);

        Long eventId = request.getEvent().getId();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Событие %d не найдено", eventId));
        });

        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);

        request = requestRepository.save(request);
        return requestMapper.requestToDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);

        if (!requests.isEmpty()) {
            return requests.stream()
                    .map(requestMapper::requestToDto)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {

        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId);

        if (!requests.isEmpty()) {
            return requests.stream()
                    .map(requestMapper::requestToDto)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              @Valid EventRequestStatusUpdateRequest requestsUpdate) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> {
            throw new ObjectNotFoundException("Событие не найдено");
        });

        if (!event.getInitiator().getId().equals(userId)) {
            throw new RequestConflictException(String.format("Доступ запрещен. Пользователь %d не является организатором", userId));
        }

        List<ParticipationRequest> participationRequests = requestRepository.findAllByIdInAndAndEventId(requestsUpdate.getRequestIds(), eventId);

        if (participationRequests.size() != requestsUpdate.getRequestIds().size()) {
            throw new ObjectNotFoundException("Некорректный id.");
        }

        for (ParticipationRequest request : participationRequests) {
            if (!request.getStatus().equals(ParticipationStatus.PENDING)) {
                throw new RequestConflictException("Только запросы со статусом PENDING могут быть приняты или отклонены.");
            }
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (requestsUpdate.getStatus() == EventStatus.REJECTED) {
            participationRequests.forEach(participationRequest -> {
                participationRequest.setStatus(ParticipationStatus.REJECTED);
                requestRepository.save(participationRequest);
                rejectedRequests.add(requestMapper.requestToDto(participationRequest));
            });
            return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return new EventRequestStatusUpdateResult(
                    participationRequests.stream().map(requestMapper::requestToDto).collect(Collectors.toList()),
                    new ArrayList<>()
            );
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new RequestConflictException(String.format("Не удалось принять запрос. Достигнуто максимальное количество участников для события %d", eventId));
        }

        participationRequests.forEach(participationRequest -> {
            if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                participationRequest.setStatus(ParticipationStatus.CONFIRMED);
                requestRepository.save(participationRequest);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmedRequests.add(requestMapper.requestToDto(participationRequest));
            } else {
                participationRequest.setStatus(ParticipationStatus.REJECTED);
                requestRepository.save(participationRequest);
                rejectedRequests.add(requestMapper.requestToDto(participationRequest));
            }
        });

        if (!confirmedRequests.isEmpty()) {
            eventRepository.save(event);
        }

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }
}