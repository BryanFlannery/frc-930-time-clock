package MSR605;

import jssc.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bryan_000 on 10/25/2014.
 */
public class MSR605 implements SerialPortEventListener {

    private List _listeners = new ArrayList();

    public enum MSR_RESPONSE {
        READ, WRITE, COMM_TEST, ERASE
    };

    public enum MSR_EVENT {
        READ_OK, WRITE_OK, READ_FAIL, WRITE_FAIL, ERASE_OK, ERASE_FAIL, COMM_TEST_OK, COMM_TEST_FAIL
    }

    private int ESC = 27;

    private MSR_RESPONSE expectedResponse = MSR_RESPONSE.READ;
    private int[] inputBuffer = new int[512];
    public MSR605Serial port;
    private int inputIndex = 0;

    public MSR605() {
        SerialPortList list = new SerialPortList();
        String [] ports = list.getPortNames("USB\\VID_067B&PID_2303");
        System.out.println(Arrays.toString(ports));
        port = new MSR605Serial(ports[0], 9600, 8, 1, 0);
        try {
            port.addEventListener(this);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

    }

    private synchronized void fireMSR605Event( MSR_EVENT type) {
        MSR605Event event = new MSR605Event(this, type);
        for (Object _listener : _listeners) {
            ((MSR605EventListener) _listener).MSR605EventFire(event);
        }
    }

    public synchronized  void addEventListener(MSR605EventListener l) {
        _listeners.add(l);
    }

    public synchronized  void removeEventListener(MSR605EventListener l) {
        _listeners.remove(l);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventValue() == SerialPortEvent.RXCHAR) {
            handleInputBuffer();
        }
    }

    public void clearBuffers() {
        inputIndex = 0;
        inputBuffer = new int[512];
    }

