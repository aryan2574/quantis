package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Watchlist Response Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse {
    private boolean success;
    private List<String> symbols;
    private String message;
}
