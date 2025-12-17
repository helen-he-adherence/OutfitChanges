package com.example.outfitchanges.ui.virtual.model;

import com.google.gson.annotations.SerializedName;

public class VirtualTryOnResponse {
    private boolean success;
    private String message;
    private String status; // pending, processing, completed
    
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("result_url")
    private String resultUrl; // 当status为completed时返回

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }
}

