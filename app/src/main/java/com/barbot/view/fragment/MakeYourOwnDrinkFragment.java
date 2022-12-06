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
import android.widget.TextView;
import android.widget.Toast;

import com.barbot.constant.Constants;
import com.barbot.R;
import com.barbot.SecurityPreferences;
import com.barbot.bluetooth.SerialListener;
import com.barbot.bluetooth.SerialService;
import com.barbot.bluetooth.SerialSocket;
import com.barbot.bluetooth.TextUtil;
import com.barbot.database.AppDatabase;
import com.barbot.model.DrinkModel;
import com.barbot.model.DrinkModelDao;
import com.barbot.view.viewmodel.MainViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MakeYourOwnDrinkFragment extends Fragment {

    MainViewModel mainViewModel;

    AppDatabase db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MainViewModel.class);

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_your_own, container, false);

        TextView noDrinksAvailable = view.findViewById(R.id.textNoDrinksAvailable);

        DrinkModelDao drinkDao = db.drinkDao();
        List<DrinkModel> drinksStored = drinkDao.getAll();

        TextView[] drinksNamesTextView = {
                view.findViewById(R.id.textDrinkOne),
                view.findViewById(R.id.textDrinkTwo),
                view.findViewById(R.id.textDrinkThree),
                view.findViewById(R.id.textDrinkFour),
                view.findViewById(R.id.textDrinkFive),
                view.findViewById(R.id.textDrinkSix),
        };
        for (TextView textNameDrink : drinksNamesTextView)
            textNameDrink.setVisibility(View.GONE);

        TextInputEditText[] drinksQuantitiesTextInputEditText = {
                view.findViewById(R.id.textInputQuantityDrinkOne),
                view.findViewById(R.id.textInputQuantityDrinkTwo),
                view.findViewById(R.id.textInputQuantityDrinkThree),
                view.findViewById(R.id.textInputQuantityDrinkFour),
                view.findViewById(R.id.textInputQuantityDrinkFive),
                view.findViewById(R.id.textInputQuantityDrinkSix),
        };
        for (TextView textInputEditQuantityDrink : drinksQuantitiesTextInputEditText)
            textInputEditQuantityDrink.setVisibility(View.GONE);

        for (int i = 0; i < drinksStored.size(); i++) {
            drinksNamesTextView[i].setText(drinksStored.get(i).getName());
            drinksNamesTextView[i].setVisibility(View.VISIBLE);
            drinksQuantitiesTextInputEditText[i].setVisibility(View.VISIBLE);
        }

        Button makeYourOwnDrinkButton = view.findViewById(R.id.buttonMakeYourOwnDrink);
        makeYourOwnDrinkButton.setOnClickListener(v -> {
            StringBuilder message = new StringBuilder();
            message.append("MakeYourOwnDrink");

            boolean isPossibleMakeDrink = true;
            ArrayList<DrinkModel> drinksUpdated = new ArrayList<>();
            for (int i = 0; i < drinksNamesTextView.length; i++) {
                String quantityString = drinksQuantitiesTextInputEditText[i].getText().toString();
                if (quantityString.length() > 0) {
                    int quantity = Integer.parseInt(quantityString);

                    if (quantity > 0) {
                        DrinkModel drink = drinkDao.findByName(drinksNamesTextView[i].getText().toString());
                        isPossibleMakeDrink = drink.quantity >= quantity;

                        if (isPossibleMakeDrink) {
                            int quantityUpdated = drink.quantity - quantity;
                            drinksUpdated.add(new DrinkModel(drink.uid, drink.getName(), quantityUpdated));
                        } else {
                            break;
                        }
                    }
                }
            }

            if (isPossibleMakeDrink) {
                for (TextInputEditText quantityTextInputEdit : drinksQuantitiesTextInputEditText)
                    message.append(":").append(quantityTextInputEdit.getText().toString());

                message.append("#");

                mainViewModel.send(message.toString());

                for (DrinkModel drinkUpdate : drinksUpdated)
                    drinkDao.update(drinkUpdate);

                Toast.makeText(getContext(), "Preparando drink", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getContext(), "Não tem bebida suficiente", Toast.LENGTH_LONG).show();
        });

        if (drinksStored.size() == 0) {
            makeYourOwnDrinkButton.setVisibility(View.GONE);
            noDrinksAvailable.setVisibility(View.VISIBLE);
        }

        return view;
    }
}