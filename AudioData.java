package pe.edu.utp.videoclip;

public record AudioData(short[] samples, float sampleRate) {
    public double durationSeconds() {
        return samples.length / sampleRate;
    }
}
