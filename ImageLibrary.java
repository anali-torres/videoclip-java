package pe.edu.utp.videoclip;

import java.nio.file.*;
import java.util.*;

public final class ImageLibrary {
    private ImageLibrary() {}

    public static MatrixImage[] loadFolder(Path folder) throws Exception {
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException(
                    "No existe la carpeta de imágenes: " + folder
            );
        }

        List<Path> files;

        try (var stream = Files.list(folder)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(ImageLibrary::isImage)
                    .sorted()
                    .toList();
        }

        if (files.isEmpty()) {
            throw new IllegalArgumentException(
                    "Coloque al menos una imagen PNG/JPG en " + folder
            );
        }

        MatrixImage[] images = new MatrixImage[files.size()];

        for (int i = 0; i < files.size(); i++) {
            images[i] = MatrixImage.load(files.get(i));
        }

        return images;
    }

    private static boolean isImage(Path p) {
        String n = p.getFileName().toString().toLowerCase();
        return n.endsWith(".png")
                || n.endsWith(".jpg")
                || n.endsWith(".jpeg");
    }
}
