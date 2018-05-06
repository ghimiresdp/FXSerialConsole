/*Originally created by Sudip Ghimire (ghimiresdp@gmail.com)
 *Please Read the License file for more information on FXSerialConsole
 *
 */
package serialConsole;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("FX Serial Console");
        primaryStage.getIcons().add(new Image(String.valueOf(getClass().getResource("res/icon.png"))));
        primaryStage.setScene(new Scene(root, 1200, 650));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
