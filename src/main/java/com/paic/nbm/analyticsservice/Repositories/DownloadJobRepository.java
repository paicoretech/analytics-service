package com.paic.nbm.analyticsservice.Repositories;

import com.paic.nbm.analyticsservice.Entities.DownloadJob;
import com.paic.nbm.analyticsservice.Entities.DownloadJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DownloadJobRepository extends JpaRepository<DownloadJob, Long> {
    Optional<DownloadJob> findByJobId(UUID jobId);
    List<DownloadJob> findByStatusIn(List<DownloadJobStatus> statusList);
    @Query("SELECT j FROM download_manager j " +
            "WHERE (:status IS NULL OR j.status = :status) " +
            "AND j.status <> 'DELETED' " +
            "AND (cast(:startDate as timestamp) IS NULL OR j.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR j.createdAt <= :endDate) " +
            "ORDER BY j.createdAt DESC")
    Page<DownloadJob> findDownloadsWithFilters(
                @Param("status") DownloadJobStatus status,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate,
                Pageable pageable
        );
}
