package com.example.textsearchtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @PostMapping
    public ResponseEntity<?> addToHistory(@RequestBody Map<String, String> body) {
        String term = body.get("term");
        historyService.addToHistory(term);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<HistoryService.SearchEntry[]> getHistory(@RequestParam String sortBy) {
        HistoryService.SearchEntry[] history = historyService.getSortedHistory(sortBy);
        return ResponseEntity.ok(history);
    }
}