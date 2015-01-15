package MSR605;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Created by bryan_000 on 10/25/2014.
 */
public class MSR605Serial extends SerialPort {

    public MSR605Serial(String portName, int baudRate, int dataBits, int stopBits, int parity) {
        super(portName);
        try {
            // open serial port and initialize parameters
            this.openPort();
            this.setParams(baudRate, dataBits, stopBits, parity);
            //this.setEventsMask(MASK_RXCHAR);

        } catch(SerialPortException e) {
            if (e.getExceptionType().equals(SerialPortException.TYPE_PORT_BUSY)) { // thrown if port is busy
                System.err.println("ERR: The serial port appears to be busy");
            }
            else if (e.getExceptionType().equals(SerialPortException.TYPE_PORT_NOT_FOUND)){ // thrown if port is not found
                System.err.println("ERR: Could not locate port "+portName);
            }
        }

    }
}
