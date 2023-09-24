package ru.practicum.comment.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.CommentUpdateDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCommentController {

    CommentService commentService;

    @GetMapping
    public List<CommentDto> getAllComments(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {

        log.info("Запрос на просмотр комментариев администратором.", from, size);
        return commentService.getAllComments(from, size);
    }

    @PatchMapping
    public List<CommentDto> moderateComments(@RequestBody CommentUpdateDto commentUpdateDto) {

        log.info("Модерация комментариев администратором.", commentUpdateDto);
        return commentService.moderateComments(commentUpdateDto);
    }
}