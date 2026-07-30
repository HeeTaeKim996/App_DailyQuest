package com.example.dailyquest.Interface.FixedTodo;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.dailyquest.Data.Fixed.FixedCategory;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_None;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyMonth;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyWeek;
import com.example.dailyquest.Data.Fixed.FixedCategoryChild.FixedCategory_everyYear;
import com.example.dailyquest.Data.Fixed.FixedCategoryEnum;
import com.example.dailyquest.R;
import com.example.dailyquest.Utils.CalenderUtils;
import com.example.dailyquest.Utils.InformUtils;
import com.example.dailyquest.databinding.UtilsOneNumberPickerBinding;
import com.example.dailyquest.databinding.UtilsOneSpinnerPickerBinding;
import com.example.dailyquest.databinding.UtilsTwoNumberPickerBinding;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FixedTodoCategorySetPanel extends LinearLayout
{
    private FixedCategory category;
    private Consumer<FixedCategory> outFixedCategoryListener;

    private Spinner categorySpinner;
    private Button setSpecificButton;
    private TextView summaryText;
    private Button okButton;

    String[] categoryStrs = { "None", "EveryYear" };


    public FixedTodoCategorySetPanel(Context context)
    { super(context); }

    public FixedTodoCategorySetPanel(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public FixedTodoCategorySetPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        categorySpinner = findViewById(R.id.spinner_setFixedCategory);
        setSpecificButton = findViewById(R.id.button_fixedTodoSetCategory_setSpecific);
        summaryText = findViewById(R.id.textView_setFixedCategory_categorySummary);
        okButton = findViewById(R.id.button_fixedTodoSetCategory_okButton);
    }

    public void initialize(Consumer<FixedCategory> outFixedCategoryFunc, FixedCategory InCategory)
    {
        Context context = getContext();

        outFixedCategoryListener = outFixedCategoryFunc;
        category = InCategory;

        updateSummary();

        ArrayAdapter<FixedCategoryEnum> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, FixedCategoryEnum.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setSelection(category.fixedCategoryEnum.ordinal());

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                FixedCategoryEnum selectedEnum = (FixedCategoryEnum) parent.getItemAtPosition(pos);
                if(selectedEnum == category.fixedCategoryEnum) return;

                switch(selectedEnum)
                {
                    case NONE:
                        category = new FixedCategory_None();
                        break;

                    case EVERY_YEAR:
                        category = new FixedCategory_everyYear();
                        break;

                    case EVERY_MONTH:
                        category = new FixedCategory_everyMonth();
                        break;

                    case EVERY_WEEK:
                        category = new FixedCategory_everyWeek();
                        break;

                    default:
                        InformUtils.instance().ShowInformYes(context,
                                "FixedTodoCategorySEtPanel : 아직 할당 안한 Category " +
                                        "생성 있음");
                        break;
                }

                updateSummary();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView){}
        });

        okButton.setOnClickListener(v->
        {
            if(outFixedCategoryListener != null)
            {
                outFixedCategoryListener.accept(category);
            }
        });

        setSpecificButton.setOnClickListener(v-> { showSpecificSetPanel(context);});
    }

    private void updateSummary()
    {
        summaryText.setText(category.getSummary());
    }

    private void showSpecificSetPanel(Context context)
    {
        switch(category.fixedCategoryEnum)
        {
            case NONE:
                break;

            case EVERY_YEAR:
                showSpecific_EVERY_YEAR(context);
                break;

            case EVERY_MONTH:
                showSpecific_EVERY_MONTH(context);
                break;

            case EVERY_WEEK:
                showSpecific_EVERY_WEEK(context);
                break;

            default:
                InformUtils.instance().ShowInformYes(context,
                        "FixedTodoCategorySEtPanel : 아직 할당 안한 Category " +
                                "Specific 처리 있음");
                break;
        }


    }

    private void showSpecific_EVERY_YEAR(Context context)
    {
        if(category instanceof FixedCategory_everyYear == false) return;

        FixedCategory_everyYear everyYear = (FixedCategory_everyYear) category;


        UtilsTwoNumberPickerBinding binding = UtilsTwoNumberPickerBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        binding.numberPickerUtilsTwoNumberPickerFirst.setMinValue(1);
        binding.numberPickerUtilsTwoNumberPickerFirst.setMaxValue(12);
        binding.numberPickerUtilsTwoNumberPickerFirst.setValue((int)everyYear.getMonth());

        binding.numberPickerUtilsTwoNumberPickerSecond.setMinValue(1);
        binding.numberPickerUtilsTwoNumberPickerSecond.setMaxValue(31);
        binding.numberPickerUtilsTwoNumberPickerSecond.setValue((int)everyYear.getDate());

        binding.buttonUtilsTwoNumberPickerOk.setOnClickListener(v->
        {
            everyYear.setMonth((byte)binding.numberPickerUtilsTwoNumberPickerFirst.getValue());
            everyYear.setDate((byte)binding.numberPickerUtilsTwoNumberPickerSecond.getValue());

            updateSummary();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void showSpecific_EVERY_MONTH(Context context)
    {
        if(category instanceof FixedCategory_everyMonth == false) return;

        FixedCategory_everyMonth everyMonth = (FixedCategory_everyMonth) category;

        UtilsOneNumberPickerBinding binding = UtilsOneNumberPickerBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        binding.numberPickerUtilsOneNumberPickerFirst.setMinValue(1);
        binding.numberPickerUtilsOneNumberPickerFirst.setMaxValue(31);
        binding.numberPickerUtilsOneNumberPickerFirst.setValue((int)everyMonth.getDate());

        binding.buttonUtilsOneNumberPickerOk.setOnClickListener(v->
        {
            everyMonth.setDate((byte)binding.numberPickerUtilsOneNumberPickerFirst.getValue());
            updateSummary();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void showSpecific_EVERY_WEEK(Context context)
    {
        if(category instanceof FixedCategory_everyWeek == false) return;

        FixedCategory_everyWeek everyWeek = (FixedCategory_everyWeek) category;

        UtilsOneSpinnerPickerBinding binding = UtilsOneSpinnerPickerBinding
            .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        final String[] INDEX_TO_DAY = new String[] {
                "일", "월", "화", "수", "목", "금", "토"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, INDEX_TO_DAY);
        binding.spinnerUtilsOneSpinnerPicker.setAdapter(adapter);

        int tempDay = everyWeek.getDay();

        binding.spinnerUtilsOneSpinnerPicker.setSelection
                ((int)everyWeek.getDay());

        binding.buttonUtilsOneSpinnerPickerOk.setOnClickListener(v->
        {
            everyWeek.setDay((byte)binding.spinnerUtilsOneSpinnerPicker.getSelectedItemPosition());
            updateSummary();
            dialog.dismiss();
        });


        dialog.show();
    }

}
