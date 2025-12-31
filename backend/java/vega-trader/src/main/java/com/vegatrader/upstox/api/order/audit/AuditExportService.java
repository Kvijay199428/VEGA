package com.vegatrader.upstox.api.order.audit;

import com.vegatrader.upstox.api.order.entity.AuditEventEntity;
import com.vegatrader.upstox.api.order.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.*;

/**
 * Audit Export Service for SEBI compliance.
 * Per order-mgmt/b2.md section 6.
 * 
 * Supports:
 * - CSV export
 * - PDF export (placeholder)
 * - Regulator Pack (ZIP: CSV + checksum)
 * 
 * @since 4.9.0
 */
@Service
public class AuditExportService {

    private static final Logger logger = LoggerFactory.getLogger(AuditExportService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditEventRepository auditEventRepository;

    public AuditExportService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Export audit events to CSV.
     */
    public ExportResult exportToCSV(ExportRequest request) throws IOException {
        logger.info("Exporting audit events to CSV for user {} from {} to {}",
                request.userId(), request.startDate(), request.endDate());

        Instant start = request.startDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = request.endDate().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Page<AuditEventEntity> events = auditEventRepository.findForExport(
                request.userId(), start, end, PageRequest.of(0, 10000));

        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("event_id,order_id,user_id,event_type,previous_state,new_state,source,created_at\n");

        // Data rows
        for (AuditEventEntity event : events) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    event.getEventId(),
                    event.getOrderId(),
                    event.getUserId(),
                    event.getEventType(),
                    event.getPreviousState() != null ? event.getPreviousState() : "",
                    event.getNewState() != null ? event.getNewState() : "",
                    event.getSource(),
                    TIMESTAMP_FORMAT.format(event.getCreatedAt().atZone(ZoneId.systemDefault()))));
        }

        String filename = String.format("audit_export_%s_%s_%s.csv",
                request.userId(),
                DATE_FORMAT.format(request.startDate()),
                DATE_FORMAT.format(request.endDate()));

        Path exportPath = Paths.get(System.getProperty("java.io.tmpdir"), filename);
        Files.writeString(exportPath, csv.toString(), StandardCharsets.UTF_8);

        logger.info("CSV export complete: {} records to {}", events.getTotalElements(), exportPath);

        return new ExportResult(
                filename,
                exportPath.toString(),
                "CSV",
                events.getTotalElements(),
                Files.size(exportPath),
                calculateChecksum(csv.toString()));
    }

    /**
     * Export audit events to PDF (placeholder).
     */
    public ExportResult exportToPDF(ExportRequest request) {
        logger.info("PDF export requested for user {}", request.userId());

        // TODO: Implement PDF generation using iText or similar
        // For now, return placeholder

        return new ExportResult(
                "audit_export.pdf",
                "/tmp/audit_export.pdf",
                "PDF",
                0,
                0,
                "NOT_IMPLEMENTED");
    }

    /**
     * Export Regulator Pack (ZIP with CSV and checksum).
     */
    public ExportResult exportRegulatorPack(ExportRequest request) throws IOException {
        logger.info("Exporting regulator pack for user {}", request.userId());

        // Generate CSV first
        ExportResult csvResult = exportToCSV(request);

        // Create ZIP
        String zipFilename = String.format("regulator_pack_%s_%s_%s.zip",
                request.userId(),
                DATE_FORMAT.format(request.startDate()),
                DATE_FORMAT.format(request.endDate()));

        Path zipPath = Paths.get(System.getProperty("java.io.tmpdir"), zipFilename);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            // Add CSV file
            addToZip(zos, csvResult.filePath(), csvResult.filename());

            // Add checksum file
            String checksumContent = String.format("%s  %s\n", csvResult.checksum(), csvResult.filename());
            ZipEntry checksumEntry = new ZipEntry("checksum.sha256");
            zos.putNextEntry(checksumEntry);
            zos.write(checksumContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Add metadata file
            String metadata = String.format("""
                    Export Metadata
                    ===============
                    User ID: %s
                    Start Date: %s
                    End Date: %s
                    Record Count: %d
                    Generated: %s
                    Checksum Algorithm: SHA-256
                    """,
                    request.userId(),
                    request.startDate(),
                    request.endDate(),
                    csvResult.recordCount(),
                    Instant.now().toString());
            ZipEntry metaEntry = new ZipEntry("metadata.txt");
            zos.putNextEntry(metaEntry);
            zos.write(metadata.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        logger.info("Regulator pack export complete: {}", zipPath);

        return new ExportResult(
                zipFilename,
                zipPath.toString(),
                "REGULATOR_PACK",
                csvResult.recordCount(),
                Files.size(zipPath),
                calculateChecksum(Files.readString(zipPath)));
    }

    /**
     * Get export history (for admin).
     */
    public List<ExportRecord> getExportHistory(String userId, int limit) {
        // TODO: Track export history in DB
        return List.of();
    }

    private void addToZip(ZipOutputStream zos, String filePath, String entryName) throws IOException {
        Path path = Paths.get(filePath);
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(path, zos);
        zos.closeEntry();
    }

    private String calculateChecksum(String content) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    /**
     * Export request.
     */
    public record ExportRequest(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String format // CSV, PDF, REGULATOR_PACK
    ) {
    }

    /**
     * Export result.
     */
    public record ExportResult(
            String filename,
            String filePath,
            String format,
            long recordCount,
            long fileSizeBytes,
            String checksum) {
    }

    /**
     * Export history record.
     */
    public record ExportRecord(
            String exportId,
            String userId,
            String format,
            LocalDate startDate,
            LocalDate endDate,
            Instant exportedAt,
            long recordCount) {
    }
}
