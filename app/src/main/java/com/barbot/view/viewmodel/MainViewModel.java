package com.barbot.view.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;
import androidx.lifecycle.AndroidViewModel;
import com.barbot.bluetooth.SerialListener;
import com.barbot.bluetooth.SerialService;
import com.barbot.bluetooth.SerialSocket;

public class MainViewModel extends AndroidViewModel implements ServiceConnection, SerialListener {

    private Context context;

    public enum Connected {False, Pending, True}

    public String deviceAddress;
    public SerialService service;

    public MainViewModel.Connected connected = MainViewModel.Connected.False;
    public boolean initialStart = true;

    public MainViewModel(Application application) {
        super(application);
        this.context = application;
    }

    public void updateDeviceAddress(String newDeviceAddress) {
        this.deviceAddress = newDeviceAddress;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(deviceAddress != null && initialStart) {
            initialStart = false;
            ((Activity)context).runOnUiThread(this::connect);
        }
    }

    public void send(String str) {
        if (deviceAddress != null) {
            if (connected != MainViewModel.Connected.True) {
                Toast.makeText(context, "bluetooth not connected", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                service.write(str.getBytes());
            } catch (Exception e) {
                onSerialIoError(e);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    public void connect() {
        if (deviceAddress != null) {
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                status("connecting...");
                connected = MainViewModel.Connected.Pending;
                SerialSocket socket = new SerialSocket(context, device);
                service.connect(socket);
            } catch (Exception e) {
                onSerialConnectError(e);
            }
        }
    }

    public void disconnect() {
        if (deviceAddress != null) {
            connected = MainViewModel.Connected.False;
            service.disconnect();
        }
    }

    private void receive(byte[] data) {
        String msg = new String(data);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private void status(String str) {
        Toast.makeText(context, "Status: " + str, Toast.LENGTH_SHORT).show();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        if (deviceAddress != null) {
            status("connected");
            connected = MainViewModel.Connected.True;
        }
    }

    @Override
    public void onSerialConnectError(Exception e) {
        if (deviceAddress != null) {
            status("connection failed: " + e.getMessage());
            disconnect();
            Toast.makeText(context, "connection failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        if (deviceAddress != null) {
            status("connection lost: " + e.getMessage());
            disconnect();
        }
    }
}
