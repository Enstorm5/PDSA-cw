package com.example.textsearchtool;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HistoryService {
    private Node head;
    private int size;

    private static class Node {
        SearchEntry data;
        Node next;

        Node(SearchEntry data) {
            this.data = data;
            this.next = null;
        }
    }

    public static class SearchEntry {
        public String term;
        public Instant time;

        public SearchEntry(String term, Instant time) {
            this.term = term;
            this.time = time;
        }
    }

    public void addToHistory(String term) {
        SearchEntry entry = new SearchEntry(term, Instant.now());
        Node newNode = new Node(entry);
        newNode.next = head;
        head = newNode;
        size++;
    }

    public SearchEntry[] getSortedHistory(String sortBy) {
        SearchEntry[] history = new SearchEntry[size];
        Node current = head;
        int index = 0;
        while (current != null) {
            history[index++] = current.data;
            current = current.next;
        }

        if ("time".equals(sortBy)) {
            bubbleSortByTime(history);
        } else {
            bubbleSortByTerm(history);
        }

        return history;
    }

    private void bubbleSortByTime(SearchEntry[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].time.isBefore(arr[j + 1].time)) {
                    SearchEntry temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    private void bubbleSortByTerm(SearchEntry[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].term.compareTo(arr[j + 1].term) > 0) {
                    SearchEntry temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}
