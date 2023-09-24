package ru.practicum.comment.service;

import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentUpdateDto;
import ru.practicum.comment.model.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto getCommentById(Long userId, Long eventId, Long commentId);

    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long eventId, Long commentId);

    List<CommentDto> getAllUserComments(Long userId);

    List<CommentDto> getAllEventComments(Long userId, Long eventId);

    List<CommentDto> getAllComments(Integer from, Integer size);

    List<CommentDto> moderateComments(CommentUpdateDto commentUpdateDto);
}