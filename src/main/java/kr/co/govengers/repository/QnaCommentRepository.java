package kr.co.govengers.repository;

import kr.co.govengers.entity.QnaComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {
    // created_at 기준 오름차순
    List<QnaComment> findByQna_QidOrderByCreatedAtAsc(Long qid);
}