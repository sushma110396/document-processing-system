import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

public class BulkUploadClient {
    private static final String UPLOAD_URL = "http://localhost:9090/documents/upload";

    public static void main(String[] args) {
        File folder = new File("test_documents");
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    uploadFile(file);
                } catch (IOException e) {
                    System.err.println("Failed to upload " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private static void uploadFile(File file) throws IOException {
        String boundary = "===" + System.currentTimeMillis() + "===";
        String lineEnd = "\r\n";
        String fileType = file.getName().endsWith(".pdf") ? "application/pdf" : "application/msword";
        String name = file.getName();

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:9090/documents/upload").openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (var output = connection.getOutputStream()) {
            // file field
            output.write(("--" + boundary + lineEnd).getBytes());
            output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + lineEnd).getBytes());
            output.write(("Content-Type: " + fileType + lineEnd + lineEnd).getBytes());
            Files.copy(file.toPath(), output);
            output.write(lineEnd.getBytes());

            // name field
            output.write(("--" + boundary + lineEnd).getBytes());
            output.write(("Content-Disposition: form-data; name=\"name\"" + lineEnd + lineEnd).getBytes());
            output.write(name.getBytes());
            output.write(lineEnd.getBytes());

            // type field
            output.write(("--" + boundary + lineEnd).getBytes());
            output.write(("Content-Disposition: form-data; name=\"type\"" + lineEnd + lineEnd).getBytes());
            output.write(fileType.getBytes());
            output.write(lineEnd.getBytes());

            // end
            output.write(("--" + boundary + "--" + lineEnd).getBytes());
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Uploaded " + file.getName() + " - Response: " + responseCode);
        connection.disconnect();
    }


}
