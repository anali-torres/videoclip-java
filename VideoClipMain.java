package pe.edu.utp.videoclip;

import java.nio.file.Path;

public class VideoClipMain {

    public static void main(String[] args) throws Exception {
        AppConfig cfg = AppConfig.load(
                Path.of("config.properties")
        );

        AudioData audio = WavReader.read(cfg.audio());

        MatrixImage[] images =
                ImageLibrary.loadFolder(
                        cfg.imagesFolder()
                );

        System.out.println("UTP | VIDEOCLIP MULTIIMAGEN + THREADS");
        System.out.println("-----------------------------------");
        System.out.println("Audio:      " + cfg.audio());
        System.out.println("Imágenes:   " + images.length);

        for (int i = 0; i < images.length; i++) {
            System.out.println(
                    "  [" + i + "] " + images[i].name()
            );
        }

        System.out.println("FPS:        " + cfg.fps());
        System.out.println("Duración:   " + cfg.durationSeconds() + " s");
        System.out.println("Frames:     " + cfg.totalFrames());
        System.out.println("Modo:       " + cfg.mode());
        System.out.println("Threads:    " + cfg.threads());
        System.out.println();

        FrameGenerator.Result serial = null;
        FrameGenerator.Result parallel = null;

        if (cfg.mode().equals("serial")
                || cfg.mode().equals("both")) {

            System.out.println("Generando versión SERIAL...");

            serial = FrameGenerator.generateSerial(
                    audio,
                    images,
                    cfg.totalFrames(),
                    cfg.serialOutput()
            );
        }

        if (cfg.mode().equals("parallel")
                || cfg.mode().equals("both")) {

            System.out.println("Generando versión PARALELA...");

            parallel = FrameGenerator.generateParallel(
                    audio,
                    images,
                    cfg.totalFrames(),
                    cfg.threads(),
                    cfg.parallelOutput()
            );
        }

        Metrics.print(serial, parallel);

        Metrics.save(
                Path.of("output/metrics.csv"),
                serial,
                parallel
        );

        System.out.println();
        System.out.println("Siguiente paso:");
        System.out.println("Ejecute los scripts de FFmpeg para crear el MP4.");
    }
}
