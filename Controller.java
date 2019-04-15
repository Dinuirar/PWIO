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

// controller class which provides backend to FXML user interface and uses ConvFilter to filter images
public class Controller {
    private Stage stage;
    private ConvFilter convFilter;
    private String dstDirectoryPath;
    private String srcDirectoryPath;

    // FXML UI elements
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

    // function bound to "filter" button
    @FXML
    void filter(ActionEvent event) {
        srcDirectoryPath = pathTextField.getText();
        if (!srcDirectoryPath.isEmpty()) {
            File directory = new File(srcDirectoryPath);
            dstDirectoryPath = createDstDirectory(srcDirectoryPath);

            // if directory is not empty - call filterImage method on every file
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
        // source picture and temporary picture references
        BufferedImage srcPic = null;
        BufferedImage tempPic = null;
        try {
            srcPic = ImageIO.read(srcFile);
            File newFilePic = new File(dstDirectoryPath + "\\" + srcFile.getName());
            int width = srcPic.getWidth(); int height = srcPic.getHeight();
            // convert image to integer array
            int[] srctab = srcPic.getRGB(0, 0, width, height, null, 0, width);
            // allocate space for a filtered image
            int[] temptab = new int[srctab.length];
            // create new convolutional filter object
            convFilter = new ConvFilter(srctab, 0, (int)srcFile.length(), temptab);
            // create filtered image
            tempPic = convFilter.filter(srcPic);
            // save filtered image
            ImageIO.write(tempPic, "jpg", newFilePic);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    // get path from file dialog
    private String getPathFromFileDialog() {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz folder:");
        java.io.File selectedDirectory = directoryChooser.showDialog(stage);
        return srcDirectoryPath = selectedDirectory.getAbsolutePath();
    }

    // create destination directory
    public String createDstDirectory(String srcDirectory) {
        new File(srcDirectory + "\\result").mkdir();
        File f = new File(srcDirectory + "\\result");
        return f.getPath();
    }

    // check if file is a picture
    private boolean ifPicture(File f) {
        String[] extensions = {".jpg", ".png"};
        for (String extension : extensions) {
            if (f.getPath().endsWith(extension))
                return true;
        }
        return false;
    }

    // show success dialog
    private void showInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sukces");
        alert.setHeaderText(null);
        alert.setContentText("Obrazy zostaly przefiltrowane!");
        alert.showAndWait();
    }

    // show information that given folder is empty
    private void showInfoNoFiles() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setHeaderText(null);
        alert.setContentText("Wybrany folder jest pusty!");
        alert.showAndWait();
        pathTextField.setText("");
    }

    // show information that no directory was given
    private void showInfoNoDirectory() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setHeaderText(null);
        alert.setContentText("Nie wybrano folderu!");
        alert.showAndWait();
        pathTextField.setText("");
    }
}