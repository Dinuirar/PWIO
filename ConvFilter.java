import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConvFilter {
    private int[][] kernel;
    private int kernel_size;
    private int shift;
    private int kernel_sum;

    public ConvFilter(int[][] k) {
        kernel = k;
        kernel_size = kernel.length;

        kernel_sum = 0;
        for (int i = 0; i < kernel_size; i++) {
            for (int j = 0; j < kernel_size; j++) {
                kernel_sum += kernel[i][j];
            }
        }
        if (kernel_sum == 0)
            kernel_sum = 1;

        shift = (kernel_size - 1) / 2;

        if (kernel_size % 2 != 1) {
            System.out.println("Wrong kernel format");
            kernel_size = 0;
            kernel_sum = 1;
            shift = 0;

            kernel_size = 0;
        }
    }

    //filters src image using n threads and returns result
    public BufferedImage filter(BufferedImage src, int threads) {
        if (src.getType() == 5) {

            int width = src.getWidth();
            int height = src.getHeight();

            BufferedImage dst = new BufferedImage(width, height, 5);

            int col_shift = width / threads;
            int left = width % threads;

            //calculate indexes used by threads
            int[] col_indexes = new int[2 * threads];

            for (int i = 0; i < threads - left; i++) {
                col_indexes[2 * i] = i * col_shift;
                col_indexes[2 * i + 1] = col_indexes[2 * i] + col_shift;
            }

            for (int i = threads - left; i < threads; i++) {
                col_indexes[2 * i] = col_indexes[2 * i - 1];
                col_indexes[2 * i + 1] = col_indexes[2 * i] + col_shift + 1;
            }

            long start = System.nanoTime();

            final ExecutorService executor = Executors.newFixedThreadPool(threads);
            final List<Future<?>> futures = new ArrayList<>();

            for (int t = 0; t < threads; t++) {
                final int t_f = t;
                Future<?> future = executor.submit(() -> {
                    for (int i = col_indexes[2 * t_f]; i < col_indexes[2 * t_f + 1]; i++) {
                        for (int j = 0; j < height; j++) {
                            if (i < kernel_size || i > width - kernel_size) {
                                dst.setRGB(i, j, src.getRGB(i, j));
                            }
                            else if (j < kernel_size || j > height - kernel_size)
                            {
                                dst.setRGB(i, j, src.getRGB(i, j));
                            }
                            else {
                                short[] new_value = {0, 0, 0};
                                for (int ki = 0; ki < kernel_size; ki++) {
                                    for (int kj = 0; kj < kernel_size; kj++) {
                                        // RBB - int 32b, 8b ALPHA | 8b RED | 8b GREEN | 8b BLUE
                                        new_value[0] += ((src.getRGB(i + ki - shift, j + kj - shift) >> 16) & 0xFF)
                                                * kernel[ki][kj];
                                        new_value[1] += ((src.getRGB(i + ki - shift, j + kj - shift) >> 8) & 0xFF)
                                                * kernel[ki][kj];
                                        new_value[2] += (src.getRGB(i + ki - shift, j + kj - shift) & 0xFF)
                                                * kernel[ki][kj];
                                    }
                                }
                                dst.setRGB(i, j,
                                        (new_value[0] / kernel_sum) << 16 | (new_value[1] / kernel_sum) << 8
                                                | (new_value[2] / kernel_sum)
                                );
                            }
                        }
                    }
                });
                futures.add(future);
            }

            try {
                for (Future<?> future : futures) {
                    future.get(); // do anything you need, e.g. isDone(), ...
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            long end = System.nanoTime();
            float time = (end-start);

            System.out.println("Time filtering " + width + "x" + height + " image with " + threads + " threads: " + time);

            return dst;
        } else {
            System.out.println("Wrong image format");
            return null;
        }
    }
}