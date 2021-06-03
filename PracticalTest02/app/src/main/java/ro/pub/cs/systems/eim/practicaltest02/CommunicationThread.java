package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[Communication Thread] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[Communication Thread] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[Communication Thread] Waiting for parameters from client (op/hour/minute)!");
            String request = bufferedReader.readLine();

            if (request == null || request.isEmpty()) {
                Log.e(Constants.TAG, "[Communication Thread] Error receiving parameters from client (op/hour/minute)!");
                return;
            }

            String[] splitRequest = request.split(",");

            if (splitRequest.length == 1) {
                if (splitRequest[0].contains("reset")) {
                    serverThread.setData(null);
                } else if (splitRequest[0].contains("poll")) {
                    // get time logic
                    Socket timeSocket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
                    bufferedReader = Utilities.getReader(timeSocket);
                    bufferedReader.readLine();
                    String timeRead = bufferedReader.readLine();
                    Log.i(Constants.TAG, "[Communication Thread] The server returned: " + timeRead);

                    AlarmInformation data = serverThread.getData();

                    if (data == null) {
                        printWriter.println("none");
                        printWriter.flush();
                        return;
                    }

                    String[] timeStampSplit = timeRead.split(" ");
                    String[] crtTime = timeStampSplit[2].split(":");

                    if (Integer.parseInt(data.startHour) > Integer.parseInt(crtTime[0])
                            || (Integer.parseInt(data.startHour) == Integer.parseInt(crtTime[0]) && Integer.parseInt(data.startMinute) > Integer.parseInt(crtTime[1]))) {
                        printWriter.println("active");
                        printWriter.flush();
                    } else {
                        printWriter.println("inactive");
                        printWriter.flush();
                    }
                }
            } else {
                if (splitRequest.length < 3) {
                    Log.e(Constants.TAG, "[Communication Thread] Error receiving parameters from client (hour/minute)!");
                    return;
                }

                String hour = splitRequest[1];
                String minute = splitRequest[2];
                Log.i(Constants.TAG, "[Communication Thread] Read: " + hour + ":" + minute);
                if (hour == null || hour.isEmpty() || minute == null || minute.isEmpty()) {
                    Log.e(Constants.TAG, "[Communication Thread] Error receiving parameters from client (hour/minute)!");
                    return;
                }

                serverThread.setData(new AlarmInformation(hour, minute));
            }


        } catch (UnknownHostException unknownHostException) {
            Log.e(Constants.TAG, "[Communication Thread] An exception has occurred: " + unknownHostException.getMessage());
            if (Constants.DEBUG) {
                unknownHostException.printStackTrace();
            }
        }  catch (IOException ioException) {
            Log.e(Constants.TAG, "[Communication Thread] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}
