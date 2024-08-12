package com.example.textsearchtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchEngine searchEngine;

    @PostMapping("/index")
    public ResponseEntity<?> indexFolder(@RequestParam String folderPath) {
        try {
            searchEngine.indexFolder(folderPath);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Folder indexed successfully"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchEngine.SearchResult>> search(@RequestParam String q) {
        List<SearchEngine.SearchResult> results = searchEngine.search(q);
        return ResponseEntity.ok(results);
    }
}