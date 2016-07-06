package com.example.zeeshan.recordcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zeeshan on 7/3/2016.
 */
public class CallHelper {

    private MediaRecorder mRecorder = null;
    private static String mFileName = null;
    String number;
    /**
     * Listener to detect incoming calls.
     */
    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                    number = incomingNumber;
                    Log.i("Result", "outside switch---->" + number);
                    Log.i("Result", "Incoming Call" + incomingNumber);
                    Toast.makeText(ctx,
                            "Incoming: " + incomingNumber,
                            Toast.LENGTH_LONG).show();
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i("Result", "inside case---->" + number);
                    DateFormat df = new SimpleDateFormat("yyMMddHHmmss");//yyMMddHHmmss
                    String date = df.format(Calendar.getInstance().getTime());
                    Log.i("Result", "DATE---->" + date);
                    Log.i("Result", "outgoing  Call Picked");
                    Toast.makeText(ctx,
                            "Call Picked Starting Audio Recorder ",
                            Toast.LENGTH_LONG).show();

                    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                    mFileName += "/"+ date + "_" + number + ".3gp";
                    Log.i("Result", "file name---->" + mFileName );

                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setOutputFile(mFileName);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        Log.i("Check", "prepare() failed");
                        e.printStackTrace();
                    }
                    try {
                        Log.i("Check", "Recording Started!!!!!");
                        mRecorder.start();
                    }
                    catch (Exception e){
                        Log.i("Check", "start failed");
                        e.printStackTrace();
                    }

                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    if(mRecorder != null){
                        Log.i("Check", "Recording Finished");
                        mRecorder.release();
                        mRecorder = null;
                        MainActivity.myRealm.beginTransaction();
                        Log.i("Check", "Storing Data!!!!!-->"+mFileName);
                        DataBase dataBase = MainActivity.myRealm.createObject(DataBase.class);
                        dataBase.setName(mFileName);

                        MainActivity.myRealm.commitTransaction();
                    }



                    break;


            }
        }
    }

    /**
     * Broadcast receiver to detect the outgoing calls.
     */
    public class OutgoingReceiver extends BroadcastReceiver {
        public OutgoingReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i("Result", "Outgoing Call" + number);
            Toast.makeText(ctx,
                    "Outgoing: "+number,
                    Toast.LENGTH_LONG).show();
/*
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/audiorecordtest.3gp";
            Log.i("Check", "FileName: "+mFileName);
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.i("Check", "prepare() failed");
            }
         */
        }
    }

    private Context ctx;
    private TelephonyManager tm;
    private CallStateListener callStateListener;
    private OutgoingReceiver outgoingReceiver;


    public CallHelper(Context ctx) {
        this.ctx = ctx;
        callStateListener = new CallStateListener();
        outgoingReceiver = new OutgoingReceiver();
    }

    public void start(){


        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);

    }
    public void stop(){
        tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(outgoingReceiver);

    }
}
