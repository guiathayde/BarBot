package com.barbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MakeYourOwnDrinkFragment extends Fragment implements ServiceConnection, SerialListener {

    private SecurityPreferences mSecurityPreferences;

    private enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;

    private MakeYourOwnDrinkFragment.Connected connected = MakeYourOwnDrinkFragment.Connected.False;
    private boolean initialStart = true;
    private final String newline = TextUtil.newline_crlf;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSecurityPreferences = new SecurityPreferences(getContext());

        deviceAddress = mSecurityPreferences.getStoredString("device");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_your_own, container, false);

        ArrayList<String> drinksNames = new ArrayList<>();
        for (String key : Constants.ALL_DRINKS_NAMES) {
            String drinkName = mSecurityPreferences.getStoredString(key);
            if (drinkName.length() > 0)
                drinksNames.add(drinkName);
        }

        TextView[] drinksNamesTextView = {
                view.findViewById(R.id.textDrinkOne),
                view.findViewById(R.id.textDrinkTwo),
                view.findViewById(R.id.textDrinkThree),
                view.findViewById(R.id.textDrinkFour),
                view.findViewById(R.id.textDrinkFive),
                view.findViewById(R.id.textDrinkSix),
        };

        for (int i = 0; i < drinksNames.size(); i++)
            drinksNamesTextView[i].setText(drinksNames.get(i));

        TextInputEditText[] drinksQuantitiesTextInputEditText = {
                view.findViewById(R.id.textInputQuantityDrinkOne),
                view.findViewById(R.id.textInputQuantityDrinkTwo),
                view.findViewById(R.id.textInputQuantityDrinkThree),
                view.findViewById(R.id.textInputQuantityDrinkFour),
                view.findViewById(R.id.textInputQuantityDrinkFive),
                view.findViewById(R.id.textInputQuantityDrinkSix),
        };

        Button makeYourOwnDrinkButton = view.findViewById(R.id.buttonMakeYourOwnDrink);
        makeYourOwnDrinkButton.setOnClickListener(v -> {
            StringBuilder message = new StringBuilder();
            for (int i = 0; i < drinksNames.size(); i++) {
                if (i == 0)
                    message.append(drinksNames.get(i)).append(":").append(drinksQuantitiesTextInputEditText[i].getText().toString());
                else
                    message.append(":").append(drinksNames.get(i)).append(":").append(drinksQuantitiesTextInputEditText[i].getText().toString());
            }

            send(message.toString());
        });

        return view;
    }

    @Override
    public void onDestroy() {
        if (connected != MakeYourOwnDrinkFragment.Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
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
            connected = MakeYourOwnDrinkFragment.Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = MakeYourOwnDrinkFragment.Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if (connected != MakeYourOwnDrinkFragment.Connected.True) {
            Toast.makeText(getActivity(), "Bluetooth não conectado", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            boolean hexEnabled = false;
            if (hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        String msg = new String(data);
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = MakeYourOwnDrinkFragment.Connected.True;
        Toast.makeText(getContext(), "Bluetooth conectado", Toast.LENGTH_LONG).show();
        send("First message#");
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
        Toast.makeText(getContext(), "connection failed", Toast.LENGTH_LONG).show();
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