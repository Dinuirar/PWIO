import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {

    private Stage stage;
    private ConvFilter convFilter;
    private String dstDirectoryPath;
    private String srcDirectoryPath;
    private int[][] matrix = {
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1}
    };

    @FXML
    private AnchorPane anchorPanel;

    @FXML
    private Button searchButton;

    @FXML
    private Label pathLabel;

    @FXML
    private TextField pathTextField;

    @FXML
    private Button filterButton;

    @FXML
    void filter(ActionEvent event) {

        srcDirectoryPath = pathTextField.getText();
        if (!srcDirectoryPath.isEmpty()) {
            File directory = new File(srcDirectoryPath);
            dstDirectoryPath = createDstDirectory(srcDirectoryPath);

            if (directory.isDirectory() && directory.list().length > 0) {

                for (File f : directory.listFiles()) {

                    if (ifPicture(f)) {
                        this.filterImage(f);
                    }
                }
                showInfo();
                pathTextField.setText("");
            } else
                showInfoNoDirectory();
        } else
            showInfoNoDirectory();
    }

    @FXML
    void search(ActionEvent event) {

        srcDirectoryPath = getPathFromFileDialog();
        pathTextField.setText(srcDirectoryPath);
    }

    public void filterImage(File srcFile) {
        convFilter = new ConvFilter(matrix);
        BufferedImage srcPic = null;
        BufferedImage tempPic = null;
        try {
            srcPic = ImageIO.read(srcFile);
            File newFilePic = new File(dstDirectoryPath + "\\" + srcFile.getName());
            tempPic = convFilter.filter(srcPic, 4);
            ImageIO.write(tempPic, "jpg", newFilePic);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private String getPathFromFileDialog() {

        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz folder:");
        java.io.File selectedDirectory = directoryChooser.showDialog(stage);
        return srcDirectoryPath = selectedDirectory.getAbsolutePath();
    }

    public String createDstDirectory(String srcDirectory) {

        new File(srcDirectory + "\\result").mkdir();
        File f = new File(srcDirectory + "\\result");
        return f.getPath();
    }

    private boolean ifPicture(File f) {

        String[] extensions = {".jpg", ".png"};
        for (String extension : extensions) {
            if (f.getPath().endsWith(extension))
                return true;
        }
        return false;
    }

    private void showInfo() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("SUPCIO!");
        alert.setHeaderText(null);
        alert.setContentText("Obrazy zosta³y przefiltorwane!");
        alert.showAndWait();
    }

    private void showInfoNoFiles() {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("B£AD!");
        alert.setHeaderText(null);
        alert.setContentText("Wybrany folder jest pusty!");
        alert.showAndWait();
        pathTextField.setText("");
    }

    private void showInfoNoDirectory() {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("B£AD!");
        alert.setHeaderText(null);
        alert.setContentText("Nie wybrano folderu!");
        alert.showAndWait();
        pathTextField.setText("");
    }
}