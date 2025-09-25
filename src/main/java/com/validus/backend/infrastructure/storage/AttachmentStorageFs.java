package com.validus.backend.infrastructure.storage;

import com.validus.backend.application.port.outgoing.AttachmentStoragePort;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AttachmentStorageFs implements AttachmentStoragePort {

    private final Path rootPath;

    public AttachmentStorageFs(@Value("${storage.root:./storage}") String root) {
        this.rootPath = Paths.get(root).toAbsolutePath();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize storage directory", e);
        }
    }

    @Override
    public String store(String invoiceId, String fileName, InputStream content) {
        try {
            Path invoiceDir = rootPath.resolve(invoiceId);
            Files.createDirectories(invoiceDir);
            String sanitizedFileName = fileName == null ? "file" : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destination = invoiceDir.resolve(System.currentTimeMillis() + "_" + sanitizedFileName);
            Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored attachment {}", destination);
            return destination.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store attachment", e);
        }
    }

    @Override
    public Path load(String storageRef) {
        return Paths.get(storageRef);
    }
}
