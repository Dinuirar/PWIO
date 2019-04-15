// based on example:
// https://docs.oracle.com/javase/tutorial/essential/concurrency/examples/ForkBlur.java

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// central worker class that does actual filtering
// class derives from RecursiveAction to be compatible with ForkJoin framework
public class ConvFilter extends RecursiveAction {
    // one-dimensional array to modify pixel values from image
    private int[] kernel = {
//            3, 0, -3,
//            10, 0, -10,
//            3, 0, -3
            1, 0, -1,
            2, 0, -2,
            1, 0, -1
    };
    private int kernel_size;

    private int[] src_tab;
    private int i_start;
    private int tab_length;
    private int[] dst_tab;

    // public constructor
    public ConvFilter(int[] src, int start, int length, int[] dst) {
        src_tab = src;
        i_start = start;
        tab_length = length;
        dst_tab = dst;
        kernel_size = kernel.length;
    }

    // if the task is small enough - compute the task directly (apply filter)
    private void computeDirectly() {
        // range of pixels to multiply and add to a given pixel
        int range = (kernel_size - 1) / 2;
        for (int index = i_start; index < Math.min(i_start + tab_length, dst_tab.length); index++) {
            float first_component = 0;
            float second_component = 0;
            float third_component = 0;
            for (int mi = -range; mi <= range; mi++) {
                // s_index: do not cross the array boundaries
                int s_index = Math.min(Math.max(0, index + mi), src_tab.length - 1);
                // source pixel
                int src_pixel = src_tab[s_index];
                // separate components of the pixel calculation
                // for every pixel from range: add its proportional part to the sum
                //first_component += ((float) ((src_pixel & 0x00ff0000) >> 16) * kernel[mi + range]) / kernel_size;
                //second_component += ((float) ((src_pixel & 0x0000ff00) >> 8) * kernel[mi + range]) / kernel_size;
                third_component += ((float) (src_pixel & 0x000000ff) * kernel[mi + range]) / kernel_size;
            }

            // assemble the pixel from separate components
            int new_pixel = (0xff000000)
                    //| (((int) first_component) << 16)
                    //| (((int) second_component) << 8)
                    | ((int) third_component);
            // save pixel into destination array
            dst_tab[index] = new_pixel;
        }
    }

    // do filtering
    @Override
    protected void compute() {
        // if the problem is small enough - calculate directly
        if (tab_length < threshold) {
            this.computeDirectly();
            return;
        }
        // pivot to split larger array
        int pivot;
        pivot = tab_length / 2;

        // create new worker threads to help finish the task
        invokeAll(new ConvFilter(src_tab, i_start, pivot, dst_tab),
                new ConvFilter(src_tab, i_start + pivot, tab_length - pivot, dst_tab));
    }

    // threshold decisive about how small should be task to calculate it directly
    private static int threshold = 20000;

    BufferedImage filter(BufferedImage src) {
        // if image is of a proper type
        if (src.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            int width = src.getWidth();
            int height = src.getHeight();

            // convert image to a one-dimensional array
            int[] srci = src.getRGB(0, 0, width, height, null, 0, width);
            int[] dsti = new int[srci.length];

            // recursively create ConvFilter object
            ConvFilter cf = new ConvFilter(srci, 0, srci.length, dsti);
            // create pool with threads
            ForkJoinPool pool = new ForkJoinPool();

            // measure time - how long does it take to process an image
            long start = System.currentTimeMillis();
            pool.invoke(cf);
            long end = System.currentTimeMillis();

            System.out.println("t = " + (end - start) + "ms");

            // create destination image and convert one-dimensional array to an image to write
            BufferedImage dstimg = new BufferedImage(width, height, 5);
            dstimg.setRGB(0, 0, width, height, dsti, 0, width);

            return dstimg;
        // if image is of a wrong format - do not process it
        } else {
            System.out.println("Wrong image format");
            return null;
        }
    }
}