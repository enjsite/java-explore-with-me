package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.dto.CommentDto;
import ru.practicum.comment.model.dto.NewCommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    Comment newCommentDtoToComment(NewCommentDto newCommentDto);

    CommentDto commentToCommentDto(Comment comment);
}