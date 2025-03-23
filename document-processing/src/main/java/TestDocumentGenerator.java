import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

//Generates sample documents in test_doc folder to test bulk upload
public class TestDocumentGenerator {
    public static void main(String[] args) throws IOException, DocumentException {
        String outputDir = "test_documents";
        new File(outputDir).mkdirs();

        // Generate 500 DOCX files
        for (int i = 1; i <= 500; i++) {
            try (XWPFDocument doc = new XWPFDocument();
                 FileOutputStream out = new FileOutputStream(outputDir + "/test_doc_" + i + ".docx")) {
                doc.createParagraph().createRun().setText("Sample Text for Document " + i);
                doc.write(out);
            }
        }

        // Generate 500 PDF files
        for (int i = 1; i <= 500; i++) {
            Document pdf = new Document();
            PdfWriter.getInstance(pdf, new FileOutputStream(outputDir + "/test_pdf_" + i + ".pdf"));
            pdf.open();
            pdf.add(new Paragraph("Sample Text for PDF " + i));
            pdf.close();
        }

        System.out.println("Test documents generated in " + outputDir);
    }
}
