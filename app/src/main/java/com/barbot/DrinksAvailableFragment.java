package com.barbot;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class DrinksAvailableFragment extends Fragment {

    public DrinksAvailableFragment() {
    }

    public static DrinksAvailableFragment newInstance() {
        return new DrinksAvailableFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drinks_available, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_drinks_available, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.drinks_settings) {
            Fragment fragment = new DrinksSetupFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "drinks_setup").addToBackStack(null).commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}