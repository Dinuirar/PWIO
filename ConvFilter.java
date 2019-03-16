import java.awt.image.BufferedImage;

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
	}

	public BufferedImage filter(BufferedImage src, BufferedImage dst, int start_col, int end_col) {
		// można tu jeszcze dać sprawdzenie, czy kolumny startowe i końcowe są
		// poprawne:
		// start_col > (kernel_size-1)/2, end_col < width - (kernel_size-1)/2
		// ale nie wiem, czy nie lepiej tego sprawdzać przed wywołaniem metody

		int height = src.getHeight();

		for (int i = start_col; i < end_col; i++) {
			for (int j = kernel_size; j < height - kernel_size; j++) {
				short[] new_value = { 0, 0, 0 };
				for (int ki = 0; ki < kernel_size; ki++) {
					// RBB - int 32b, 8b ALPHA | 8b RED | 8b GREEN | 8b BLACK
					for (int kj = 0; kj < kernel_size; kj++) {
						new_value[0] += ((dst.getRGB(i + ki - shift, j + kj - shift) >> 16) & 0xFF) * kernel[ki][kj];
						new_value[1] += ((dst.getRGB(i + ki - shift, j + kj - shift) >> 8) & 0xFF) * kernel[ki][kj];
						new_value[2] += (dst.getRGB(i + ki - shift, j + kj - shift) & 0xFF) * kernel[ki][kj];
					}
				}
				// nie do końca rozumiem czemu to działa ale działa
				dst.setRGB(i, j, (new_value[0] / kernel_sum) << 16 | (new_value[1] / kernel_sum) << 8
						| (new_value[2] / kernel_sum));
			}
		}
		return dst;
	}

	// ta metoda jest raczej niepotrzebna
	public void update_kernel(int[][] k) {
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
	}
}