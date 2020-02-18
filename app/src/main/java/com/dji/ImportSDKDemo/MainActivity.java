package com.dji.ImportSDKDemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.gimbal.HandheldGimbal;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.afinal.core.AsyncTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;

    //CHECK WHICH TO USE
    private Handler mHandler;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        setContentView(R.layout.activity_main);

        //Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());



    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
               @Override
                public void run() {
                    showToast("registering, pls wait...");
                    DJISDKManager myManager = DJISDKManager.getInstance();
                    Context myContext = MainActivity.this.getApplicationContext();
            DJISDKManager.SDKManagerCallback myCaller = new DJISDKManager.SDKManagerCallback() {
                @Override
                public void onRegister(DJIError djiError) {
                    if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                        showToast("Register Success");


                        Timer timer = new Timer();
//Set the schedule function
                        timer.scheduleAtFixedRate(new TimerTask() {
                                                      @Override
                                                      public void run() {
                                                          showToast("OOOOOO");
                                                          ReadFileAndUpdateOsmo();
                                                      }},0, 1000);

                        // DJISDKManager.getInstance().startConnectionToProduct();
                    } else {
                        showToast("Register sdk fails, please check the bundle id and network connection!");
                    }
                    Log.v(TAG, djiError.getDescription());
                }

                @Override
                public void onProductDisconnect() {
                    Log.d(TAG, "onProductDisconnect");
                    showToast("Product Disconnected");
                    notifyStatusChange();
                }

                @Override
                public void onProductConnect(BaseProduct baseProduct) {
                    Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                    showToast("Product Connected");
                    notifyStatusChange();
                }

                @Override
                public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                              BaseComponent newComponent) {
                    if (newComponent != null) {
                        newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                            @Override
                            public void onConnectivityChange(boolean isConnected) {
                                Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                notifyStatusChange();
                            }
                        });
                    }
                    Log.d(TAG,
                            String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                    componentKey,
                                    oldComponent,
                                    newComponent));
                }

                @Override
                public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                }

                @Override
                public void onDatabaseDownloadProgress(long l, long l1) {

                }
            };
                  myManager.registerApp(myContext, myCaller);

                }
           });
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean _connecting = false;
    private void ConnectToOsmo3(){
        if(_connecting) return;
        _connecting = true;
        if (DJISDKManager.getInstance().getBluetoothProductConnector() == null) {
            ToastUtils.setResultToToast("pls wait the sdk initiation finished");
            _connecting = false;
            return;
        }
        if(ConnectionManager.getInstance().ConnectedToOsmo()) {
            _connecting = false;

            return;
        }
        ConnectionManager.getInstance().FindAndConnectOsmo3();
        _connecting = false;
    }

    private void ReadFileAndUpdateOsmo(){

        if(!ConnectionManager.getInstance().ConnectedToOsmo()){
            ConnectToOsmo3();
            return;
        }
        int yaw =0;
        int pitch = 0;

        try {
            Context  con = createPackageContext("com.vuforia.engine.Pose1", 0);//first app package name is "com.sharedpref1"
            SharedPreferences pref = con.getSharedPreferences("demopref", Context.MODE_PRIVATE);
            String yawString = pref.getString("yaw", "0");
            String pitchString = pref.getString("pitch", "0");
            yaw = Integer.parseInt(yawString);
            pitch = Integer.parseInt(pitchString);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("Not data shared", e.toString());
            return;
        }

        if(Math.abs(pitch) > 180){
            if(pitch > 0) pitch = 360 - pitch;
            else pitch = pitch + 360;
        }
        if(Math.abs(yaw) > 180){
            if(yaw > 0) yaw = 360 - yaw;
            else yaw = yaw + 360;
        }

        HandHeld t = (HandHeld) DJISDKManager.getInstance().getProduct();
        HandheldGimbal j = (HandheldGimbal) t.getGimbal();
        j.rotate(new Rotation.Builder()
                        .mode(RotationMode.RELATIVE_ANGLE)
                        .pitch(pitch)
                        .yaw(yaw)
                        .build()
                ,new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError error) {
                        if(error != null)  ToastUtils.showToast(error.toString());
                    }
                });

    }




}

