package com.example.textsearchtool;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Component
public class SearchEngine {
    private BSTree bst;
    private String folderPath;

    public SearchEngine() {
        this.bst = new BSTree();
    }

    public void indexFolder(String folderPath) throws IOException {
        this.folderPath = folderPath;
        File[] files = new File(folderPath).listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile()) {
                    indexFile(file);
                }
            }
        }
    }

    private void indexFile(File file) throws IOException {
        String content = extractFileContent(file);
        String[] words = content.toLowerCase().split("\\W+");

        for (String word : words) {
            if (!word.isEmpty()) {
                bst.insert(word, file.getName());
            }
        }
    }

    private String extractFileContent(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                return new PDFTextStripper().getText(document);
            }
        } else {
            return new String(Files.readAllBytes(file.toPath()));
        }
    }

    public List<SearchResult> search(String query) {
        return searchWord(query.toLowerCase());
    }

    private List<SearchResult> searchWord(String word) {
        Set<BSTree.FileInfo> fileInfos = bst.search(word);
        List<SearchResult> results = new ArrayList<>();
        Set<String> addedFiles = new HashSet<>();

        for (BSTree.FileInfo fileInfo : fileInfos) {
            if (addedFiles.add(fileInfo.fileName)) {
                String snippet = getSnippet(fileInfo.fileName, word);
                if (!snippet.contains("Snippet not available")) {
                    results.add(new SearchResult(
                            fileInfo,
                            snippet,
                            countOccurrencesInFile(word, fileInfo.fileName)
                    ));
                }
            }
        }

        return results;
    }

    private String getSnippet(String fileName, String searchTerm) {
        String content = getFileContent(folderPath + "\\" + fileName);
        int index = content.toLowerCase().indexOf(searchTerm);

        if (index != -1) {
            int start = Math.max(0, index - 50);
            int end = Math.min(content.length(), index + searchTerm.length() + 50);
            return "..." + content.substring(start, end) + "...";
        }
        return "Snippet not available";
    }

    private String getFileContent(String filePath) {
        try {
            return extractFileContent(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private int countOccurrencesInFile(String word, String fileName) {
        Set<BSTree.FileInfo> fileInfos = bst.search(word);
        for (BSTree.FileInfo fileInfo : fileInfos) {
            if (fileInfo.fileName.equals(fileName)) {
                String content = getFileContent(folderPath + "\\" + fileName).toLowerCase();
                int lastIndex = 0;
                int count = 0;

                while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
                    count++;
                    lastIndex += word.length();
                }
                return count;
            }
        }
        return 0;
    }

    public static class SearchResult {
        public BSTree.FileInfo fileInfo;
        public String snippet;
        public int occurrences;

        public SearchResult(BSTree.FileInfo fileInfo, String snippet, int occurrences) {
            this.fileInfo = fileInfo;
            this.snippet = snippet;
            this.occurrences = occurrences;
        }
    }
}
