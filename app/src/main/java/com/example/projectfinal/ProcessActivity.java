package com.example.projectfinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class ProcessActivity extends AppCompatActivity {

    private BarChart barChart;
    private TextView dataDetail;

    private String monthlyExpenses;
    private String savingsGoal;
    private String duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        barChart = findViewById(R.id.barChart);
        dataDetail = findViewById(R.id.dataDetail);

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        monthlyExpenses = sharedPreferences.getString("monthlyExpenses", "0");
        savingsGoal = sharedPreferences.getString("savingsGoal", "0");
        duration = sharedPreferences.getString("duration", "0");

        String summary = "Monthly Expenses: " + monthlyExpenses + "\n" +
                "Savings Goal: " + savingsGoal + "\n" +
                "Duration: " + duration + " days";
        dataDetail.setText(summary);

        ArrayList<BarEntry> entries = new ArrayList<>();

        float expenses = Float.parseFloat(monthlyExpenses);
        float goal = Float.parseFloat(savingsGoal);
        float days = Float.parseFloat(duration);

        entries.add(new BarEntry(0f, expenses));
        entries.add(new BarEntry(1f, goal));
        entries.add(new BarEntry(2f, days));

        BarDataSet barDataSet = new BarDataSet(entries, "User Data");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.invalidate();

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                String detail = "";

                if (index == 0) {
                    detail = "Monthly Expenses: " + expenses;
                } else if (index == 1) {
                    detail = "Savings Goal: " + goal;
                } else if (index == 2) {
                    detail = "Duration: " + days + " days";
                }

                dataDetail.setText(detail);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProcessActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
