package com.barbot.view.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.barbot.R;
import com.barbot.SecurityPreferences;
import com.barbot.bluetooth.SerialListener;
import com.barbot.bluetooth.SerialService;
import com.barbot.bluetooth.SerialSocket;
import com.barbot.bluetooth.TextUtil;
import com.barbot.constant.Constants;
import com.barbot.database.AppDatabase;
import com.barbot.model.DrinkModel;
import com.barbot.model.DrinkModelDao;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DrinksSetupFragment extends Fragment implements ServiceConnection, SerialListener {

    private SecurityPreferences mSecurityPreferences;

    AppDatabase db;

    private enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;

    private DrinksSetupFragment.Connected connected = DrinksSetupFragment.Connected.False;
    private boolean initialStart = true;

    TextInputEditText inputFieldNameDrinkOne;
    TextInputEditText inputFieldQuantityDrinkOne;

    TextInputEditText inputFieldNameDrinkTwo;
    TextInputEditText inputFieldQuantityDrinkTwo;

    TextInputEditText inputFieldNameDrinkThree;
    TextInputEditText inputFieldQuantityDrinkThree;

    TextInputEditText inputFieldNameDrinkFour;
    TextInputEditText inputFieldQuantityDrinkFour;

    TextInputEditText inputFieldNameDrinkFive;
    TextInputEditText inputFieldQuantityDrinkFive;

    TextInputEditText inputFieldNameDrinkSix;
    TextInputEditText inputFieldQuantityDrinkSix;

    Button buttonSave;

    public DrinksSetupFragment() {
    }

    public static DrinksSetupFragment newInstance() {
        return new DrinksSetupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSecurityPreferences = new SecurityPreferences(getContext());

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();

        deviceAddress = getArguments().getString("device");
        deviceAddress = mSecurityPreferences.getStoredString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != DrinksSetupFragment.Connected.False)
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

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drinks_setup, container, false);

        ArrayList<TextInputEditText> nameTextInputEditTexts = new ArrayList<>();
        ArrayList<TextInputEditText> quantityTextInputEditTexts = new ArrayList<>();

        inputFieldNameDrinkOne = view.findViewById(R.id.textInputNameDrinkOne);
        nameTextInputEditTexts.add(inputFieldNameDrinkOne);
        inputFieldQuantityDrinkOne = view.findViewById(R.id.textInputQuantityDrinkOne);
        quantityTextInputEditTexts.add(inputFieldQuantityDrinkOne);

        inputFieldNameDrinkTwo = view.findViewById(R.id.textInputNameDrinkTwo);
        nameTextInputEditTexts.add(inputFieldNameDrinkTwo);
        inputFieldQuantityDrinkTwo = view.findViewById(R.id.textInputQuantityDrinkTwo);
        quantityTextInputEditTexts.add(inputFieldQuantityDrinkTwo);

        inputFieldNameDrinkThree = view.findViewById(R.id.textInputNameDrinkThree);
        nameTextInputEditTexts.add(inputFieldNameDrinkThree);
        inputFieldQuantityDrinkThree = view.findViewById(R.id.textInputQuantityDrinkThree);
        quantityTextInputEditTexts.add(inputFieldQuantityDrinkThree);

        inputFieldNameDrinkFour = view.findViewById(R.id.textInputNameDrinkFour);
        nameTextInputEditTexts.add(inputFieldNameDrinkFour);
        inputFieldQuantityDrinkFour = view.findViewById(R.id.textInputQuantityDrinkFour);
        quantityTextInputEditTexts.add(inputFieldQuantityDrinkFour);

        inputFieldNameDrinkFive = view.findViewById(R.id.textInputNameDrinkFive);
        nameTextInputEditTexts.add(inputFieldNameDrinkFive);
        inputFieldQuantityDrinkFive = view.findViewById(R.id.textInputQuantityDrinkFive);
        quantityTextInputEditTexts.add(inputFieldQuantityDrinkFive);

        inputFieldNameDrinkSix = view.findViewById(R.id.textInputNameDrinkSix);
        nameTextInputEditTexts.add(inputFieldNameDrinkSix);
        inputFieldQuantityDrinkSix = view.findViewById(R.id.textInputQuantityDrinkSix);
        quantityTextInputEditTexts.add(inputFieldQuantityDrinkSix);

        DrinkModelDao drinkDao = db.drinkDao();
        List<DrinkModel> drinksStored = drinkDao.getAll();
        for (int i = 0; i < drinksStored.size(); i++) {
            nameTextInputEditTexts.get(i).setText(drinksStored.get(i).getName());
            quantityTextInputEditTexts.get(i).setText(drinksStored.get(i).getQuantity());
        }

        buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> sendDrinksSetup());

        return view;
    }

    private void sendDrinksSetup() {
        String nameDrinkOne = inputFieldNameDrinkOne.getText().toString();
        String quantityDrinkOne = inputFieldQuantityDrinkOne.getText().toString();

        String nameDrinkTwo = inputFieldNameDrinkTwo.getText().toString();
        String quantityDrinkTwo = inputFieldQuantityDrinkTwo.getText().toString();

        String nameDrinkThree = inputFieldNameDrinkThree.getText().toString();
        String quantityDrinkThree = inputFieldQuantityDrinkThree.getText().toString();

        String nameDrinkFour = inputFieldNameDrinkFour.getText().toString();
        String quantityDrinkFour = inputFieldQuantityDrinkFour.getText().toString();

        String nameDrinkFive = inputFieldNameDrinkFive.getText().toString();
        String quantityDrinkFive = inputFieldQuantityDrinkFive.getText().toString();

        String nameDrinkSix = inputFieldNameDrinkSix.getText().toString();
        String quantityDrinkSix = inputFieldQuantityDrinkSix.getText().toString();

        String data = "DrinkSetup" + ":" + nameDrinkOne + ":" + quantityDrinkOne + ":" + nameDrinkTwo + ":" + quantityDrinkTwo + ":" + nameDrinkThree + ":" + quantityDrinkThree + ":" + nameDrinkFour + ":" + quantityDrinkFour + ":" + nameDrinkFive + ":" + quantityDrinkFive + ":" + nameDrinkSix + ":" + quantityDrinkSix;

        send(data);
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = DrinksSetupFragment.Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = DrinksSetupFragment.Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if (connected != DrinksSetupFragment.Connected.True) {
            Toast.makeText(getActivity(), "Bluetooth não conectado", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            boolean hexEnabled = false;
            String newline = TextUtil.newline_crlf;
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

        if (msg.contains("DrinksUpdatedSuccessfully"))
            Toast.makeText(getContext(), "Bebidas atualizadas com sucesso", Toast.LENGTH_LONG).show();
        else if (msg.contains("FailedUpdateDrinks"))
            Toast.makeText(getContext(), "Erro ao atualizar as bebidas", Toast.LENGTH_LONG).show();
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
        connected = DrinksSetupFragment.Connected.True;
        Toast.makeText(getContext(), "Bluetooth conectado", Toast.LENGTH_LONG).show();
        send("InitialSetup#");
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