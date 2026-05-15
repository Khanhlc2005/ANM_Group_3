package file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void writesAndReadsUtf8Text() throws Exception {
        FileService service = new FileService();
        Path file = tempDir.resolve("message.txt");

        service.writeText(file, "DES Studio\nXin ch\u00E0o");

        assertEquals("DES Studio\nXin ch\u00E0o", service.readText(file));
    }

    @Test
    void writesNullContentAsEmptyFile() throws Exception {
        FileService service = new FileService();
        Path file = tempDir.resolve("empty.txt");

        service.writeText(file, null);

        assertEquals("", service.readText(file));
    }
}
