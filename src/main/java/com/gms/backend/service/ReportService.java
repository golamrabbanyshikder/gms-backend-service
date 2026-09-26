package com.gms.backend.service;

import com.gms.backend.entity.Report;
import com.gms.backend.exception.ReportNotFoundException;
import com.gms.backend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public Report createReport(Report report) {
        report.setCreatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    public Report updateReport(Long reportId, Report reportDetails) {
        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + reportId));
        
        report.setReportType(reportDetails.getReportType());
        report.setFileName(reportDetails.getFileName());
        report.setUpdatedAt(LocalDateTime.now());
        
        return reportRepository.save(report);
    }

    public Report getReportById(Long reportId) {
        return reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + reportId));
    }

    public List<Report> getReportsByPatientId(Long patientId) {
        return reportRepository.findByPatientId(patientId);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public void deleteReport(Long reportId) {
        Report report = reportRepository.findByReportId(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + reportId));
        reportRepository.delete(report);
    }
}
