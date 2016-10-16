package com.pyramitec.sphero_guia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.orbotix.ConvenienceRobot;
import com.orbotix.Sphero;
import com.orbotix.calibration.api.CalibrationEventListener;
import com.orbotix.calibration.api.CalibrationImageButtonView;
import com.orbotix.calibration.api.CalibrationView;
import com.orbotix.classic.DiscoveryAgentClassic;
import com.orbotix.common.*;
import java.util.List;

public class MainActivity extends Activity implements DiscoveryAgentEventListener,
        RobotChangedStateListener {

    private static final String TAG = "MainActivity";
    private DiscoveryAgentClassic _currentDiscoveryAgent;
    private ConvenienceRobot _connectedRobot;
    private CalibrationView _calibrationView;
    private CalibrationImageButtonView _calibrationButtonView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupCalibration();
        findViewById(R.id.entire_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                _calibrationView.interpretMotionEvent(event);
                return true;
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Conectando a Sphero");
        builder.setMessage("Quando a Sphero ficar verde, clique no botão azul e aponte o blue tail para a porta");
// Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        _currentDiscoveryAgent = DiscoveryAgentClassic.getInstance();
        startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_currentDiscoveryAgent != null) {
            _currentDiscoveryAgent.removeRobotStateListener(this);
             for (Robot r : _currentDiscoveryAgent.getConnectedRobots()) {
                r.sleep();
            }
        }
    }

   /**
     * Invoked when the discovery agent finds a new available robot, or updates and already available robot
     * @param robots The list of all robots, connected or not, known to the discovery agent currently
     */
    @Override
    public void handleRobotsAvailable(List<Robot> robots) {
            _currentDiscoveryAgent.connect(robots.get(0));
    }

    /**
     * Invoked when a robot changes state. For example, when a robot connects or disconnects.
     * @param robot The robot whose state changed
     * @param type Describes what changed in the state
     */
    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online:
                _currentDiscoveryAgent.stopDiscovery();
                _currentDiscoveryAgent.removeDiscoveryListener(this);
                _calibrationView.setEnabled(true);
                _calibrationButtonView.setEnabled(true);
                _connectedRobot = new Sphero(robot);
                _connectedRobot.setLed(0f, 1f, 0f);

                break;
            case Disconnected:
                _calibrationView.setEnabled(false);
                _calibrationButtonView.setEnabled(false);
                    break;
            default:
                Log.v(TAG, "Not handling state change notification: " + type);
                break;
        }
    }

    /**
     * Sets up the calibration gesture and button
     */
    private void setupCalibration() {
        _calibrationView = (CalibrationView)findViewById(R.id.calibrationView);
        _calibrationView.setShowGlow(true);
        _calibrationView.setCalibrationEventListener(new CalibrationEventListener() {
            @Override
            public void onCalibrationBegan() {
                Log.v(TAG, "Calibration began!");
                _connectedRobot.calibrating(true);
            }

            /**
             * Invoked when the user moves the calibration ring
             * @param angle The angle that the robot has rotated to.
             */
            @Override
            public void onCalibrationChanged(float angle) {
                _connectedRobot.rotate(angle);
            }

            /**
             * Invoked when the user stops the calibration process
             */
            @Override
            public void onCalibrationEnded() {
                _connectedRobot.stop();
                _connectedRobot.calibrating(false);
            }
        });
        _calibrationView.setEnabled(false);
        _calibrationButtonView = (CalibrationImageButtonView) findViewById(R.id.calibrateButton);
        _calibrationButtonView.setCalibrationView(_calibrationView);
        _calibrationButtonView.setEnabled(false);
    }

    private void startDiscovery() {
        try {
            _currentDiscoveryAgent.addDiscoveryListener(this);
            _currentDiscoveryAgent.addRobotStateListener(this);
            _currentDiscoveryAgent.startDiscovery(this);
        } catch (DiscoveryException e) {
            Log.e(TAG, "Could not start discovery. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
