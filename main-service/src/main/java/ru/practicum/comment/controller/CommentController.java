package ru.practicum.comment.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable @Positive Long userId,
                                 @PathVariable @Positive Long eventId,
                                 @RequestBody @Valid NewCommentDto newCommentDto) {

        log.info(String.format("Запрос на создание нового комментария %s пользователем %d к событию %d", newCommentDto.toString(), userId, eventId));
        return commentService.addComment(userId, eventId, newCommentDto);
    }

    @GetMapping("/events/{eventId}/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable @Positive Long userId,
                                     @PathVariable @Positive Long eventId,
                                     @PathVariable @Positive Long commentId) {

        log.info("Запрос на просмотр комментария по id %d к событию %d", commentId, eventId);
        return commentService.getCommentById(userId, eventId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getUserComments(@PathVariable @Positive Long userId,
                                            @PathVariable @Positive Long eventId) {

        log.info(String.format("Просмотр всех комментариев к событию %d", eventId));
        return commentService.getAllEventComments(userId, eventId);
    }

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(@PathVariable @Positive Long userId) {

        log.info(String.format("Просмотр всех комментариев пользователя %d", userId));
        return commentService.getAllUserComments(userId);
    }

    @PatchMapping("/events/{eventId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId,
                                    @PathVariable @Positive Long commentId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {

        log.info(String.format("Запрос на адейт комментария %d", commentId));
        return commentService.updateComment(userId, eventId, commentId, newCommentDto);
    }

    @DeleteMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long eventId,
                              @PathVariable @Positive Long commentId) {

        log.info(String.format("Запрос на удаление комментария %d", commentId));
        commentService.deleteComment(userId, eventId, commentId);
    }
}