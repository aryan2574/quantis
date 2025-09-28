package com.quantis.market_data.controller;

import com.quantis.market_data.model.AssetType;
import com.quantis.market_data.model.Symbol;
import com.quantis.market_data.service.SymbolDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Symbol Controller
 * 
 * REST endpoints for symbol management and population
 */
@RestController
@RequestMapping("/api/symbols")
@CrossOrigin(origins = "*")
public class SymbolController {
    
    @Autowired
    private SymbolDataService symbolDataService;
    
    /**
     * Get all symbols for a specific asset type
     */
    @GetMapping
    public ResponseEntity<List<Symbol>> getSymbols(@RequestParam AssetType assetType) {
        try {
            List<Symbol> symbols = symbolDataService.getSymbolsForAssetType(assetType);
            return ResponseEntity.ok(symbols);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all symbols across all asset types
     */
    @GetMapping("/all")
    public ResponseEntity<Map<AssetType, List<Symbol>>> getAllSymbols() {
        try {
            Map<AssetType, List<Symbol>> allSymbols = Map.of();
            for (AssetType assetType : AssetType.values()) {
                List<Symbol> symbols = symbolDataService.getSymbolsForAssetType(assetType);
                allSymbols.put(assetType, symbols);
            }
            return ResponseEntity.ok(allSymbols);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Populate symbols for a specific asset type
     */
    @PostMapping("/populate")
    public ResponseEntity<String> populateSymbols(@RequestParam AssetType assetType) {
        try {
            symbolDataService.populateSymbolsForAssetType(assetType);
            return ResponseEntity.ok("Symbols populated successfully for " + assetType);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to populate symbols: " + e.getMessage());
        }
    }
    
    /**
     * Populate symbols for all asset types
     */
    @PostMapping("/populate-all")
    public ResponseEntity<String> populateAllSymbols() {
        try {
            symbolDataService.populateAllSymbols();
            return ResponseEntity.ok("All symbols populated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to populate symbols: " + e.getMessage());
        }
    }
    
    /**
     * Get symbol count by asset type
     */
    @GetMapping("/count")
    public ResponseEntity<Map<AssetType, Integer>> getSymbolCounts() {
        try {
            Map<AssetType, Integer> counts = Map.of();
            for (AssetType assetType : AssetType.values()) {
                List<Symbol> symbols = symbolDataService.getSymbolsForAssetType(assetType);
                counts.put(assetType, symbols.size());
            }
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Search symbols by pattern
     */
    @GetMapping("/search")
    public ResponseEntity<List<Symbol>> searchSymbols(
            @RequestParam String pattern,
            @RequestParam(required = false) AssetType assetType) {
        try {
            List<Symbol> results = List.of();
            
            if (assetType != null) {
                List<Symbol> symbols = symbolDataService.getSymbolsForAssetType(assetType);
                results = symbols.stream()
                    .filter(symbol -> symbol.getSymbol().toUpperCase().contains(pattern.toUpperCase()))
                    .toList();
            } else {
                // Search across all asset types
                for (AssetType at : AssetType.values()) {
                    List<Symbol> symbols = symbolDataService.getSymbolsForAssetType(at);
                    List<Symbol> matches = symbols.stream()
                        .filter(symbol -> symbol.getSymbol().toUpperCase().contains(pattern.toUpperCase()))
                        .toList();
                    results.addAll(matches);
                }
            }
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get symbol details
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Symbol> getSymbolDetails(@PathVariable String symbol) {
        try {
            // Search for symbol across all asset types
            for (AssetType assetType : AssetType.values()) {
                List<Symbol> symbols = symbolDataService.getSymbolsForAssetType(assetType);
                Symbol found = symbols.stream()
                    .filter(s -> s.getSymbol().equals(symbol))
                    .findFirst()
                    .orElse(null);
                
                if (found != null) {
                    return ResponseEntity.ok(found);
                }
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
