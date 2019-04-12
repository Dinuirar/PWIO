import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    private BufferedImage srcPic;

    @FXML
    void filter(ActionEvent event) {
        srcDirectoryPath = pathTextField.getText();
        if (!srcDirectoryPath.isEmpty()) {
            File directory = new File(srcDirectoryPath);
            dstDirectoryPath = createDstDirectory(srcDirectoryPath);

            if (directory.isDirectory() && directory.list().length > 0) {
                for (File f : directory.listFiles()) {
                    if (ifPicture(f)) {
                        filterImage(f);
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
        BufferedImage srcPic = null;
        BufferedImage tempPic = null;
        try {
            srcPic = ImageIO.read(srcFile);
            File newFilePic = new File(dstDirectoryPath + "\\" + srcFile.getName());
            int width = srcPic.getWidth(); int height = srcPic.getHeight();
            int[] srctab = srcPic.getRGB(0, 0, width, height, null, 0, width);
            int[] temptab = new int[srctab.length];
            convFilter = new ConvFilter(srctab, 0, (int)srcFile.length(), temptab);
            tempPic = convFilter.filter(srcPic);
            ImageIO.write(tempPic, "jpg", newFilePic);
        } catch (IOException e1) {
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
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText("Obrazy zostaly przefiltrowane!");
        alert.showAndWait();
    }

    private void showInfoNoFiles() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setHeaderText(null);
        alert.setContentText("Wybrany folder jest pusty!");
        alert.showAndWait();
        pathTextField.setText("");
    }

    private void showInfoNoDirectory() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setHeaderText(null);
        alert.setContentText("Nie wybrano folderu!");
        alert.showAndWait();
        pathTextField.setText("");
    }
}