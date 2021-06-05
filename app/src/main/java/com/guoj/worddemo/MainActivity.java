package com.guoj.worddemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    NavController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = Navigation.findNavController(findViewById(R.id.fragment));
        NavigationUI.setupActionBarWithNavController(this, controller);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        controller.navigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        controller.navigateUp();
        return super.onSupportNavigateUp();
    }
}