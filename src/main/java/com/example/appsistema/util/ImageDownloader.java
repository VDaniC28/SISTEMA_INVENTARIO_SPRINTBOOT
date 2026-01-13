package com.example.appsistema.util;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

public class ImageDownloader {

    public static String guardarImagenDesdeUrl(String imageUrl, String subcarpeta) throws Exception {
        URL url = new URL(imageUrl);

        // Detectar extensión
        String extension = ".jpg";
        if (imageUrl.toLowerCase().endsWith(".png")) {
            extension = ".png";
        } else if (imageUrl.toLowerCase().endsWith(".jpeg")) {
            extension = ".jpeg";
        }

        // Crear carpeta uploads/productos si no existe
        Path uploadDir = Paths.get("uploads", subcarpeta);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Nombre aleatorio
        String fileName = UUID.randomUUID() + extension;
        Path destino = uploadDir.resolve(fileName);

        // Descargar
        try (InputStream in = url.openStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        // Devolver ruta accesible (para usar en la vista)
        return "/uploads/" + subcarpeta + "/" + fileName;
    }
}
