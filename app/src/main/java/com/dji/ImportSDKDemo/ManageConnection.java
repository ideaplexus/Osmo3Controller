package com.dji.ImportSDKDemo;

import java.util.List;

import dji.sdk.sdkmanager.BluetoothDevice;
import dji.sdk.sdkmanager.BluetoothProductConnector;

public class ManageConnection {

    private BluetoothProductConnector _connector = null;
    private BluetoothProductConnector.BluetoothDevicesListCallback bluetoothProductCallback =
            new BluetoothProductConnector.BluetoothDevicesListCallback() {

                @Override
                public void onUpdate(List<BluetoothDevice> list)
                {

                }
            };

}
