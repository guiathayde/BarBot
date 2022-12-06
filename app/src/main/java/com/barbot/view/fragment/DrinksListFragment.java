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
import com.barbot.view.viewmodel.MainViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;

public class DrinksListFragment extends Fragment {

    MainViewModel mainViewModel;

    SecurityPreferences mSecurityPreferences;

    AppDatabase db;

    DrinkListRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mainViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MainViewModel.class);

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, Constants.DATABASE_NAME).allowMainThreadQueries().build();
    }

    private void storeDrinkInformationInSecurityPreferences(Integer drinkResourceImageId, String drinkName, ArrayList<DrinkListModel.Ingredient> ingredients) {
        mSecurityPreferences.storeString("drinkResourceImageId", drinkResourceImageId.toString());
        mSecurityPreferences.storeString("drinkName", drinkName);

        Gson gson = new Gson();
        String jsonIngredients = gson.toJson(ingredients);
        mSecurityPreferences.storeString("ingredients", jsonIngredients);
    }

    private void changeScreenToMakeDrinkFragment() {
        getFragmentManager().beginTransaction().replace(R.id.fragment, new MakeDrinkFragment(), "make_drink").addToBackStack(null).commit();
    }

    private void changeScreenToMakeYourOwnDrinkFragment() {
        getFragmentManager().beginTransaction().replace(R.id.fragment, new MakeYourOwnDrinkFragment(), "make_your_own_drink").addToBackStack(null).commit();
    }

    private void renderDrinksList(View view) {
        ArrayList<DrinkListModel.Ingredient> caipirinhaIngredients = new ArrayList<>();
        caipirinhaIngredients.add(new DrinkListModel.Ingredient("Vodka", 50, 1));
        caipirinhaIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 100, 3));

        ArrayList<DrinkListModel.Ingredient> blueLagoonIngredients = new ArrayList<>();
        blueLagoonIngredients.add(new DrinkListModel.Ingredient("Vodka", 50, 1));
        blueLagoonIngredients.add(new DrinkListModel.Ingredient("Licor Curaçau Blue Stock", 25, 5));
        blueLagoonIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 150, 3));

        ArrayList<DrinkListModel.Ingredient> cosmoIngredients = new ArrayList<>();
        cosmoIngredients.add(new DrinkListModel.Ingredient("Vodka", 40, 1));
        cosmoIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 30, 2));
        cosmoIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 15, 3));

        ArrayList<DrinkListModel.Ingredient> lemonDropIngredients = new ArrayList<>();
        lemonDropIngredients.add(new DrinkListModel.Ingredient("Vodka", 50, 1));
        lemonDropIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 30, 3));
        lemonDropIngredients.add(new DrinkListModel.Ingredient("Água com açúcar", 30, 4));

        ArrayList<DrinkListModel.Ingredient> blueMoonIngredients = new ArrayList<>();
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Vodka", 30, 1));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 50, 2));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 30, 3));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Água com açúcar", 20, 4));
        blueMoonIngredients.add(new DrinkListModel.Ingredient("Licor Curaçau Blue Stock", 20, 5));

        ArrayList<DrinkListModel.Ingredient> blueGinMoonIngredients = new ArrayList<>();
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 50, 2));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 30, 3));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Água com açúcar", 20, 4));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Licor Curaçau Blue Stock", 20, 5));
        blueGinMoonIngredients.add(new DrinkListModel.Ingredient("Gin", 30, 6));

        ArrayList<DrinkListModel.Ingredient> doubleStrikeIngredients = new ArrayList<>();
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Vodka", 30, 1));
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 50, 2));
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 30, 3));
        doubleStrikeIngredients.add(new DrinkListModel.Ingredient("Licor Curaçau Blue Stock", 20, 5));

        ArrayList<DrinkListModel.Ingredient> tomCollinsIngredients = new ArrayList<>();
        tomCollinsIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 30, 3));
        tomCollinsIngredients.add(new DrinkListModel.Ingredient("Água com açúcar", 30, 4));
        tomCollinsIngredients.add(new DrinkListModel.Ingredient("Gin", 35, 6));

        ArrayList<DrinkListModel.Ingredient> flyingDutchmanIngredients = new ArrayList<>();
        flyingDutchmanIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 20, 3));
        flyingDutchmanIngredients.add(new DrinkListModel.Ingredient("Água com açúcar", 15, 4));
        flyingDutchmanIngredients.add(new DrinkListModel.Ingredient("Gin", 30, 6));

        ArrayList<DrinkListModel.Ingredient> londonCosmoIngredients = new ArrayList<>();
        londonCosmoIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 80, 2));
        londonCosmoIngredients.add(new DrinkListModel.Ingredient("Gin", 30, 6));

        ArrayList<DrinkListModel.Ingredient> vodkaCranberryIngredients = new ArrayList<>();
        vodkaCranberryIngredients.add(new DrinkListModel.Ingredient("Vodka", 30, 1));
        vodkaCranberryIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 80, 2));
        vodkaCranberryIngredients.add(new DrinkListModel.Ingredient("Água com açúcar", 20, 4));

        ArrayList<DrinkListModel.Ingredient> cranberryGinIngredients = new ArrayList<>(); // Vodka - 1, CranberryJuice - 2, LemonJuice - 3, SugarWater - 4, BlueCur - 5, Gin - 6
        cranberryGinIngredients.add(new DrinkListModel.Ingredient("Xarope de cranberry", 80, 2));
        cranberryGinIngredients.add(new DrinkListModel.Ingredient("Suco de limão", 30, 3));
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
        drinks.add(new DrinkListModel("Faça o seu", R.drawable.mystery_drink, makeYourOwnIngredients));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewDrinksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DrinkListRecyclerViewAdapter(getContext(), drinks);
        adapter.setClickListener((v, position) -> {
            DrinkListModel drink = adapter.getItem(position);
            storeDrinkInformationInSecurityPreferences(drink.getDrinkImageResourceId(), drink.getDrinkName(), drink.getIngredients());
            if (drink.getDrinkName() == "Faça o seu")
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
            getFragmentManager().beginTransaction().replace(R.id.fragment, new DrinksSetupFragment(), "drinks_setup").addToBackStack(null).commit();
            return true;
        } else if (id == R.id.debug_settings) {
            getFragmentManager().beginTransaction().replace(R.id.fragment, new TerminalFragment(), "terminal").addToBackStack(null).commit();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}