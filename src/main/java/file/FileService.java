package file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileService {
    private static final String[] SUPPORTED_TEXT_EXTENSIONS = {"txt", "csv", "md", "log", "json", "xml"};

    public String readText(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public void writeText(Path path, String content) throws IOException {
        Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
    }

    public boolean isSupportedTextFile(Path path) {
        if (path == null || path.getFileName() == null) {
            return false;
        }

        String fileName = path.getFileName().toString().toLowerCase();
        for (String extension : SUPPORTED_TEXT_EXTENSIONS) {
            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    public String[] supportedTextExtensions() {
        return SUPPORTED_TEXT_EXTENSIONS.clone();
    }
}
