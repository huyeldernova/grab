package com.example.api.repository.jpa;

import com.example.api.common.FileStatus;
import com.example.api.common.TargetType;
import com.example.api.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileRecord, Long>{

    //Cleanup job dùng — tìm TEMP files quá hạn
    List<FileRecord> findByStatusAndCreatedAtBefore(FileStatus status, LocalDateTime cutoff);

    // Lấy files theo target — ví dụ: tất cả ảnh CMND của driver X
    List<FileRecord> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    // Lấy files theo uploader — kiểm tra ownership
    List<FileRecord> findByIdInAndUploaderId(List<Long> ids, Long uploaderId);
}
