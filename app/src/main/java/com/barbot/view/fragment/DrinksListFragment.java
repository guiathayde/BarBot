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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.barbot.database.AppDatabase;
import com.barbot.constant.Constants;
import com.barbot.model.DrinkListModel;
import com.barbot.view.adapter.DrinkListRecyclerViewAdapter;
import com.barbot.model.DrinkModel;
import com.barbot.model.DrinkModelDao;
import com.barbot.R;
import com.barbot.SecurityPreferences;
import com.barbot.bluetooth.SerialListener;
import com.barbot.bluetooth.SerialService;
import com.barbot.bluetooth.SerialSocket;
import com.barbot.bluetooth.TextUtil;
import com.google.gson.Gson;

import java.util.ArrayList;

public class DrinksListFragment extends Fragment implements ServiceConnection, SerialListener {

    SecurityPreferences mSecurityPreferences;

    AppDatabase db;

    DrinkListRecyclerViewAdapter adapter;

    private enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;

    private DrinksListFragment.Connected connected = DrinksListFragment.Connected.False;
    private boolean initialStart = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mSecurityPreferences = new SecurityPreferences(getContext());

        deviceAddress = mSecurityPreferences.getStoredString("device");

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();
    }

    @Override
    public void onDestroy() {
        if (connected != DrinksListFragment.Connected.False)
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

    private void storeDrinkInformationInSecurityPreferences(Integer drinkResourceImageId, String drinkName, ArrayList<DrinkListModel.Ingredient> ingredients) {
        mSecurityPreferences.storeString("drinkResourceImageId", drinkResourceImageId.toString());
        mSecurityPreferences.storeString("drinkName", drinkName);

        Gson gson = new Gson();
        String jsonIngredients = gson.toJson(ingredients);
        mSecurityPreferences.storeString("ingredients", jsonIngredients);
    }

    private void changeScreenToMakeDrinkFragment() {
        Bundle args = new Bundle();
        args.putString("device", deviceAddress);
        Fragment fragment = new MakeDrinkFragment();
        fragment.setArguments(args);
        disconnect();
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "make_drink").addToBackStack(null).commit();
    }

    private void changeScreenToMakeYourOwnDrinkFragment() {
        Bundle args = new Bundle();
        args.putString("device", deviceAddress);
        Fragment fragment = new MakeYourOwnDrinkFragment();
        fragment.setArguments(args);
        disconnect();
        getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "make_your_own_drink").addToBackStack(null).commit();
    }

    private void renderDrinksList(View view) {
        ArrayList<DrinkListModel.Ingredient> caipirinhaIngredients = new ArrayList<>();
        caipirinhaIngredients.add(new DrinkListModel.Ingredient("Vodka", 50, 1));
        caipirinhaIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 100, 3));

        ArrayList<DrinkListModel.Ingredient> blueLagoonIngredients = new ArrayList<>();
        blueLagoonIngredients.add(new DrinkListModel.Ingredient("Vodka", 50, 1));
        blueLagoonIngredients.add(new DrinkListModel.Ingredient("Licor Cura??au Blue Stock", 25, 5));
        blueLagoonIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 150, 3));

        ArrayList<DrinkListModel.Ingredient> cosmoIngredients = new ArrayList<>();
        cosmoIngredients.add(new DrinkListModel.Ingredient("Vodka", 40, 1));
        cosmoIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 30, 2));
        cosmoIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 15, 3));

        ArrayList<DrinkListModel.Ingredient> lemonDropIngredients = new ArrayList<>();
        lemonDropIngredients.add(new DrinkListModel.Ingredient("Vodka", 50, 1));
        lemonDropIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 30, 3));
        lemonDropIngredients.add(new DrinkListModel.Ingredient("??gua com a????car", 30, 4));

        ArrayList<DrinkListModel.Ingredient> blueMoonIngredients = new ArrayList<>();
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Vodka", 30, 1));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 50, 2));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 30, 3));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("??gua com a????car", 20, 4));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Licor Cura??au Blue Stock", 20, 5));

        ArrayList<DrinkListModel.Ingredient> blueGinMoonIngredients = new ArrayList<>();
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 50, 2));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 30, 3));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("??gua com a????car", 20, 4));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Licor Cura??au Blue Stock", 20, 5));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Gin", 30, 6));

        ArrayList<DrinkListModel.Ingredient> doubleStrikeIngredients = new ArrayList<>();
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Vodka", 30, 1));
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 50, 2));
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 30, 3));
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Licor Cura??au Blue Stock", 20, 5));

        ArrayList<DrinkListModel.Ingredient> tomCollinsIngredients = new ArrayList<>();
        tomCollinsIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 30, 3));
        tomCollinsIngredients.add(new DrinkListModel.Ingredient("??gua com a????car", 30, 4));
        tomCollinsIngredients.add(new DrinkListModel.Ingredient("Gin", 35, 6));

        ArrayList<DrinkListModel.Ingredient> flyingDutchmanIngredients = new ArrayList<>();
        flyingDutchmanIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 20, 3));
        flyingDutchmanIngredients.add(new DrinkListModel.Ingredient("??gua com a????car", 15, 4));
        flyingDutchmanIngredients.add(new DrinkListModel.Ingredient("Gin", 30, 6));

        ArrayList<DrinkListModel.Ingredient> londonCosmoIngredients = new ArrayList<>();
        londonCosmoIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 80, 2));
        londonCosmoIngredients.add(new DrinkListModel.Ingredient("Gin", 30, 6));

        ArrayList<DrinkListModel.Ingredient> vodkaCranberryIngredients = new ArrayList<>();
        vodkaCranberryIngredients.add(new DrinkListModel.Ingredient("Vodka", 30, 1));
        vodkaCranberryIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 80, 2));
        vodkaCranberryIngredients.add(new DrinkListModel.Ingredient("??gua com a????car", 20, 4));

        ArrayList<DrinkListModel.Ingredient> cranberryGinIngredients = new ArrayList<>(); // Vodka - 1, CranberryJuice - 2, LemonJuice - 3, SugarWater - 4, BlueCur - 5, Gin - 6
        cranberryGinIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 80, 2));
        cranberryGinIngredients.add(new DrinkListModel.Ingredient("Suco de lim??o", 30, 3));
        cranberryGinIngredients.add(new DrinkListModel.Ingredient("Gin", 35, 6));

        ArrayList<DrinkListModel.Ingredient> makeYourOwnIngredients = new ArrayList<>();

        ArrayList<DrinkListModel> drinks = new ArrayList<>();
        drinks.add(new DrinkListModel("Caipirinha", R.drawable.caipirinha, caipirinhaIngredients));
        drinks.add(new DrinkListModel("Blue Lagoon", R.drawable.blue_lagoon, blueLagoonIngredients));
        drinks.add(new DrinkListModel("Cosmo", R.drawable.cosmo, cosmoIngredients));
        drinks.add(new DrinkListModel("Lemon Drop", R.drawable.lemon_drop, lemonDropIngredients));
        drinks.add(new DrinkListModel("Blue Moon", R.drawable.blue_moon, blueMoonIngredients));
        drinks.add(new DrinkListModel("Blue Gin Moon", R.drawable.blue_gin_moon, blueGinMoonIngredients));
        drinks.add(new DrinkListModel("Double Strike", R.drawable.double_strike, doubleStrikeIngredients));
        drinks.add(new DrinkListModel("Tom Collins", R.drawable.tom_collins, tomCollinsIngredients));
        drinks.add(new DrinkListModel("Flying Dutchman", R.drawable.flying_dutchman, flyingDutchmanIngredients));
        drinks.add(new DrinkListModel("London Cosmo", R.drawable.london_cosmo, londonCosmoIngredients));
        drinks.add(new DrinkListModel("Vodka Cranberry", R.drawable.vodka_cranberry, vodkaCranberryIngredients));
        drinks.add(new DrinkListModel("Cranberry Gin", R.drawable.cranberry_gin, cranberryGinIngredients));
        drinks.add(new DrinkListModel("Fa??a o seu", R.drawable.mystery_drink, makeYourOwnIngredients));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewDrinksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DrinkListRecyclerViewAdapter(getContext(), drinks);
        adapter.setClickListener((v, position) -> {
            DrinkListModel drink = adapter.getItem(position);
            storeDrinkInformationInSecurityPreferences(drink.getDrinkImageResourceId(), drink.getDrinkName(), drink.getIngredients());
            if (drink.getDrinkName() == "Fa??a o seu")
                changeScreenToMakeYourOwnDrinkFragment();
            else
                changeScreenToMakeDrinkFragment();
        });
        recyclerView.setAdapter(adapter);

        ProgressBar loading = view.findViewById(R.id.progressBarLoadingDrinks);
        loading.setVisibility(View.GONE);

        TextView loadingText = view.findViewById(R.id.textLoadingDrinks);
        loadingText.setVisibility(View.GONE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drinks_list, container, false);

        renderDrinksList(view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_drinks_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.drinks_settings) {
            Bundle args = new Bundle();
            args.putString("device", deviceAddress);
            Fragment fragment = new DrinksSetupFragment();
            fragment.setArguments(args);
            disconnect();
            getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "drinks_setup").addToBackStack(null).commit();
            return true;
        } else if (id == R.id.debug_settings) {
            Bundle args = new Bundle();
            args.putString("device", deviceAddress);
            Fragment fragment = new TerminalFragment();
            fragment.setArguments(args);
            disconnect();
            getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = DrinksListFragment.Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = DrinksListFragment.Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if (connected != DrinksListFragment.Connected.True) {
            Toast.makeText(getActivity(), "Bluetooth n??o conectado", Toast.LENGTH_SHORT).show();
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

        if (msg.contains("InitialSetup")) {
            DrinkModelDao drinkDao = db.drinkDao();
            drinkDao.deleteAll();
            String[] initialSetup = msg.split(":");
            for (int i = 1; i < initialSetup.length; i += 2)
                drinkDao.insertAll(new DrinkModel(null, initialSetup[i], Integer.parseInt(initialSetup[i + 1])));
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void onSerialConnect() {
        status("connected");
        connected = DrinksListFragment.Connected.True;
        Toast.makeText(getContext(), "Bluetooth conectado", Toast.LENGTH_LONG).show();
        send("FirstMessage#");
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