    public void handleInputBuffer() {
        try {
            //System.out.println("BytesCount: "+port.getInputBufferBytesCount());
            int[] in = port.readIntArray();
            for (int anIn : in) {
                //System.out.println(anIn + " = " + (char) anIn);
                inputBuffer[inputIndex++] = anIn;
                if ((inputIndex >= 2) && (inputBuffer[inputIndex - 2] == 0x1B)) {
                    //System.out.println("ESC-> "+anIn);
                    int valid = 999;
                    int[] error = new int[10];
                    switch (expectedResponse) {
                        case WRITE:
                        case READ: valid = 0x30;
                            error = new int[]{0x31, 0x32, 0x34, 0x39};
                            break;
                        case ERASE: valid = 0x30;
                            error = new int[]{0x41};
                            break;
                        case COMM_TEST: valid = 0x79;
                            break;
                    }
                    //System.out.println(expectedResponse.toString());
                    //System.out.println("Valid: "+valid);
                    //System.out.println("Error: "+Arrays.toString(error));

                    if (anIn == valid) {
                        switch (expectedResponse) {
                            case WRITE: fireMSR605Event(MSR_EVENT.WRITE_OK);
                                break;
                            case READ: fireMSR605Event(MSR_EVENT.READ_OK);
                                break;
                            case  ERASE: fireMSR605Event(MSR_EVENT.ERASE_OK);
                                break;
                            case COMM_TEST: fireMSR605Event(MSR_EVENT.COMM_TEST_OK);
                                break;
                        }
                    } else {
                        for (int x: error) {
                            if (x == anIn) {
                                switch (expectedResponse) {
                                    case WRITE: fireMSR605Event(MSR_EVENT.WRITE_FAIL);
                                        break;
                                    case READ: fireMSR605Event(MSR_EVENT.READ_FAIL);
                                        break;
                                    case  ERASE: fireMSR605Event(MSR_EVENT.ERASE_FAIL);
                                        break;
                                    case COMM_TEST: fireMSR605Event(MSR_EVENT.COMM_TEST_FAIL);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }


    }

    public String[] parseTrackData() {
        // this will contain our tracks as we parse them from integers
        //System.out.println(Arrays.toString(inputBuffer));
        String[] tracks = new String[3];
        int index = 5; // index for our inputBuffer, starts at 5 because of the lead in characters in the iso format
        int i = 0; //index for our character arrays

        char[] t1 = new char[inputBuffer.length]; // create our char arrays at max length, they will be trimmed later
        while (inputBuffer[index] != (int)'?') { // parse until we find the ending track character [?]
            t1[i++] = (char)inputBuffer[index++]; // pull a character out and advance our counters
        }
        tracks[0] = new String(t1).trim(); // trim the string and place it into the output array

        // repeat for track 2
        i = 0;
        //index+=4;
        char[] t2 = new char[inputBuffer.length];
        while (inputBuffer[index] != (int)'?') {
            t2[i++] = (char)inputBuffer[index++];
        }
        tracks[1] = new String(t2).trim();

        // repeat for track 3
        i = 0;
        //index+=4;
        char[] t3 = new char[inputBuffer.length];
        while (inputBuffer[index] != (int)'?') {
            t3[i++] = (char)inputBuffer[index++];
        }
        tracks[2] = new String(t3).trim();

        return tracks;
    }

    private void sendBuffer(int[] buffer){

        try {
            port.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            port.writeIntArray(buffer);
        } catch (SerialPortException e) {
            System.err.println("There was a problem communicating with the serial device: "+e.getExceptionType());
        }
    }

    /*
    @function MSR_CommTest - Tests communication with the card reader
    @param timeout - number of milliseconds we wait for the card reader to respond
    @return return true on successful test, else false
     */
    public void commTest() {
        clearBuffers();
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 'e';
        expectedResponse = MSR_RESPONSE.COMM_TEST;
        sendBuffer(buff);


        /*int[] response = readBuffer(timeout); // get the write confirmation
        if (response.length == 2) { //make sure the response was the right size
            if (response[1] == (int)'y') { // if the status byte equals 'y' the write was successful
                return true;
            }
        }
        return false;
        */
    }
    /*
        @function MSR_ExitComm - closes the connection to the serial port
         */
    public void exitComm() {
        try {
            port.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    /*
    @function MSR_Reset - issues the reset command to the card reader
     */
    public void reset()
    {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x61;
        sendBuffer(buff);
        //System.out.println(Arrays.toString(buff));
    }

    /*
    @function MSR_All_LED_ON - turns all the LEDs on the card reader on
     */
    public void All_LED_ON() {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x82;
        sendBuffer(buff);
        //System.out.println(Arrays.toString(buff));
    }

    public void All_LED_On() {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x82;
        sendBuffer(buff);
    }

    public void Green_LED_On() {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x83;
        sendBuffer(buff);
    }

    public void Yellow_LED_On() {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x84;
        sendBuffer(buff);
    }

    public void Red_LED_On() {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x85;
        sendBuffer(buff);
    }
    /*
        @function MSR_All_LED_OFF - turns all the LEDs on the card reader off
         */
    public void All_LED_Off() {
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 0x81;
        sendBuffer(buff);
    }

    /*
    @function MSR_Write - writes an iso formatted 3 track card
    @param track1 - alphaNumeric string
    @param track2 - Numeric string
    @param track3 - Numeric string
    @return returns true on successful data write
     */
    public void write(String track1, String track2, String track3){
        clearBuffers();
        int[] buff = new int[512];
        int len = 0;
        int i;

        // Writing command
        buff[len++] = ESC;
        buff[len++] = 'w';

        //Data Block command
        buff[len++] = ESC;
        buff[len++] = 's';

        // Track 1
        buff[len++] = ESC;
        buff[len++] = 1; // track 1 designator
        i = 0;
        while(i < track1.length()) {
            buff[len++] = track1.charAt(i++); // convert our string to chars and cast them into an int array
        }

        // Track 2
        buff[len++] = ESC;
        buff[len++] = 2; // track 2 designator
        i = 0;
        while(i < track2.length()) {
            buff[len++] = track2.charAt(i++);
        }

        // Track 3
        buff[len++] = ESC;
        buff[len++] = 3 ; // track 3 designator
        i = 0;
        while(i < track3.length()) {
            buff[len++] = track3.charAt(i++);
        }
        expectedResponse = MSR_RESPONSE.WRITE;
        buff[len++] = 0x3f;
        buff[len] = 0x1c;
        sendBuffer(buff); // send the buffer through the serial connection
        /*
        int[] response = readBuffer(timeout); // get the write confirmation
        if (response.length == 2) { //make sure the response was the right size
            if (response[1] == 0x30) { // if the status byte equals 0x30 the write was successful
                return true;
            }
        }
        return false;
        */
    }

    /*
    @function MSR_Read() - tells the card reader to accept a card swipe
     */
    public void read() {
        clearBuffers();
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len] = 'r'; //write command
        expectedResponse = MSR_RESPONSE.READ;
        sendBuffer(buff); //send the buffer through the serial connection

    }

    /*
    @function MSR_Erase - erases all tracks on the card, returns true if successful
    @param timeout - number milliseconds we wait for a response
    @return boolean - returns true on successful erase
     */
    public void erase() {
        clearBuffers();
        int[] buff = new int[512];
        int len = 0;

        buff[len++] = ESC;
        buff[len++] = 'c'; //write command
        buff[len] = 7; // select byte, selects all three tracks
        expectedResponse = MSR_RESPONSE.ERASE;
        sendBuffer(buff); //send the buffer through the serial connection


    }
}





