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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barbot.database.AppDatabase;
import com.barbot.constant.*;
import com.barbot.model.DrinkListModel;
import com.barbot.model.DrinkModel;
import com.barbot.model.DrinkModelDao;
import com.barbot.view.adapter.IngredientRecyclerViewAdapter;
import com.barbot.R;
import com.barbot.SecurityPreferences;
import com.barbot.bluetooth.SerialListener;
import com.barbot.bluetooth.SerialService;
import com.barbot.bluetooth.SerialSocket;
import com.barbot.bluetooth.TextUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class MakeDrinkFragment extends Fragment implements ServiceConnection, SerialListener {

    SecurityPreferences mSecurityPreferences;

    AppDatabase db;

    private enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;

    private MakeDrinkFragment.Connected connected = MakeDrinkFragment.Connected.False;
    private boolean initialStart = true;
    private final String newline = TextUtil.newline_crlf;

    IngredientRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSecurityPreferences = new SecurityPreferences(getContext());

        deviceAddress = getArguments().getString("device");
        deviceAddress = mSecurityPreferences.getStoredString("device");

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();
    }

    @Override
    public void onDestroy() {
        if (connected != MakeDrinkFragment.Connected.False)
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_drink, container, false);

        String drinkName = mSecurityPreferences.getStoredString("drinkName");
        TextView drinkNameTextView = view.findViewById(R.id.textMakeDrinkName);
        drinkNameTextView.setText(drinkName);

        String drinkResourceImageId = mSecurityPreferences.getStoredString("drinkResourceImageId");
        ImageView drinkImageView = view.findViewById(R.id.imageMakeDrink);
        drinkImageView.setImageResource(Integer.parseInt(drinkResourceImageId));

        String jsonIngredients = mSecurityPreferences.getStoredString("ingredients");
        Gson gson = new Gson();
        DrinkListModel.Ingredient[] ingredientsItem = gson.fromJson(jsonIngredients,
                DrinkListModel.Ingredient[].class);

        ArrayList<DrinkListModel.Ingredient> ingredients = new ArrayList<>(Arrays.asList(ingredientsItem));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewIngredients);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IngredientRecyclerViewAdapter(getContext(), ingredients);
        adapter.setClickListener((viewItem, position) -> {
            Toast.makeText(getContext(), "Voce clicou " + adapter.getItem(position) + " da posição " + position, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        DrinkModelDao drinkDao = db.drinkDao();

        Button makeDrinkButton = view.findViewById(R.id.buttonMakeDrink);
        makeDrinkButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Voce clicou para fazer " + drinkName, Toast.LENGTH_SHORT).show();

            boolean isPossibleMakeDrink = true;
            ArrayList<DrinkModel> drinksUpdated = new ArrayList<>();
            for (DrinkListModel.Ingredient ingredient : ingredients) {
                DrinkModel drink = drinkDao.findByName(ingredient.getName());
                if (drink != null) {
                    isPossibleMakeDrink = drink.quantity >= ingredient.getQuantity();

                    if (isPossibleMakeDrink) {
                        int quantity = drink.quantity - ingredient.getQuantity();
                        drinksUpdated.add(new DrinkModel(drink.uid, drink.getName(), quantity));
                    } else {
                        break;
                    }
                } else {
                    isPossibleMakeDrink = false;
                    break;
                }
            }

            if (isPossibleMakeDrink) {
                StringBuilder msg = new StringBuilder();
                msg.append("Make ");
                msg.append(drinkName);
                msg.append("#");
                send(msg.toString());
                for (DrinkModel drinkUpdate : drinksUpdated)
                    drinkDao.update(drinkUpdate);
                Toast.makeText(getContext(), "Preparando drink", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getContext(), "Não tem bebida suficiente", Toast.LENGTH_LONG).show();
        });

        return view;
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = MakeDrinkFragment.Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = MakeDrinkFragment.Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if (connected != MakeDrinkFragment.Connected.True) {
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
        connected = MakeDrinkFragment.Connected.True;
        Toast.makeText(getContext(), "Bluetooth conectado", Toast.LENGTH_LONG).show();
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