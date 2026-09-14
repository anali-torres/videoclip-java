package pe.edu.utp.videoclip;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public final class MatrixImage {

    private final int[][] data;
    private final String name;

    public MatrixImage(int[][] source, String name) {
        this.name = name;
        data = new int[source.length][source[0].length];

        for (int r = 0; r < source.length; r++) {
            System.arraycopy(source[r], 0, data[r], 0, source[r].length);
        }
    }

    public String name() { return name; }
    public int height() { return data.length; }
    public int width() { return data[0].length; }

    public static MatrixImage load(Path path) throws Exception {
        BufferedImage image = ImageIO.read(path.toFile());

        if (image == null) {
            throw new IllegalArgumentException("No se pudo leer " + path);
        }

        int[][] m = new int[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x,y);
                int r = (rgb >> 16) & 255;
                int g = (rgb >> 8) & 255;
                int b = rgb & 255;

                m[y][x] = clamp((int)Math.round(
                        0.299*r + 0.587*g + 0.114*b
                ));
            }
        }

        return new MatrixImage(m, path.getFileName().toString());
    }

    public MatrixImage brighten(double factor) {
        int[][] out = new int[height()][width()];

        for (int r = 0; r < height(); r++) {
            for (int c = 0; c < width(); c++) {
                out[r][c] = clamp(
                        (int)Math.round(data[r][c] * factor)
                );
            }
        }

        return new MatrixImage(out, name);
    }

    public MatrixImage invert() {
        int[][] out = new int[height()][width()];

        for (int r = 0; r < height(); r++) {
            for (int c = 0; c < width(); c++) {
                out[r][c] = 255 - data[r][c];
            }
        }

        return new MatrixImage(out, name);
    }

    public MatrixImage rotate(double degrees) {
        int h = height();
        int w = width();
        int[][] out = new int[h][w];

        double theta = Math.toRadians(degrees);
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);

        double cx = (w - 1) / 2.0;
        double cy = (h - 1) / 2.0;

        for (int y2 = 0; y2 < h; y2++) {
            for (int x2 = 0; x2 < w; x2++) {
                double dx = x2 - cx;
                double dy = y2 - cy;

                double x =  cos*dx + sin*dy + cx;
                double y = -sin*dx + cos*dy + cy;

                int xi = (int)Math.round(x);
                int yi = (int)Math.round(y);

                out[y2][x2] =
                        xi >= 0 && xi < w && yi >= 0 && yi < h
                        ? data[yi][xi]
                        : 0;
            }
        }

        return new MatrixImage(out, name);
    }

    public MatrixImage blur() {
        return convolve(new double[][]{
                {1,1,1},
                {1,1,1},
                {1,1,1}
        }, 9.0, 0);
    }

    public MatrixImage sharpen() {
        return convolve(new double[][]{
                {0,-1,0},
                {-1,5,-1},
                {0,-1,0}
        }, 1.0, 0);
    }

    public MatrixImage sobel() {
        int[][] out = new int[height()][width()];

        int[][] gx = {
                {-1,0,1},
                {-2,0,2},
                {-1,0,1}
        };

        int[][] gy = {
                {-1,-2,-1},
                {0,0,0},
                {1,2,1}
        };

        for (int r = 1; r < height()-1; r++) {
            for (int c = 1; c < width()-1; c++) {
                int sx = 0;
                int sy = 0;

                for (int kr = -1; kr <= 1; kr++) {
                    for (int kc = -1; kc <= 1; kc++) {
                        int p = data[r+kr][c+kc];
                        sx += p * gx[kr+1][kc+1];
                        sy += p * gy[kr+1][kc+1];
                    }
                }

                out[r][c] = clamp(
                        (int)Math.round(Math.hypot(sx, sy))
                );
            }
        }

        return new MatrixImage(out, name);
    }

    private MatrixImage convolve(
            double[][] kernel,
            double divisor,
            double bias
    ) {
        int[][] out = new int[height()][width()];

        for (int r = 1; r < height()-1; r++) {
            for (int c = 1; c < width()-1; c++) {
                double sum = 0;

                for (int kr = -1; kr <= 1; kr++) {
                    for (int kc = -1; kc <= 1; kc++) {
                        sum += data[r+kr][c+kc]
                                * kernel[kr+1][kc+1];
                    }
                }

                out[r][c] = clamp(
                        (int)Math.round(sum/divisor + bias)
                );
            }
        }

        return new MatrixImage(out, name);
    }

    public void save(Path path) throws Exception {
        path.toFile().getParentFile().mkdirs();

        BufferedImage image = new BufferedImage(
                width(),
                height(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                int v = clamp(data[y][x]);
                int rgb = (v << 16) | (v << 8) | v;
                image.setRGB(x, y, rgb);
            }
        }

        ImageIO.write(image, "png", path.toFile());
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
