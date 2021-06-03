package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private String address;
    private int port;
    private String operation;
    private String hour;
    private String minute;
    private TextView alarmStatusTextView;

    private Socket socket;

    public ClientThread(String address, int port, String operation, String hour, String minute, TextView alarmStatusTextView) {
        this.address = address;
        this.port = port;
        this.operation = operation;
        this.hour = hour;
        this.minute = minute;
        this.alarmStatusTextView = alarmStatusTextView;
    }

    public synchronized void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[Client Thread] Could not create socket!");
                return;
            }

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[Client Thread] Buffered Reader / Print Writer are null!");
                return;
            }

            if (operation.equals(Constants.SET)) {
                printWriter.println(operation + "," + hour + "," + minute);
                printWriter.flush();
            } else {
                printWriter.println(operation);
                printWriter.flush();
            }


            String alarmStatus;

            while ((alarmStatus = bufferedReader.readLine()) != null) {
                final String finalizedAlarmStatus = alarmStatus;
                alarmStatusTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        alarmStatusTextView.setText(finalizedAlarmStatus);
                    }
                });
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[Client Thread] An exception has occured: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[Client Thread] An exception has occured: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
