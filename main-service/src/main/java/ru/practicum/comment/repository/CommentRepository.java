package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByUserIdAndStatus(Long userId, CommentStatus status);

    List<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status);

    void deleteById(Long commentId);

    List<Comment> findAllByIdInAndStatus(List<Long> commentIds, CommentStatus status);

    boolean existsByIdAndUserIdAndEventIdAndStatus(Long commentId, Long userId, Long eventId, CommentStatus published);
}