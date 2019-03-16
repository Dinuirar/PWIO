import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class Controller {

	private ConvFilter convFilter;
	private String dstDirectoryPath;
	private int[][] matrix = { { 3, 4, 5 }, { 1, 4, 5 }, { 1, 2, 3 } };

	public void start() throws IOException {

		System.out.print("Podaj œcie¿kê do katalogu z obrazami:");
		Scanner scanner = new Scanner(System.in);
		String directoryPath = scanner.nextLine();

		File directory = new File(directoryPath);
		dstDirectoryPath = createDstDirectory(directoryPath);

		if (directory.list().length > 0) {

			for (File f : directory.listFiles()) {

				if (ifPicture(f)) {
					this.createNewThread(f);
				}
			}
		} else
			System.out.println("Directory is empty!");
		scanner.close();
	}

	public void createNewThread(File srcFile) {

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {

			String threadName = Thread.currentThread().getName();
			System.out.println("Start executor: " + System.currentTimeMillis() / 1000 + " " + threadName);
			convFilter = new ConvFilter(matrix);

			BufferedImage srcPic = null;
			BufferedImage tempPic = null;
			try {
				srcPic = ImageIO.read(srcFile);
				tempPic = ImageIO.read(srcFile);
				File newFilePic = new File(dstDirectoryPath + "\\" + srcFile.getName());
				tempPic = convFilter.filter(srcPic, tempPic, 1, 25);
				ImageIO.write(tempPic, "jpg", newFilePic);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println(
					"End executor: " + System.currentTimeMillis() / 1000 + " " + Thread.currentThread().getName());
			executor.shutdown();
		});
	}

	public String createDstDirectory(String srcDirectory) {

		new File(srcDirectory + "\\result").mkdir();
		File f = new File(srcDirectory + "\\result");
		return f.getPath();
	}

	private boolean ifPicture(File f) {

		String[] extensions = { ".jpg", ".png" };
		for (String extension : extensions) {
			if (f.getPath().endsWith(extension))
				return true;
		}
		return false;
	}
}
