package com.example.employeeapp;   // change to your package name

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Iterator;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);   // back arrow

        LinearLayout container = findViewById(R.id.detailContainer);

        try {
            JSONObject employee = new JSONObject(getIntent().getStringExtra("employee_json"));
            setTitle(JsonUtils.title(employee));

            // One label + value pair per field in the JSON
            Iterator<String> keys = employee.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = employee.opt(key);
                String text = (value == null || employee.isNull(key)) ? "-" : String.valueOf(value);
                addRow(container, JsonUtils.prettyKey(key), text);
            }
        } catch (Exception e) {
            addRow(container, "Error", "Could not read employee details.");
        }
    }

    private void addRow(LinearLayout container, String label, String value) {
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(13);
        labelView.setTextColor(Color.GRAY);
        labelView.setPadding(0, 24, 0, 0);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(18);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setTextIsSelectable(true);

        container.addView(labelView);
        container.addView(valueView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
