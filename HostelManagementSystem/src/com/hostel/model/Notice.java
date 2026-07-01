package com.hostel.model;

import java.sql.Timestamp;

public class Notice {
    private int noticeId, createdBy;
    private Integer targetGirlId;
    private String title, message, targetType, targetGirlName;
    private Timestamp createdDate;

    public Notice() {}

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int n) { this.noticeId = n; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int c) { this.createdBy = c; }
    public Integer getTargetGirlId() { return targetGirlId; }
    public void setTargetGirlId(Integer t) { this.targetGirlId = t; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getMessage() { return message; }
    public void setMessage(String m) { this.message = m; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String t) { this.targetType = t; }
    public String getTargetGirlName() { return targetGirlName; }
    public void setTargetGirlName(String t) { this.targetGirlName = t; }
    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp t) { this.createdDate = t; }
}
