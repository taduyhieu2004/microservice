package com.taskflow.notification.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdatePreferenceRequest {
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Map<String, Boolean> perTypeSettings;
}
