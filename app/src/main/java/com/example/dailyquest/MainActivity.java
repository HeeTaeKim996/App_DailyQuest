package com.example.dailyquest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{
    MainInterface mainInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mainInterface = new MainInterface(this);
        setContentView(mainInterface.getRootView());
    }
}