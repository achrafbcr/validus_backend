package com.validus.backend.application.port.outgoing;

import java.io.InputStream;
import java.nio.file.Path;

public interface AttachmentStoragePort {

    String store(String invoiceId, String fileName, InputStream content);

    Path load(String storageRef);
}
