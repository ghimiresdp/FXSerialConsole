/* This is a singleton class created to store all the global runtime variables.
 */

package serialConsole;

import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.control.Alert;
import javafx.stage.Window;

public class runtimeVariables {
    private static runtimeVariables ourInstance = new runtimeVariables();

    public static runtimeVariables getInstance() {
        return ourInstance;
    }
    private  SerialPort selectedPort;
    private SerialPort[] portLists;

    public SerialPort[] getPortLists() {
        return portLists;
    }

    public void setPortLists(SerialPort[] portLists) {
        this.portLists = portLists;
    }

    public SerialPort getSelectedPort() {
        return selectedPort;
    }

    public void setSelectedPort(SerialPort selectedPort) {
        this.selectedPort = selectedPort;
    }

    public void showAlert(Alert.AlertType type,
                          Window parentWindow,
                          String titleText,
                          String headerText,
                          String contentText) {
        Alert alert = new Alert(type);
        alert.setContentText(contentText);
        alert.setHeaderText(headerText);
        alert.setTitle(titleText);
        alert.initOwner(parentWindow);
        alert.showAndWait();
    }

    private runtimeVariables() {
    }

}

