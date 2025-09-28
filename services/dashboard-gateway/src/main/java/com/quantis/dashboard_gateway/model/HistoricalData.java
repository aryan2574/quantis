package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Historical Data Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalData {
    private String symbol;
    private List<DataPoint> dataPoints;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDateTime timestamp;
        private BigDecimal price;
        private BigDecimal volume;
    }
}
