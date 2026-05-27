package com.paic.nbm.analyticsservice.Entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "download_manager")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID jobId;

    @Enumerated(EnumType.STRING) // <-- Persist enum as String, not ordinal
    @Column(nullable = false)
    private DownloadJobStatus status;

    private String filePath;
    private String fileName;
    private boolean cancelled;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
