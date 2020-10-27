package io.snice.networking.gtp;

import io.snice.buffer.Buffer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GtpStackTestBase {

    public static Buffer loadRaw(final String resource) throws Exception {
        final Path path = Paths.get(GtpStackTestBase.class.getResource(resource).toURI());
        final byte[] content = Files.readAllBytes(path);
        return Buffer.of(content);
    }

}
