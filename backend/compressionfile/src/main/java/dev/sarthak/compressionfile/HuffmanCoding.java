package dev.sarthak.compressionfile;

import org.springframework.stereotype.Service;

import java.util.*;

class Node implements Comparable<Node> {
    char character;
    int frequency;
    Node left, right;

    public Node(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.frequency, other.frequency);
    }
}
@Service
public class HuffmanCoding {
    public static HashMap<Character, Integer> frequencyMap(String input) {
        HashMap<Character, Integer> map = new HashMap<>();
        for (char c : input.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        return map;
    }

    public static Node buildHuffmanTree(HashMap<Character, Integer> frequencyMap) {
        PriorityQueue<Node> queue = new PriorityQueue<>();

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            Node node = new Node(entry.getKey(), entry.getValue());
            queue.add(node);
        }

        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();

            Node internalNode = new Node('\0', left.frequency + right.frequency);
            internalNode.left = left;
            internalNode.right = right;

            queue.add(internalNode);
        }

        return queue.poll();
    }

    public static String compress(String input, Node root) {
        StringBuilder compressed = new StringBuilder();

        for (char c : input.toCharArray()) {
            String code = findHuffmanCode(c, root, "");
            compressed.append(code);
        }

        return compressed.toString();
    }

    private static String findHuffmanCode(char c, Node node, String code) {
        if (node.character == c) {
            return code;
        }

        if (node.left != null) {
            String leftCode = findHuffmanCode(c, node.left, code + "0");
            if (leftCode != null) {
                return leftCode;
            }
        }

        if (node.right != null) {
            String rightCode = findHuffmanCode(c, node.right, code + "1");
            if (rightCode != null) {
                return rightCode;
            }
        }

        return null;
    }

    public static String decompress(String compressed, Node root) {
        StringBuilder decompressed = new StringBuilder();
        Node currentNode = root;

        for (char c : compressed.toCharArray()) {
            if (c == '0') {
                currentNode = currentNode.left;
            } else {
                currentNode = currentNode.right;
            }

            if (currentNode.left == null && currentNode.right == null) {
                decompressed.append(currentNode.character);
                currentNode = root;
            }
        }

        return decompressed.toString();
    }

    public static void main(String[] args) {
        String input = "Hello, World!";
        HashMap<Character, Integer> frequencyMap = frequencyMap(input);
        Node root = buildHuffmanTree(frequencyMap);

        String compressed = compress(input, root);
        System.out.println("Compressed: " + compressed);

        String decompressed = decompress(compressed, root);
        System.out.println("Decompressed: " + decompressed);
    }
}