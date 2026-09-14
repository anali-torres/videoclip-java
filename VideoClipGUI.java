package pe.edu.utp.videoclip;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;

public class VideoClipGUI extends JFrame {

    private final JComboBox<String> mode =
            new JComboBox<>(new String[]{
                    "SERIAL",
                    "PARALLEL",
                    "BOTH"
            });

    private final JSpinner threads =
            new JSpinner(
                    new SpinnerNumberModel(
                            4, 1, 32, 1
                    )
            );

    private final JTextArea log =
            new JTextArea();

    public VideoClipGUI() {
        super("UTP | VideoClip MultiImagen + Threads");

        setDefaultCloseOperation(
                WindowConstants.EXIT_ON_CLOSE
        );

        setSize(900, 580);
        setLocationRelativeTo(null);

        JPanel root =
                new JPanel(new BorderLayout(10,10));

        root.setBorder(
                new EmptyBorder(12,12,12,12)
        );

        JPanel top =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton run =
                new JButton("Generar frames");

        top.add(new JLabel("Modo:"));
        top.add(mode);
        top.add(new JLabel("Threads:"));
        top.add(threads);
        top.add(run);

        log.setEditable(false);
        log.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        14
                )
        );

        root.add(top, BorderLayout.NORTH);
        root.add(
                new JScrollPane(log),
                BorderLayout.CENTER
        );

        setContentPane(root);

        run.addActionListener(
                e -> generate()
        );
    }

    private void generate() {
        // Worker para no congelar Swing.
        new Thread(() -> {
            try {
                AppConfig cfg =
                        AppConfig.load(
                                Path.of("config.properties")
                        );

                AudioData audio =
                        WavReader.read(cfg.audio());

                MatrixImage[] images =
                        ImageLibrary.loadFolder(
                                cfg.imagesFolder()
                        );

                String selected =
                        (String)mode.getSelectedItem();

                int threadCount =
                        (Integer)threads.getValue();

                FrameGenerator.Result serial = null;
                FrameGenerator.Result parallel = null;

                append("Imágenes cargadas: " + images.length);
                append("Frames: " + cfg.totalFrames());

                if ("SERIAL".equals(selected)
                        || "BOTH".equals(selected)) {

                    append("SERIAL iniciado...");

                    serial =
                            FrameGenerator.generateSerial(
                                    audio,
                                    images,
                                    cfg.totalFrames(),
                                    cfg.serialOutput()
                            );

                    append(String.format(
                            "SERIAL: %.3f s",
                            serial.seconds()
                    ));
                }

                if ("PARALLEL".equals(selected)
                        || "BOTH".equals(selected)) {

                    append(
                            "PARALLEL iniciado con "
                            + threadCount
                            + " threads..."
                    );

                    parallel =
                            FrameGenerator.generateParallel(
                                    audio,
                                    images,
                                    cfg.totalFrames(),
                                    threadCount,
                                    cfg.parallelOutput()
                            );

                    append(String.format(
                            "PARALLEL: %.3f s",
                            parallel.seconds()
                    ));
                }

                if (serial != null && parallel != null) {
                    double speedup =
                            serial.seconds()
                            / parallel.seconds();

                    append(String.format(
                            "SPEEDUP: %.3fx",
                            speedup
                    ));
                }

                Metrics.save(
                        Path.of("output/metrics.csv"),
                        serial,
                        parallel
                );

                append("Terminado.");

            } catch (Exception ex) {
                append("ERROR: " + ex.getMessage());
            }
        }, "utp-gui-worker").start();
    }

    private void append(String text) {
        SwingUtilities.invokeLater(
                () -> log.append(text + "\n")
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> new VideoClipGUI().setVisible(true)
        );
    }
}
