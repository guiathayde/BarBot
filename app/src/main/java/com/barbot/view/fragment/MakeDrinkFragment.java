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
import com.barbot.view.viewmodel.MainViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class MakeDrinkFragment extends Fragment {

    MainViewModel mainViewModel;

    SecurityPreferences mSecurityPreferences;

    AppDatabase db;

    IngredientRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mainViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MainViewModel.class);

        mSecurityPreferences = new SecurityPreferences(getContext());

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();
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
                mainViewModel.send(msg.toString());
                for (DrinkModel drinkUpdate : drinksUpdated)
                    drinkDao.update(drinkUpdate);
                Toast.makeText(getContext(), "Preparando drink", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getContext(), "Não tem bebida suficiente", Toast.LENGTH_LONG).show();
        });

        return view;
    }
}