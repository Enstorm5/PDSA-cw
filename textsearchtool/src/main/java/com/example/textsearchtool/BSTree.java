package com.example.textsearchtool;

import java.util.HashSet;
import java.util.Set;

public class BSTree {
    private Node root;

    private static class Node {
        String word;
        Set<FileInfo> documents;
        Node left, right;

        Node(String word) {
            this.word = word;
            this.documents = new HashSet<>();
        }
    }

    public static class FileInfo {
        public String fileName;

        public FileInfo(String fileName) {
            this.fileName = fileName;
        }
    }

    public void insert(String word, String document) {
        root = insert(root, word, document);
    }

    private Node insert(Node node, String word, String document) {
        if (node == null) {
            Node newNode = new Node(word);
            newNode.documents.add(new FileInfo(document));
            return newNode;
        }

        int cmp = word.compareTo(node.word);
        if (cmp < 0) {
            node.left = insert(node.left, word, document);
        } else if (cmp > 0) {
            node.right = insert(node.right, word, document);
        } else {
            node.documents.add(new FileInfo(document));
        }

        return node;
    }

    public Set<FileInfo> search(String word) {
        Node node = search(root, word);
        return (node != null) ? node.documents : new HashSet<>();
    }

    private Node search(Node node, String word) {
        if (node == null) {
            return null;
        }

        int cmp = word.compareTo(node.word);
        if (cmp < 0) {
            return search(node.left, word);
        } else if (cmp > 0) {
            return search(node.right, word);
        } else {
            return node;
        }
    }
}