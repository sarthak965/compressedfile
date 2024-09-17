package dev.sarthak.compressionfile;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/compression")
@SpringBootApplication(scanBasePackages = {"com.example.filecompression"})
public class FileCompressionController {

    private final HuffmanCoding huffmanCoding;

    @Autowired
    public FileCompressionController(HuffmanCoding huffmanCoding) {
        this.huffmanCoding = huffmanCoding;
    }

    @PostMapping("/compress")
    public ResponseEntity<String> compressFile(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String compressedFileName = fileName + ".huffman";

        // Read the file content
        byte[] fileContent = file.getBytes();
        String fileContentString = new String(fileContent);

        // Compress the file content using Huffman coding
        String compressedContent = huffmanCoding.compress(fileContentString, huffmanCoding.buildHuffmanTree(huffmanCoding.frequencyMap(fileContentString)));

        // Save the compressed content to a file
        Path path = Paths.get(compressedFileName);
        Files.write(path, compressedContent.getBytes());

        // Return the compressed file name
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/compression/download/")
                .path(compressedFileName)
                .toUriString();

        return ResponseEntity.ok(fileDownloadUri);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load the compressed file as a resource
        Path path = Paths.get(fileName);
        Resource resource = new org.springframework.core.io.FileSystemResource(path.toFile());

        // Try to determine the file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/decompress")
    public ResponseEntity<String> decompressFile(@RequestParam("file") MultipartFile file) throws IOException {
        // Read the compressed file content
        byte[] compressedContent = file.getBytes();
        String compressedContentString = new String(compressedContent);

        // Decompress the file content using Huffman coding
        String decompressedContent = huffmanCoding.decompress(compressedContentString, huffmanCoding.buildHuffmanTree(huffmanCoding.frequencyMap(compressedContentString)));

        // Save the decompressed content to a file
        String decompressedFileName = file.getOriginalFilename().replace(".huffman", "");
        Path path = Paths.get(decompressedFileName);
        Files.write(path, decompressedContent.getBytes());

        // Return the decompressed file name
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/compression/download/")
                .path(decompressedFileName)
                .toUriString();

        return ResponseEntity.ok(fileDownloadUri);
    }
    public static void main(String[] args) {
        SpringApplication.run(CompressionfileApplication.class, args);
    }

}