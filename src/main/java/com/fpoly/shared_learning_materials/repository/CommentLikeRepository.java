package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.CommentLike;
import com.fpoly.shared_learning_materials.domain.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

    @Query("SELECT cl FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    Optional<CommentLike> findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.likeType = 'LIKE'")
    Long countLikesByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.likeType = 'DISLIKE'")
    Long countDislikesByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT cl FROM CommentLike cl WHERE cl.comment.id = :commentId")
    List<CommentLike> findAllByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT cl FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId")
    List<CommentLike> findByCommentIdAndUserIdList(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId AND cl.likeType = 'LIKE'")
    Boolean existsByCommentIdAndUserIdAndLikeType(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.user.id = :userId AND cl.likeType = 'DISLIKE'")
    Boolean existsByCommentIdAndUserIdAndDislikeType(@Param("commentId") Long commentId, @Param("userId") Long userId);
}