package servlet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class SearchEngine {
    private AVLTree avlTree;
    private String folderPath;

    public SearchEngine() {
        this.avlTree = new AVLTree();
    }

    public void indexFolder(String folderPath) throws IOException {
        this.folderPath = folderPath;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    indexFile(file);
                }
            }
        }
    }

    private void indexFile(File file) throws IOException {
        String content;
        String fileName = file.getName();

        if (fileName.toLowerCase().endsWith(".pdf")) {
            content = extractTextFromPdf(file);
        } else {
            content = new String(Files.readAllBytes(file.toPath()));
        }

        String[] words = content.toLowerCase().split("\\W+");

        for (String word : words) {
            if (!word.isEmpty()) {
                avlTree.insert(word, fileName);
            }
        }
    }

    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public static class SearchResult {
        public AVLTree.FileInfo fileInfo;
        public String snippet;
        public String fullPath;
        public int occurrences;

        public SearchResult(AVLTree.FileInfo fileInfo, String snippet, String fullPath, int occurrences) {
            this.fileInfo = fileInfo;
            this.snippet = snippet;
            this.fullPath = fullPath;
            this.occurrences = occurrences;
        }
    }

    public List<SearchResult> search(String query) {
        query = query.toLowerCase();
        boolean isPhrase = query.startsWith("\"") && query.endsWith("\"");

        if (isPhrase) {
            query = query.substring(1, query.length() - 1);
            return phraseSearch(query);
        } else {
            return wordSearch(query);
        }
    }

    private List<SearchResult> wordSearch(String word) {
        Set<AVLTree.FileInfo> fileInfos = avlTree.search(word);
        List<SearchResult> results = new ArrayList<>();
        Set<String> addedFiles = new HashSet<>();

        for (AVLTree.FileInfo fileInfo : fileInfos) {
            if (!addedFiles.contains(fileInfo.fileName)) {
                String fullPath = this.folderPath + File.separator + fileInfo.fileName;
                String snippet = getSnippet(fileInfo.fileName, word);
                if (!snippet.equals("Snippet not available (phrase not found in file)")) {
                    int occurrences = countOccurrences(fileInfo.fileName, word);
                    results.add(new SearchResult(fileInfo, snippet, fullPath, occurrences));
                    addedFiles.add(fileInfo.fileName);
                }
            }
        }

        return results;
    }

    private List<SearchResult> phraseSearch(String phrase) {
        String[] words = phrase.split("\\s+");
        if (words.length == 0) return new ArrayList<>();

        Set<AVLTree.FileInfo> fileInfos = avlTree.search(words[0]);
        List<SearchResult> results = new ArrayList<>();
        Set<String> addedFiles = new HashSet<>();

        for (AVLTree.FileInfo fileInfo : fileInfos) {
            if (!addedFiles.contains(fileInfo.fileName)) {
                String fullPath = this.folderPath + File.separator + fileInfo.fileName;
                String fileContent = getFileContent(fullPath);

                if (fileContent.toLowerCase().contains(phrase)) {
                    String snippet = getSnippet(fileInfo.fileName, phrase);
                    if (!snippet.equals("Snippet not available (phrase not found in file)")) {
                        int occurrences = countOccurrences(fileInfo.fileName, phrase);
                        results.add(new SearchResult(fileInfo, snippet, fullPath, occurrences));
                        addedFiles.add(fileInfo.fileName);
                    }
                }
            }
        }

        return results;
    }

    private String getFileContent(String filePath) {
        try {
            File file = new File(filePath);
            if (filePath.toLowerCase().endsWith(".pdf")) {
                return extractTextFromPdf(file);
            } else {
                return new String(Files.readAllBytes(file.toPath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getSnippet(String fileName, String searchTerm) {
        try {
            String fullPath = this.folderPath + File.separator + fileName;
            File file = new File(fullPath);

            if (!file.exists()) {
                return "Snippet not available (file not found)";
            }

            String content = getFileContent(fullPath);

            int index = content.toLowerCase().indexOf(searchTerm.toLowerCase());
            if (index != -1) {
                int start = Math.max(0, index - 50);
                int end = Math.min(content.length(), index + searchTerm.length() + 50);
                return "..." + content.substring(start, end) + "...";
            } else {
                return "Snippet not available (phrase not found in file)";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Snippet not available (error reading file)";
        }
    }

    private int countOccurrences(String fileName, String searchTerm) {
        String fullPath = this.folderPath + File.separator + fileName;
        String content = getFileContent(fullPath).toLowerCase();
        searchTerm = searchTerm.toLowerCase();

        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = content.indexOf(searchTerm, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += searchTerm.length();
            }
        }

        return count;
    }
}