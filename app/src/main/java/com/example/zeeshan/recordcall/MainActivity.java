package com.example.zeeshan.recordcall;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    TextView textViewDetectState,tv;
    private Button buttonToggleDetect;
    private Button buttonExit;
    private boolean detectEnabled;
    private static String mFileName = null;
    private MediaPlayer mPlayer = null;
    public static Realm myRealm;

    WifiManager wifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tv = (TextView) findViewById(R.id.textView3);
        textViewDetectState = (TextView) findViewById(R.id.textView2);
        buttonToggleDetect = (Button) findViewById(R.id.buttonDetectToggle);
        buttonExit = (Button) findViewById(R.id.buttonExit);

        myRealm = Realm.getInstance(
                new RealmConfiguration.Builder(getApplicationContext())
                        .name("myOtherRealm.realm")
                        .build()
        );


        wifi = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        if (wifi.getConnectionInfo().getNetworkId() != -1) {
            // connected
            //will implement a method here which will upload local audio files to the server and delete them from here.
            Toast.makeText(getApplicationContext(),
                    "WIFI Detected Will Upload DATA To Server ",
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),
                    "WIFI Not Detected Storing Data In Local DB ",
                    Toast.LENGTH_LONG).show();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void StartRecorder(View v) {
        setDetectEnabled(!detectEnabled);
    }

    public void StopRecorder(View v){
        setDetectEnabled(false);
        MainActivity.this.finish();
    }

    private void setDetectEnabled(boolean enable) {
        detectEnabled = enable;

        Intent intent = new Intent(this, CallDetectService.class);
        if (enable) {
            // start detect service
            Log.i("Result", "start detect service");
            startService(intent);

            buttonToggleDetect.setText("Turn off");
            textViewDetectState.setText("Detecting");
        }
        else {
            // stop detect service
            stopService(intent);

            buttonToggleDetect.setText("Turn on");
            textViewDetectState.setText("Not detecting");
        }
    }
    
    //for now only one audio file will be played every time as this method is used for checking only
    public void PlayRecording(View v){
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/160705175242_03008434629.3gp";
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.i("Check", "prepare() failed");
            e.printStackTrace();
        }

    }

    public void StopPlaying(View v){
        mPlayer.release();
        mPlayer = null;
    }


    public void fetchData(View v){
        RealmResults<DataBase> results1 =
                myRealm.where(DataBase.class).findAll();
        tv.setMovementMethod(new ScrollingMovementMethod());
        for(DataBase c:results1) {
            Log.i("Ans","results1 :"+ c.getName() +"\n");
            tv.setText(tv.getText() + "Audio Record :" + c.getName() + "\n");
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
