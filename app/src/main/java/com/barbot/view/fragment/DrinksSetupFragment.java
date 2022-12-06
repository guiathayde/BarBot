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
import androidx.lifecycle.ViewModelProvider;
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
import com.barbot.view.viewmodel.MainViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DrinksSetupFragment extends Fragment {

    MainViewModel mainViewModel;

    AppDatabase db;

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

    String nameDrinkOne;
    String quantityDrinkOne;
    String nameDrinkTwo;
    String quantityDrinkTwo;
    String nameDrinkThree;
    String quantityDrinkThree;
    String nameDrinkFour;
    String quantityDrinkFour;
    String nameDrinkFive;
    String quantityDrinkFive;
    String nameDrinkSix;
    String quantityDrinkSix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MainViewModel.class);

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();
    }

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
            quantityTextInputEditTexts.get(i).setText(drinksStored.get(i).getQuantity().toString());
        }

        buttonSave = view.findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(v -> sendDrinksSetup());

        return view;
    }

    private void sendDrinksSetup() {
        nameDrinkOne = inputFieldNameDrinkOne.getText().toString();
        quantityDrinkOne = inputFieldQuantityDrinkOne.getText().toString();

        nameDrinkTwo = inputFieldNameDrinkTwo.getText().toString();
        quantityDrinkTwo = inputFieldQuantityDrinkTwo.getText().toString();

        nameDrinkThree = inputFieldNameDrinkThree.getText().toString();
        quantityDrinkThree = inputFieldQuantityDrinkThree.getText().toString();

        nameDrinkFour = inputFieldNameDrinkFour.getText().toString();
        quantityDrinkFour = inputFieldQuantityDrinkFour.getText().toString();

        nameDrinkFive = inputFieldNameDrinkFive.getText().toString();
        quantityDrinkFive = inputFieldQuantityDrinkFive.getText().toString();

        nameDrinkSix = inputFieldNameDrinkSix.getText().toString();
        quantityDrinkSix = inputFieldQuantityDrinkSix.getText().toString();

        String data = "DrinkSetup" +
                ":" + nameDrinkOne + ":" + quantityDrinkOne +
                ":" + nameDrinkTwo + ":" + quantityDrinkTwo +
                ":" + nameDrinkThree + ":" + quantityDrinkThree +
                ":" + nameDrinkFour + ":" + quantityDrinkFour +
                ":" + nameDrinkFive + ":" + quantityDrinkFive +
                ":" + nameDrinkSix + ":" + quantityDrinkSix + "#";

        mainViewModel.send(data);
        storeDrinksSetup();
    }

    private void storeDrinksSetup() {
        DrinkModelDao drinkDao = db.drinkDao();

        ArrayList<DrinkModel> drinksSetup = new ArrayList<>();

        if (nameDrinkOne.length() > 0 && quantityDrinkOne.length() > 0)
            drinksSetup.add(new DrinkModel(null, nameDrinkOne, Integer.parseInt(quantityDrinkOne)));

        if (nameDrinkTwo.length() > 0 && quantityDrinkTwo.length() > 0)
            drinksSetup.add(new DrinkModel(null, nameDrinkTwo, Integer.parseInt(quantityDrinkTwo)));

        if (nameDrinkThree.length() > 0 && quantityDrinkThree.length() > 0)
            drinksSetup.add(new DrinkModel(null, nameDrinkThree, Integer.parseInt(quantityDrinkThree)));

        if (nameDrinkFour.length() > 0 && quantityDrinkFour.length() > 0)
            drinksSetup.add(new DrinkModel(null, nameDrinkFour, Integer.parseInt(quantityDrinkFour)));

        if (nameDrinkFive.length() > 0 && quantityDrinkFive.length() > 0)
            drinksSetup.add(new DrinkModel(null, nameDrinkFive, Integer.parseInt(quantityDrinkFive)));

        if (nameDrinkSix.length() > 0 && quantityDrinkSix.length() > 0)
            drinksSetup.add(new DrinkModel(null, nameDrinkSix, Integer.parseInt(quantityDrinkSix)));

        if (drinksSetup.size() > 0) {
            drinkDao.deleteAll();

            for (DrinkModel drink : drinksSetup)
                drinkDao.insertAll(drink);

            Toast.makeText(getContext(), "Bebidas salvas com sucesso", Toast.LENGTH_SHORT).show();
        }
    }
}