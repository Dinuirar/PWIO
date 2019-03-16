import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Controller controller = new Controller();
		try {
			controller.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("End program : " + System.currentTimeMillis() / 1000);

	}

}
