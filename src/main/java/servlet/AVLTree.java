package servlet;

import java.util.HashSet;
import java.util.Set;

public class AVLTree {
    private Node root;

    private static class Node {
        String word;
        Set<FileInfo> documents;
        Node left, right;
        int height;

        Node(String word) {
            this.word = word;
            this.documents = new HashSet<>();
            this.height = 1;
        }
    }

    public static class FileInfo {
        public String fileName;
        public String fileType;

        public FileInfo(String fileName) {
            this.fileName = fileName;
            this.fileType = getFileExtension(fileName);
        }

        private String getFileExtension(String fileName) {
            int lastDotIndex = fileName.lastIndexOf('.');
            return (lastDotIndex == -1) ? "unknown" : fileName.substring(lastDotIndex + 1);
        }
    }

    private int height(Node node) {
        return (node == null) ? 0 : node.height;
    }

    private int balanceFactor(Node node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private void updateHeight(Node node) {
        if (node != null) {
            node.height = 1 + Math.max(height(node.left), height(node.right));
        }
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        updateHeight(x);
        updateHeight(y);

        return y;
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
            return node;
        }

        updateHeight(node);

        int balance = balanceFactor(node);


        if (balance > 1) {
            if (word.compareTo(node.left.word) < 0) {
                return rotateRight(node);
            } else {
                node.left = rotateLeft(node.left);
                return rotateRight(node);
            }
        }


        if (balance < -1) {
            if (word.compareTo(node.right.word) > 0) {
                return rotateLeft(node);
            } else {
                node.right = rotateRight(node.right);
                return rotateLeft(node);
            }
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
