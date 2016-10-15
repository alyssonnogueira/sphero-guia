package com.pyramitec.sphero_guia;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.command.RollCommand;
import com.orbotix.common.*;
import com.orbotix.classic.*;
import com.orbotix.*;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private ConvenienceRobot mRobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DiscoveryAgentClassic.getInstance().addRobotStateListener( new RobotChangedStateListener(){
            @Override
            public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type){
                switch (type){
                    case Online:
                        if (robot instanceof RobotClassic) {
                            mRobot = new Sphero(robot);
                            TextView textViewConnection = (TextView) findViewById(R.id.textViewSpheroConnection);
                            textViewConnection.setVisibility(GONE);
                            Button buttonGo = (Button) findViewById(R.id.buttonGo);
                            buttonGo.setVisibility(View.VISIBLE);
                            buttonGo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Snackbar.make(view, "VAAAI SPHEEROOOOOOWWW!! :D ", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    if(mRobot!=null)
                                        mRobot.sendCommand( new RollCommand( 60, (float) 0.5, RollCommand.State.GO ) );
                                }
                            });
                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Disconnected:
                        break;
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        // This line assumes that this object is a Context
        try {
            DualStackDiscoveryAgent.getInstance().startDiscovery(this);
        } catch( DiscoveryException e ) {
            //handle exception
        }
    }

    @Override
    protected void onStop() {
        if( mRobot != null )
            mRobot.disconnect();
        super.onStop();
    }
}
