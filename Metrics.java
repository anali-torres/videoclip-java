package pe.edu.utp.videoclip;

import java.io.PrintWriter;
import java.nio.file.*;

public final class Metrics {
    private Metrics() {}

    public static void print(
            FrameGenerator.Result serial,
            FrameGenerator.Result parallel
    ) {
        System.out.println();
        System.out.println("============= RESULTADOS =============");

        if (serial != null) {
            System.out.printf(
                    "Serial:   %.3f s%n",
                    serial.seconds()
            );
        }

        if (parallel != null) {
            System.out.printf(
                    "Paralelo: %.3f s | %d threads%n",
                    parallel.seconds(),
                    parallel.threads()
            );
        }

        if (serial != null && parallel != null) {
            double speedup =
                    serial.seconds() / parallel.seconds();

            double efficiency =
                    speedup / parallel.threads();

            System.out.printf(
                    "Speedup:    %.3fx%n",
                    speedup
            );

            System.out.printf(
                    "Eficiencia: %.2f%%%n",
                    efficiency * 100
            );
        }

        System.out.println("======================================");
    }

    public static void save(
            Path file,
            FrameGenerator.Result serial,
            FrameGenerator.Result parallel
    ) throws Exception {
        Files.createDirectories(file.getParent());

        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(file)
        )) {
            out.println("mode,frames,threads,seconds");

            if (serial != null) {
                out.printf(
                        "%s,%d,%d,%.6f%n",
                        serial.mode(),
                        serial.frames(),
                        serial.threads(),
                        serial.seconds()
                );
            }

            if (parallel != null) {
                out.printf(
                        "%s,%d,%d,%.6f%n",
                        parallel.mode(),
                        parallel.frames(),
                        parallel.threads(),
                        parallel.seconds()
                );
            }
        }
    }
}
