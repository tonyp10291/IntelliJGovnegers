package kr.co.govengers.service;

import kr.co.govengers.entity.Notice;
import kr.co.govengers.repository.NTRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NTSvc {

    private final NTRepo ntRepo;

    public NTSvc(NTRepo ntRepo) {
        this.ntRepo = ntRepo;
    }

    public List<Notice> getAllNotices() {
        return ntRepo.findAll();
    }
}
