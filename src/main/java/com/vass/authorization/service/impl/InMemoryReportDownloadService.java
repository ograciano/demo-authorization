package com.vass.authorization.service.impl;

import com.vass.authorization.service.ReportDownloadService;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;

@Service
public class InMemoryReportDownloadService implements ReportDownloadService {

    @Override
    public byte[] downloadReport(Long reportId) {
        String reportContent = "Report download for id " + reportId;
        return reportContent.getBytes(StandardCharsets.UTF_8);
    }
}
