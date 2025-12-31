package com.vegatrader.upstox.api.sectoral.service;

import com.vegatrader.upstox.api.sectoral.entity.*;
import com.vegatrader.upstox.api.sectoral.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads index constituents from NSE CSV feeds.
 * 
 * @since 4.3.0
 */
@Service
public class IndexConstituentLoader {

    private static final Logger logger = LoggerFactory.getLogger(IndexConstituentLoader.class);

    private final IndexMasterRepository indexRepo;
    private final IndexConstituentRepository constituentRepo;
    private final HttpClient httpClient;

    public IndexConstituentLoader(IndexMasterRepository indexRepo, IndexConstituentRepository constituentRepo) {
        this.indexRepo = indexRepo;
        this.constituentRepo = constituentRepo;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Loads all indices that need refresh.
     */
    @Transactional
    public int loadAllIndices() {
        List<IndexMasterEntity> indices = indexRepo.findIndicesNeedingRefresh(LocalDate.now());
        int total = 0;

        for (IndexMasterEntity index : indices) {
            try {
                int count = loadIndex(index);
                total += count;
                logger.info("Loaded {} constituents for {}", count, index.getIndexCode());
            } catch (Exception e) {
                logger.error("Failed to load index {}: {}", index.getIndexCode(), e.getMessage());
            }
        }

        logger.info("Total indices refreshed: {}, constituents loaded: {}", indices.size(), total);
        return total;
    }

    /**
     * Loads constituents for a specific index.
     */
    @Transactional
    public int loadIndex(String indexCode) {
        IndexMasterEntity index = indexRepo.findById(indexCode).orElse(null);
        if (index == null) {
            logger.warn("Index not found: {}", indexCode);
            return 0;
        }
        return loadIndex(index);
    }

    /**
     * Loads constituents from CSV URL.
     */
    @Transactional
    public int loadIndex(IndexMasterEntity index) {
        try {
            String csv = downloadCsv(index.getSourceUrl());
            List<IndexConstituentEntity> constituents = parseCsv(csv, index);

            // Clear existing and insert new
            constituentRepo.deleteByIndexCode(index.getIndexCode());
            constituentRepo.saveAll(constituents);

            // Update index metadata
            indexRepo.updateLastRefreshed(index.getIndexCode(), LocalDate.now(), constituents.size());

            return constituents.size();
        } catch (Exception e) {
            logger.error("Error loading index {}: {}", index.getIndexCode(), e.getMessage());
            return 0;
        }
    }

    private String downloadCsv(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + " for " + url);
        }

        return response.body();
    }

    private List<IndexConstituentEntity> parseCsv(String csv, IndexMasterEntity index) {
        List<IndexConstituentEntity> result = new ArrayList<>();
        String[] lines = csv.split("\n");

        boolean headerFound = false;
        int symbolIdx = -1, companyIdx = -1, industryIdx = -1, isinIdx = -1, weightIdx = -1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            String[] cols = line.split(",");

            if (!headerFound) {
                // Find column indices from header
                for (int i = 0; i < cols.length; i++) {
                    String col = cols[i].trim().toLowerCase();
                    if (col.contains("symbol"))
                        symbolIdx = i;
                    else if (col.contains("company"))
                        companyIdx = i;
                    else if (col.contains("industry"))
                        industryIdx = i;
                    else if (col.contains("isin"))
                        isinIdx = i;
                    else if (col.contains("weight"))
                        weightIdx = i;
                }
                headerFound = symbolIdx >= 0;
                continue;
            }

            if (symbolIdx >= cols.length)
                continue;

            String symbol = cols[symbolIdx].trim().toUpperCase();
            if (symbol.isEmpty() || symbol.equals("SYMBOL"))
                continue;

            IndexConstituentEntity entity = new IndexConstituentEntity();
            entity.setIndexCode(index.getIndexCode());
            entity.setSymbol(symbol);
            entity.setInstrumentKey(index.getExchange() + "_EQ|" + symbol);

            if (companyIdx >= 0 && companyIdx < cols.length) {
                entity.setCompanyName(cols[companyIdx].trim());
            }
            if (industryIdx >= 0 && industryIdx < cols.length) {
                entity.setIndustry(cols[industryIdx].trim());
            }
            if (isinIdx >= 0 && isinIdx < cols.length) {
                entity.setIsin(cols[isinIdx].trim());
            }
            if (weightIdx >= 0 && weightIdx < cols.length) {
                try {
                    entity.setWeight(Double.parseDouble(cols[weightIdx].trim()));
                } catch (NumberFormatException ignored) {
                }
            }

            entity.setEffectiveDate(LocalDate.now());
            result.add(entity);
        }

        return result;
    }
}
