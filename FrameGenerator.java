package pe.edu.utp.videoclip;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public final class FrameGenerator {
    private FrameGenerator() {}

    public record Result(
            String mode,
            int frames,
            int threads,
            double seconds
    ) {}

    // ============================================================
    // SERIAL
    // ============================================================
    public static Result generateSerial(
            AudioData audio,
            MatrixImage[] images,
            int totalFrames,
            Path outDir
    ) throws Exception {
        clean(outDir);

        long startTime = System.nanoTime();

        for (int frame = 0; frame < totalFrames; frame++) {
            generateOne(
                    audio,
                    images,
                    frame,
                    totalFrames,
                    outDir
            );
        }

        double seconds =
                (System.nanoTime() - startTime)
                / 1_000_000_000.0;

        return new Result(
                "SERIAL",
                totalFrames,
                1,
                seconds
        );
    }

    // ============================================================
    // PARALELO CON THREAD POOL
    // ============================================================
    public static Result generateParallel(
            AudioData audio,
            MatrixImage[] images,
            int totalFrames,
            int threads,
            Path outDir
    ) throws Exception {
        clean(outDir);

        ExecutorService pool =
                Executors.newFixedThreadPool(threads);

        List<Future<?>> jobs = new ArrayList<>();

        long startTime = System.nanoTime();

        for (int frame = 0; frame < totalFrames; frame++) {
            final int frameNumber = frame;

            jobs.add(
                pool.submit(() -> {
                    try {
                        generateOne(
                                audio,
                                images,
                                frameNumber,
                                totalFrames,
                                outDir
                        );
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
            );
        }

        // Esperar todas las tareas.
        for (Future<?> job : jobs) {
            job.get();
        }

        pool.shutdown();

        double seconds =
                (System.nanoTime() - startTime)
                / 1_000_000_000.0;

        return new Result(
                "PARALLEL",
                totalFrames,
                threads,
                seconds
        );
    }

    private static void generateOne(
            AudioData audio,
            MatrixImage[] images,
            int frame,
            int totalFrames,
            Path outDir
    ) throws Exception {
        double level = AudioAnalyzer.levelForFrame(
                audio,
                frame,
                totalFrames
        );

        int index = StudentWork.chooseImageIndex(
                level,
                frame,
                totalFrames,
                images.length
        );

        // Protección por si el estudiante devuelve un índice inválido.
        index = Math.max(
                0,
                Math.min(images.length - 1, index)
        );

        MatrixImage transformed =
                StudentWork.applyEffects(
                        images[index],
                        level,
                        frame,
                        totalFrames
                );

        Path output = outDir.resolve(
                String.format("frame_%03d.png", frame)
        );

        transformed.save(output);
    }

    private static void clean(Path dir) throws Exception {
        Files.createDirectories(dir);

        try (var stream = Files.list(dir)) {
            stream
                    .filter(p ->
                            p.getFileName()
                             .toString()
                             .toLowerCase()
                             .endsWith(".png")
                    )
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {}
                    });
        }
    }
}
