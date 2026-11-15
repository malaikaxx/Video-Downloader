package com.streamvault;

import com.streamvault.DownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Long> {
    List<DownloadHistory> findByUsernameIgnoreCaseOrderByDownloadedAtDesc(String username);
}