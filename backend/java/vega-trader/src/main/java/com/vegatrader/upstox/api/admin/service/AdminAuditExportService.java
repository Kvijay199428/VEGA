package com.vegatrader.upstox.api.admin.service;

import com.vegatrader.upstox.api.admin.entity.AdminActionAuditEntity;
import com.vegatrader.upstox.api.admin.repository.AdminActionAuditRepository;
import com.vegatrader.upstox.api.order.entity.OrderAuditEntity;
import com.vegatrader.upstox.api.order.repository.OrderAuditRepository;
import com.vegatrader.upstox.auth.entity.TokenAuditEntity;
import com.vegatrader.upstox.auth.repository.TokenAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Audit Export Service.
 * Handles exporting audit data to CSV/PDF.
 * Per a1.md Section 10.
 * 
 * @since 5.0.0
 */
@Service
public class AdminAuditExportService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuditExportService.class);
    private static final String EXPORT_DIR = "exports/audit";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final AdminActionAuditRepository adminAuditRepo;
    private final OrderAuditRepository orderAuditRepo;
    private final TokenAuditRepository tokenAuditRepo;

    @Autowired
    public AdminAuditExportService(AdminActionAuditRepository adminAuditRepo,
            OrderAuditRepository orderAuditRepo,
            TokenAuditRepository tokenAuditRepo) {
        this.adminAuditRepo = adminAuditRepo;
        this.orderAuditRepo = orderAuditRepo;
        this.tokenAuditRepo = tokenAuditRepo;
    }

    /**
     * Export admin audit to CSV.
     */
    public ExportResult exportAdminAuditCsv(Instant startDate, Instant endDate, String requestedBy) {
        logger.info("Exporting admin audit CSV: {} to {} by {}", startDate, endDate, requestedBy);

        List<AdminActionAuditEntity> data = adminAuditRepo.findByTimeRange(startDate, endDate);
        String filename = generateFilename("admin_audit", "csv");
        Path filePath = Paths.get(EXPORT_DIR, filename);

        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                // Header
                writer.write("AuditId,ActionType,TargetEntity,TargetId,PerformedBy,PerformedAt,Success,Reason\n");

                // Data
                for (AdminActionAuditEntity row : data) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                            row.getAuditId(),
                            escapeCsv(row.getActionType()),
                            escapeCsv(row.getTargetEntity()),
                            escapeCsv(row.getTargetId()),
                            escapeCsv(row.getPerformedBy()),
                            row.getPerformedAt(),
                            row.getSuccess(),
                            escapeCsv(row.getReason())));
                }
            }

            logger.info("Exported {} admin audit records to {}", data.size(), filePath);
            return new ExportResult(true, filePath.toString(), data.size(), "CSV");

        } catch (IOException e) {
            logger.error("Export failed: {}", e.getMessage());
            return new ExportResult(false, null, 0, "CSV");
        }
    }

    /**
     * Export order audit to CSV.
     */
    public ExportResult exportOrderAuditCsv(Instant startDate, Instant endDate, String requestedBy) {
        logger.info("Exporting order audit CSV: {} to {} by {}", startDate, endDate, requestedBy);

        List<OrderAuditEntity> data = orderAuditRepo.findByTimeRange(startDate, endDate);
        String filename = generateFilename("order_audit", "csv");
        Path filePath = Paths.get(EXPORT_DIR, filename);

        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                // Header
                writer.write("AuditId,OrderId,EventType,ActorId,ActorType,BrokerCode,EventTs,LatencyMs\n");

                // Data
                for (OrderAuditEntity row : data) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%d\n",
                            row.getAuditId(),
                            escapeCsv(row.getOrderId()),
                            escapeCsv(row.getEventType()),
                            escapeCsv(row.getActorId()),
                            escapeCsv(row.getActorType()),
                            escapeCsv(row.getBrokerCode()),
                            row.getEventTimestamp(),
                            row.getLatencyMs() != null ? row.getLatencyMs() : 0));
                }
            }

            logger.info("Exported {} order audit records to {}", data.size(), filePath);
            return new ExportResult(true, filePath.toString(), data.size(), "CSV");

        } catch (IOException e) {
            logger.error("Export failed: {}", e.getMessage());
            return new ExportResult(false, null, 0, "CSV");
        }
    }

    /**
     * Export token audit to CSV.
     */
    public ExportResult exportTokenAuditCsv(Instant startDate, Instant endDate, String requestedBy) {
        logger.info("Exporting token audit CSV: {} to {} by {}", startDate, endDate, requestedBy);

        List<TokenAuditEntity> data = tokenAuditRepo.findByTimeRange(startDate, endDate);
        String filename = generateFilename("token_audit", "csv");
        Path filePath = Paths.get(EXPORT_DIR, filename);

        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                // Header
                writer.write("AuditId,ApiName,EventType,ActorId,ActorType,EventTs,IpAddress\n");

                // Data
                for (TokenAuditEntity row : data) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                            row.getAuditId(),
                            escapeCsv(row.getApiName()),
                            escapeCsv(row.getEventType()),
                            escapeCsv(row.getActorId()),
                            escapeCsv(row.getActorType()),
                            row.getEventTimestamp(),
                            escapeCsv(row.getIpAddress())));
                }
            }

            logger.info("Exported {} token audit records to {}", data.size(), filePath);
            return new ExportResult(true, filePath.toString(), data.size(), "CSV");

        } catch (IOException e) {
            logger.error("Export failed: {}", e.getMessage());
            return new ExportResult(false, null, 0, "CSV");
        }
    }

    /**
     * Generate combined audit report.
     */
    public ExportResult generateAuditReport(Instant startDate, Instant endDate, String requestedBy) {
        // Export all three
        ExportResult admin = exportAdminAuditCsv(startDate, endDate, requestedBy);
        ExportResult order = exportOrderAuditCsv(startDate, endDate, requestedBy);
        ExportResult token = exportTokenAuditCsv(startDate, endDate, requestedBy);

        int totalRecords = admin.recordCount() + order.recordCount() + token.recordCount();
        boolean allSuccess = admin.success() && order.success() && token.success();

        return new ExportResult(allSuccess, EXPORT_DIR, totalRecords, "CSV_BUNDLE");
    }

    // === Private Helpers ===

    private String generateFilename(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // === DTOs ===

    public record ExportResult(
            boolean success,
            String filePath,
            int recordCount,
            String format) {
    }
}
