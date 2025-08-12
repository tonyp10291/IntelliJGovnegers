package kr.co.govengers.repository;

import kr.co.govengers.entity.QnaComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {
    List<QnaComment> findByQna_QidOrderByCreatedAtAsc(Long qid);
}