package com.barbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

public class BTService implements ServiceConnection, SerialListener {

    private Context context;
    private Activity activity;

    private SecurityPreferences mSecurityPreferences;

    public static BTService instance;

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;

    public TextView receiveText;
    public TextView sendText;
    public TextUtil.HexWatcher hexWatcher;

    private BTService.Connected connected = BTService.Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    public BTService(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        mSecurityPreferences = new SecurityPreferences(activity.getApplicationContext());
        deviceAddress = mSecurityPreferences.getStoredString("device");
    }

    public static BTService getInstance(Activity activity) {
        if (BTService.instance == null) {
            BTService.instance = new BTService(activity);
        }

        return BTService.instance;
    }

    public void onDestroy() {
        if (connected != BTService.Connected.False)
            disconnect();
        activity.getApplicationContext().stopService(new Intent(activity.getApplicationContext(), SerialService.class));
    }

    public void onStart() {
        if(service != null)
            service.attach(this);
        else
            activity.getApplicationContext().startService(new Intent(activity.getApplicationContext(), SerialService.class));
    }

    public void onStop() {
        if(service != null && !activity.isChangingConfigurations())
            service.detach();
    }

    public void onDetach() {
        try { activity.getApplicationContext().unbindService(this); } catch(Exception ignored) {}
    }

    public void onResume() {
        if(initialStart && service != null) {
            initialStart = false;
            activity.runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart) {
            initialStart = false;
            activity.runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = BTService.Connected.Pending;
            SerialSocket socket = new SerialSocket(context, device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = BTService.Connected.False;
        service.disconnect();
    }

    public void send(String str) {
        if(connected != BTService.Connected.True) {
            Toast.makeText(context, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                data = (str + newline).getBytes();
            }
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        if(hexEnabled) {
            receiveText.append(TextUtil.toHexString(data) + '\n');
        } else {
            String msg = new String(data);
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = receiveText.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    @Override
    public void onSerialConnect() {
        status("connected");
        connected = BTService.Connected.True;
        Toast.makeText(context, "bluetooth connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
        Toast.makeText(context, "connection bluetooth failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }
}
