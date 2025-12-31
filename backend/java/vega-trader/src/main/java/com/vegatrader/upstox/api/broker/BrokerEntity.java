package com.vegatrader.upstox.api.broker;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for brokers table.
 * 
 * @since 4.2.0
 */
@Entity
@Table(name = "brokers")
public class BrokerEntity {

    @Id
    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "api_type", nullable = false, length = 16)
    private String apiType;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Column(name = "config_json")
    private String configJson;

    @Column(name = "api_base_url")
    private String apiBaseUrl;

    @Column(name = "websocket_url")
    private String websocketUrl;

    @Column(name = "auth_type", length = 16)
    private String authType = "OAUTH2";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BrokerEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Broker toRecord() {
        return new Broker(brokerId, name, apiType, Boolean.TRUE.equals(enabled),
                priority, apiBaseUrl, websocketUrl, authType, null);
    }
}
