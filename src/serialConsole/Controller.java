package serialConsole;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.awt.*;
public class Controller {

    @FXML
    public Button clearConsoleBtn;
    @FXML
    public ToggleGroup group1;
    @FXML
    public RadioButton asciiRadioBtn;
    @FXML
    public RadioButton hexRadioBtn;
    @FXML
    public RadioButton binaryRadioBtn;
    @FXML
    public CheckBox CRCheckBox;
    @FXML
    public CheckBox LFCheckBox;
    @FXML
    public CheckBox echoCheckBox;
    @FXML
    public TextField dataTextField;
    @FXML
    public Button sendBtn;
    @FXML
    public ComboBox<Integer> baudRateComboBox;
    @FXML
    public CheckBox spaceCheckBox;
    @FXML
    ComboBox<String> portsComboBox;
    @FXML
    Button refreshPortsBtn;
    @FXML
    Button connectBtn;
    @FXML
    Button disconnectBtn;
    @FXML
    TextArea logConsole;
    private SerialPort port = null;


    public void initialize() {
        refreshPorts();
        //list all the common baud rates
        int[] baudRates = {
                110, 300, 600, 1200, 2400, 4800,
                9600, 14400, 19200, 38400, 57600, 74880, 115200,
                128000, 230400, 250000, 500000, 1000000, 2000000};
        for (int baudRate : baudRates
                ) {
            baudRateComboBox.getItems().add(baudRate);
        }
    }

    public void connectPort() {
        if (portsComboBox.getSelectionModel().getSelectedIndex() >= 0) {
            port = runtimeVariables.getInstance().getPortLists()
                    [portsComboBox.getSelectionModel().getSelectedIndex()];
            if (!port.isOpen()) {
                port.openPort();

                if (baudRateComboBox.getSelectionModel().getSelectedIndex() < 0)
                    baudRateComboBox.getSelectionModel().select(6);
                int baudRate = baudRateComboBox.getSelectionModel().getSelectedItem();
                port.setBaudRate(baudRate);
                logConsole.appendText("\nConnected to ");
                logConsole.appendText(runtimeVariables.getInstance().getPortLists()
                        [portsComboBox.getSelectionModel().getSelectedIndex()].getDescriptivePortName());
                logConsole.appendText(" at baud rate of " + baudRate + " Bauds/s");

                try {
                    port.addDataListener(new SerialPortDataListener() {
                        @Override
                        public int getListeningEvents() {
                            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                        }

                        @Override
                        public void serialEvent(SerialPortEvent event) {
                            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                                return;
                            byte[] newData = new byte[port.bytesAvailable()];
                            port.readBytes(newData, newData.length);
                            Platform.runLater(()->processReceivedData(newData));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                connectBtn.setDisable(true);
                disconnectBtn.setDisable(false);
                portsComboBox.setDisable(true);
                refreshPortsBtn.setDisable(true);
                dataTextField.setDisable(false);
                sendBtn.setDisable(false);
            } else {
                runtimeVariables.getInstance().showAlert(Alert.AlertType.ERROR, portsComboBox.getScene().getWindow(),
                        "Port Open Error", "Could not open the port",
                        "The Port you are trying to open could not be opened");
            }
        } else {
            runtimeVariables.getInstance().showAlert(Alert.AlertType.ERROR, portsComboBox.getScene().getWindow(),
                    "Error", "Port Not Selected",
                    "Please select the port to open");
        }
    }

    private void processReceivedData(byte[] newData) {
        StringBuilder builder = new StringBuilder();
        if (hexRadioBtn.isSelected()) {
            for (byte b : newData) {
                builder.append(String.format("%02X",b));
                if (spaceCheckBox.isSelected()) builder.append(" ");
            }
        } else if (asciiRadioBtn.isSelected()) {
            for (byte b : newData) {
                builder.append((char) b);
            }
        } else if (binaryRadioBtn.isSelected()) {
            builder = new StringBuilder();
            for (byte b : newData) {
                builder.append(Integer.toBinaryString(b&0xFF).replace(' ', '0'));
                if (spaceCheckBox.isSelected()) builder.append(" ");
            }
        }
        logConsole.appendText(builder.toString());
    }

    public void disconnectPort() {
        connectBtn.setDisable(false);
        disconnectBtn.setDisable(true);
        portsComboBox.setDisable(false);
        refreshPortsBtn.setDisable(false);
        dataTextField.setDisable(true);
        sendBtn.setDisable(true);
        if (port.isOpen()) {
            port.closePort();
            logConsole.appendText("\n" + port.getDescriptivePortName());
            logConsole.appendText(" Disconnected Successfully");
        } else {
            runtimeVariables.getInstance().showAlert(Alert.AlertType.ERROR, portsComboBox.getScene().getWindow(),
                    "Port Close Error", "Could not close the port",
                    "The Port you are trying to close is not accessible");
        }
    }

    public void refreshPorts() {
        //clear the current ports combo box and search again for new ports.
        portsComboBox.getItems().clear();
        logConsole.appendText("\nRefreshing Ports");
        SerialPort[] ports = SerialPort.getCommPorts();
        logConsole.appendText("\nAvailable Ports:");
        for (SerialPort p : ports) {
            logConsole.appendText("\n->\t" + p.getSystemPortName() + "\t" + p.getDescriptivePortName());
            portsComboBox.getItems().add(p.getSystemPortName() + "\t" + p.getDescriptivePortName());
        }
        runtimeVariables.getInstance().setPortLists(ports);
        if (ports.length > 0) {
            connectBtn.setDisable(false);
            portsComboBox.getSelectionModel().select(0);
        } else
            logConsole.appendText("\nNo serial ports available");
    }

    @FXML
    public void clearLog() {
        logConsole.setText("[FX Serial Console]\n");
    }

    @FXML
    public void sendSerialData() {
        if (echoCheckBox.isSelected()) logConsole.appendText("\n"+dataTextField.getText() + "\n");
        StringBuilder rawData = new StringBuilder();
        rawData.append(dataTextField.getText());
        if (CRCheckBox.isSelected()) rawData.append("\r");
        if (LFCheckBox.isSelected()) rawData.append("\n");
        port.writeBytes(rawData.toString().getBytes(),rawData.toString().length());
        dataTextField.clear();
    }
    //set the baud rate of selected port.
    public void setBaudRate() {
        if (port != null) {
            int baudRate = baudRateComboBox.getSelectionModel().getSelectedItem();
            port.setBaudRate(baudRate);
            logConsole.appendText("\nBaudRate is set to " + baudRate + " Bauds/s");
        }
    }
    // it is needed when we want to limit key typed in HEX or binary mode

    /*public void processKeyType(KeyEvent keyEvent) {
        if(hexRadioBtn.isSelected()){
            // filter keys 0-9, and A-F and new lines, spaces etc.
            if(!keyEvent.getCharacter().matches("[0-9A-Fa-f\n\r\t\b ]*")){
                Toolkit.getDefaultToolkit().beep();
                keyEvent.consume();
            }
        }
        // filter keys 0,1 and new lines, spaces etc.
        else if(binaryRadioBtn.isSelected()){
            if(!keyEvent.getCharacter().matches("[0-1\n\r\t\b ]*")){
                Toolkit.getDefaultToolkit().beep();
                keyEvent.consume();
            }
        }
    }
*/
}
