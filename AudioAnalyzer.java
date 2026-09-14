package pe.edu.utp.videoclip;

public final class AudioAnalyzer {
    private AudioAnalyzer() {}

    public static double levelForFrame(
            AudioData audio,
            int frame,
            int totalFrames
    ) {
        short[] samples = audio.samples();

        int start = (int)(
                (long)frame * samples.length / totalFrames
        );

        int end = (int)(
                (long)(frame + 1) * samples.length / totalFrames
        );

        if (end <= start) {
            end = Math.min(samples.length, start + 1);
        }

        double level = StudentWork.calculateAudioLevel(
                samples,
                start,
                end
        );

        return Math.max(0.0, Math.min(1.0, level));
    }
}
