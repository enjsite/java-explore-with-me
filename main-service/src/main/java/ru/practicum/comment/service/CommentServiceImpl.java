package ru.practicum.comment.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentUpdateDto;
import ru.practicum.comment.model.dto.NewCommentDto;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    EventRepository eventRepository;
    UserRepository userRepository;
    CommentMapper commentMapper;

    String commentNotFoundErr = "Комментарий %d не найден";
    String eventNotFoundErr = "Событие %d не найдено";
    String userNotFoundErr = "Пользователь %d не найден";
    String commentStatusErr = "Комментарий на модерации.";

    @Override
    public CommentDto getCommentById(Long userId, Long eventId, Long commentId) {

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(commentNotFoundErr, commentId));
        });

        if (comment.getStatus().equals(CommentStatus.PENDING)) {
            throw new RequestConflictException(commentStatusErr);
        }

        return commentMapper.commentToCommentDto(comment);
    }

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {

        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(userNotFoundErr, userId));
        });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(eventNotFoundErr, eventId));
        });

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RequestConflictException("Нельзя оставить комментарий, событие не опубликовано.");
        }

        Comment comment = commentMapper.newCommentDtoToComment(newCommentDto);

        comment.setCreatedOn(LocalDateTime.now());
        comment.setEvent(event);
        comment.setUser(user);
        comment.setStatus(CommentStatus.PENDING);

        comment = commentRepository.save(comment);

        return commentMapper.commentToCommentDto(comment);
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto) {

        userRepository.findById(userId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(userNotFoundErr, userId));
        });

        eventRepository.findById(eventId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(eventNotFoundErr, eventId));
        });

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format(commentNotFoundErr, commentId));
        });

        if (comment.getStatus().equals(CommentStatus.PENDING)) {
            throw new RequestConflictException(commentStatusErr);
        }

        comment.setCreatedOn(LocalDateTime.now());
        comment.setText(newCommentDto.getText());
        comment.setStatus(CommentStatus.PENDING);

        comment = commentRepository.save(comment);

        return commentMapper.commentToCommentDto(comment);
    }

    @Override
    public List<CommentDto> getAllUserComments(Long userId) {

        List<Comment> comments = commentRepository.findAllByUserIdAndStatus(userId, CommentStatus.PUBLISHED);

        if (!comments.isEmpty()) {
            return comments.stream()
                    .map(commentMapper::commentToCommentDto)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public List<CommentDto> getAllEventComments(Long userId, Long eventId) {

        List<Comment> comments = commentRepository.findAllByEventIdAndStatus(eventId, CommentStatus.PUBLISHED);

        if (!comments.isEmpty()) {
            return comments.stream()
                    .map(commentMapper::commentToCommentDto)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long eventId, Long commentId) {

        if (commentRepository.existsByIdAndUserIdAndEventIdAndStatus(commentId, userId, eventId, CommentStatus.PUBLISHED)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ObjectNotFoundException(String.format(commentNotFoundErr, commentId));
        }
    }

    @Override
    public List<CommentDto> getAllComments(Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Page<Comment> comments = commentRepository.findAll(pageable);

        return comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> moderateComments(CommentUpdateDto commentUpdateDto) {

        List<Comment> comments = commentRepository.findAllByIdInAndStatus(commentUpdateDto.getCommentIds(), CommentStatus.PENDING);

        if (comments.size() != commentUpdateDto.getCommentIds().size()) {
            throw new ObjectNotFoundException("Некорректный commentId.");
        }

        switch (commentUpdateDto.getStatus()) {
            case PUBLISHED:
                comments.forEach(comment -> comment.setStatus(CommentStatus.PUBLISHED));
                comments = commentRepository.saveAll(comments);
                return comments.stream()
                        .map(commentMapper::commentToCommentDto)
                        .collect(Collectors.toList());
            case REJECTED:
                comments.forEach(comment -> commentRepository.deleteAllById(commentUpdateDto.getCommentIds()));
                return new ArrayList<>();
            default:
                throw new IncorrectRequestException("Некорректный статус.");
        }
    }
}