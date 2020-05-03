/**
 *
 */
package io.snice.codecs.codec.gtp;

import io.snice.buffer.Buffer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jonas@jonasborjesson.com
 */
public class GtpTestBase {

    public static Buffer loadRaw(final String resource) throws Exception {
        final Path path = Paths.get(GtpTestBase.class.getResource(resource).toURI());
        final byte[] content = Files.readAllBytes(path);
        return Buffer.of(content);
    }

}
