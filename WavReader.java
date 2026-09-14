package pe.edu.utp.videoclip;

import javax.sound.sampled.*;
import java.nio.file.Path;

public final class WavReader {
    private WavReader() {}

    public static AudioData read(Path path) throws Exception {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
            AudioFormat base = in.getFormat();

            AudioFormat format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    1,
                    2,
                    base.getSampleRate(),
                    false
            );

            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(format, in)) {
                byte[] bytes = pcm.readAllBytes();
                short[] samples = new short[bytes.length / 2];

                for (int i = 0; i < samples.length; i++) {
                    int lo = bytes[2*i] & 0xFF;
                    int hi = bytes[2*i + 1] << 8;
                    samples[i] = (short)(hi | lo);
                }

                return new AudioData(samples, format.getSampleRate());
            }
        }
    }
}
