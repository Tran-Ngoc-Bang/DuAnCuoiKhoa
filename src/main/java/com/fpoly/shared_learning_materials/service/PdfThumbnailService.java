package com.fpoly.shared_learning_materials.service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfThumbnailService {

    public void generateThumbnails(File pdfFile, Long fileId) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int totalPages = Math.min(document.getNumberOfPages(), 5);

            Path outputDir = Paths.get("src/main/resources/static/uploads/thumbnails");
            outputDir.toFile().mkdirs();

            for (int page = 0; page < totalPages; ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 100); 
                String fileName = String.format("%d_page%d.png", fileId, page + 1);
                File outputFile = outputDir.resolve(fileName).toFile();
                ImageIO.write(bim, "png", outputFile);
            }
        }
    }
}
