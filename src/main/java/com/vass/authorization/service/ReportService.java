package com.vass.authorization.service;

import com.vass.authorization.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public String downloadReport(Long reportId) {
        if (reportId == null || reportId <= 0 || reportId > 2) {
            throw new ResourceNotFoundException("Reporte no encontrado");
        }
        return "Contenido del reporte " + reportId;
    }
}
