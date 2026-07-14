package com.example.dailyquest;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.example.dailyquest.databinding.YesBinding;
import com.example.dailyquest.databinding.YesOrNoBinding;

import java.util.function.Consumer;

public class InformUtils
{
    private static InformUtils _instance = new InformUtils();
    private InformUtils(){}

    public static InformUtils instance()
    {
        return _instance;
    }


    public void ShowInformYes(Context context, String informString)
    {
        YesBinding binding = YesBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        binding.textViewYesExplain.setText(informString);
        binding.buttonYes.setOnClickListener(v->{dialog.dismiss();});

        dialog.show();
    }

    public void ShowYesOrNo(Context context, String informString, Consumer<Boolean> yesOrNoFunc)
    {
        YesOrNoBinding binding = YesOrNoBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        binding.textViewYesOrNoExplain.setText(informString);
        binding.buttonYesOrNoYes.setOnClickListener(v->
        {
            yesOrNoFunc.accept(true);
            dialog.dismiss();
        });
        binding.buttonYesOrNoNo.setOnClickListener(v->
        {
            yesOrNoFunc.accept(false);
            dialog.dismiss();
        });

        dialog.show();
    }
}
