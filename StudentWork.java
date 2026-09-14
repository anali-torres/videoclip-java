package pe.edu.utp.videoclip;

/*
 * ============================================================
 * ARCHIVO PRINCIPAL QUE DEBE MODIFICAR EL ESTUDIANTE
 * ============================================================
 *
 * El proyecto ya contiene:
 * - lectura WAV
 * - lectura de varias imágenes
 * - generación de frames
 * - modo serial
 * - modo paralelo con threads
 * - medición de tiempos
 *
 * El estudiante debe completar/personalizar los TODO.
 */
public final class StudentWork {

    private StudentWork() {}

    /*
     * TODO 1
     * Analizar un segmento del arreglo short[] y devolver un nivel
     * entre 0.0 y 1.0.
     *
     * Versión inicial:
     * devuelve un valor fijo para que el proyecto pueda ejecutarse.
     *
     * El estudiante debe reemplazarla por un recorrido del arreglo.
     */
    public static double calculateAudioLevel(
            short[] samples,
            int start,
            int end
    ) {
        if (samples == null || samples.length == 0) {
            return 0.0;
        }

        // Protegemos los límites por si el último bloque es más corto.
        int s = Math.max(0, start);
        int e = Math.min(samples.length, end);

        if (e <= s) {
            return 0.0;
        }

        long sum = 0;
        for (int i = s; i < e; i++) {
            sum += Math.abs((int) samples[i]);
        }

        double average = sum / (double) (e - s);

        // Un short va de -32768 a 32767, así que normalizamos por 32768
        // para obtener un valor entre 0.0 (silencio) y ~1.0 (volumen máximo).
        double level = average / 32768.0;

        return Math.max(0.0, Math.min(1.0, level));
    }

    /*
     * TODO 2
     * Elegir qué imagen de MatrixImage[] se utilizará en el frame.
     *
     * La versión inicial cambia por tiempo.
     * El estudiante puede cambiarla para que dependa del audio.
     */
    public static int chooseImageIndex(
            double level,
            int frameNumber,
            int totalFrames,
            int imageCount
    ) {
        if (imageCount <= 1) return 0;

        // Regla 1 (tiempo): recorre cíclicamente todas las imágenes
        // a lo largo de la duración del clip.
        int block = Math.max(1, totalFrames / imageCount);
        int timeIndex = Math.min(imageCount - 1, frameNumber / block);

        // Regla 2 (amplitud): la energía del audio indica qué tan
        // "intensa" debería verse la imagen; a mayor nivel, se
        // recorren imágenes más adelante en el arreglo.
        int levelIndex = (int) Math.floor(level * imageCount);
        levelIndex = Math.max(0, Math.min(imageCount - 1, levelIndex));

        // Combinación de ambas reglas: en los golpes de energía fuerte
        // (level alto) priorizamos la imagen sugerida por el audio,
        // como si fuera un "flash"; en el resto del tiempo seguimos
        // el recorrido normal por tiempo.
        if (level > 0.6) {
            return levelIndex;
        }
        return timeIndex;
    }

    /*
     * TODO 3
     * Crear el aspecto visual de cada frame.
     *
     * Debe utilizar como mínimo:
     * - una transformación geométrica
     * - un filtro basado en matriz/convolución
     *
     * La versión inicial solo rota suavemente.
     */
    public static MatrixImage applyEffects(
            MatrixImage base,
            double level,
            int frameNumber,
            int totalFrames
    ) {
        // Transformación geométrica: una leve rotación oscilante
        // que le da movimiento constante a la animación.
        double angle = Math.sin(frameNumber * 0.12) * (6.0 + level * 15.0); 
        MatrixImage rotated = base.rotate(angle);

        // Filtros por matriz/convolución según el nivel de audio:
        // 4 estados visuales distintos (silencio, bajo, medio, alto).
       if (level < 0.20) {
           double brillo = 0.9 + level;
            return rotated.brighten(brillo);
        } else if (level < 0.45) {
            return rotated.blur();
        } else if (level < 0.70) {
            return rotated.sharpen();
         } else {
            if (frameNumber % 2 == 0) {
                return rotated.brighten(1.8);
         } else {
            return rotated.brighten(0.5);
         }
    }
    }
} 
  