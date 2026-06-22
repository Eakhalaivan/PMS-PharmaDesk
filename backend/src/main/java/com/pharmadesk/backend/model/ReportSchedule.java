package com.pharmadesk.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_schedules")
@SQLDelete(sql = "UPDATE report_schedules SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class ReportSchedule extends BaseEntity {

    @Column(name = "schedule_name", nullable = false)
    private String scheduleName;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "report_category")
    private String reportCategory;

    // DAILY, WEEKLY, FORTNIGHTLY, MONTHLY, CUSTOM
    @Column(name = "frequency", nullable = false)
    private String frequency;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "delivery_time")
    private String deliveryTime; // HH:mm

    // EMAIL, WHATSAPP, BOTH
    @Column(name = "channels")
    private String channels;

    @Column(name = "email_recipients", length = 1000)
    private String emailRecipients; // comma-separated

    @Column(name = "whatsapp_numbers", length = 500)
    private String whatsappNumbers; // comma-separated

    // PDF, EXCEL, BOTH
    @Column(name = "file_formats")
    private String fileFormats;

    @Column(name = "report_params", length = 2000)
    private String reportParams; // JSON string of saved filter params

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    // SENT, FAILED, PENDING
    @Column(name = "last_sent_status")
    private String lastSentStatus;

    @Column(name = "last_sent_error", length = 500)
    private String lastSentError;

    // Getters and Setters
    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getReportCategory() { return reportCategory; }
    public void setReportCategory(String reportCategory) { this.reportCategory = reportCategory; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public String getChannels() { return channels; }
    public void setChannels(String channels) { this.channels = channels; }
    public String getEmailRecipients() { return emailRecipients; }
    public void setEmailRecipients(String emailRecipients) { this.emailRecipients = emailRecipients; }
    public String getWhatsappNumbers() { return whatsappNumbers; }
    public void setWhatsappNumbers(String whatsappNumbers) { this.whatsappNumbers = whatsappNumbers; }
    public String getFileFormats() { return fileFormats; }
    public void setFileFormats(String fileFormats) { this.fileFormats = fileFormats; }
    public String getReportParams() { return reportParams; }
    public void setReportParams(String reportParams) { this.reportParams = reportParams; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
    public String getLastSentStatus() { return lastSentStatus; }
    public void setLastSentStatus(String lastSentStatus) { this.lastSentStatus = lastSentStatus; }
    public String getLastSentError() { return lastSentError; }
    public void setLastSentError(String lastSentError) { this.lastSentError = lastSentError; }
}
