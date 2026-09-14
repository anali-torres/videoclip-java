package pe.edu.utp.videoclip;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public record AppConfig(
        Path audio,
        Path imagesFolder,
        int fps,
        int durationSeconds,
        String mode,
        int threads,
        Path serialOutput,
        Path parallelOutput,
        String videoSerial,
        String videoParallel
) {
    public static AppConfig load(Path file) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        }

        return new AppConfig(
                Path.of(p.getProperty("audio")),
                Path.of(p.getProperty("imagesFolder")),
                Integer.parseInt(p.getProperty("fps", "12")),
                Integer.parseInt(p.getProperty("durationSeconds", "10")),
                p.getProperty("mode", "both").trim().toLowerCase(),
                Integer.parseInt(p.getProperty("threads", "4")),
                Path.of(p.getProperty("serialOutput", "output/frames_serial")),
                Path.of(p.getProperty("parallelOutput", "output/frames_parallel")),
                p.getProperty("videoSerial", "videoclip_serial.mp4"),
                p.getProperty("videoParallel", "videoclip_parallel.mp4")
        );
    }

    public int totalFrames() {
        return fps * durationSeconds;
    }
}
