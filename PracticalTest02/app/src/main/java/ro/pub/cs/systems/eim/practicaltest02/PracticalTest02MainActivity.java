package ro.pub.cs.systems.eim.practicaltest02;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalTest02MainActivity extends AppCompatActivity {
    private EditText serverPortEditText = null;
    private Button connectButton = null;

    private EditText clientAddressEditText = null;
    private EditText clientPortEditText = null;
    private EditText hourEditText = null;
    private EditText minuteEditText = null;
    private Button setButton = null;
    private Button resetButton = null;
    private Button pollButton = null;
    private TextView alarmStatusTextView = null;

    private ServerThread serverThread = null;
    private ClientThread clientThread = null;

    private class ConnectButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String serverPort = serverPortEditText.getText().toString();
            if (serverPort == null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[Main Activity] Server port should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            serverThread = new ServerThread(Integer.parseInt(serverPort));

            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[Main Activity] Could not create server thread!");
                return;
            }

            serverThread.start();
        }
    }

    private ConnectButtonClickListener connectButtonClickListener = new ConnectButtonClickListener();

    private class SetButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();

            if (clientAddress == null || clientAddress.isEmpty() || clientPort == null || clientPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[Main Activity] Client connection parameters should be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (v.getId() == R.id.set_button) {

                String hour = hourEditText.getText().toString();
                String minute = minuteEditText.getText().toString();

                if (hour == null || hour.isEmpty()
                        || minute == null || minute.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Parameters from client (city / information type) should be filled", Toast.LENGTH_SHORT).show();
                    return;
                }
                alarmStatusTextView.setText(Constants.EMPTY_STRING);

                clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), Constants.SET, hour, minute, alarmStatusTextView);

                clientThread.start();
            } else if (v.getId() == R.id.reset_button) {
                clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), Constants.RESET, null, null, alarmStatusTextView);

                clientThread.start();
            } else if (v.getId() == R.id.poll_button) {
                clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort), Constants.POLL, null, null, alarmStatusTextView);

                clientThread.start();
            }
        }
    }

    private SetButtonClickListener setButtonClickListener = new SetButtonClickListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        serverPortEditText = findViewById(R.id.server_port_edit_text);
        connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(connectButtonClickListener);

        clientAddressEditText = findViewById(R.id.client_address_edit_text);
        clientPortEditText = findViewById(R.id.client_port_edit_text);
        hourEditText = findViewById(R.id.hour_edit_text);
        minuteEditText = findViewById(R.id.minute_edit_text);

        setButton = findViewById(R.id.set_button);
        resetButton = findViewById(R.id.reset_button);
        pollButton = findViewById(R.id.poll_button);

        setButton.setOnClickListener(setButtonClickListener);
        resetButton.setOnClickListener(setButtonClickListener);
        pollButton.setOnClickListener(setButtonClickListener);

        alarmStatusTextView = findViewById(R.id.alarm_status_text_view);
    }

    @Override
    protected void onDestroy() {
        if (serverThread != null) {
            serverThread.stopThread();
        }

        super.onDestroy();
    }
}