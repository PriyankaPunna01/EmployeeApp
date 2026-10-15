package com.example.employeeapp;   // change to your package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String JSON_URL = "https://aamras.com/dummy/EmployeeDetails.json";

    private ListView listView;
    private ProgressBar progressBar;
    private TextView statusText;

    private final List<JSONObject> employees = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Employees");

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("employee_json", employees.get(position).toString());
            startActivity(intent);
        });

        loadEmployees();
    }

    private void loadEmployees() {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);

        // Network calls must run off the main thread
        executor.execute(() -> {
            try {
                String json = download(JSON_URL);
                JSONArray array = JsonUtils.findArray(json);

                List<JSONObject> parsed = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.optJSONObject(i);
                    if (o != null) parsed.add(o);
                }
                mainHandler.post(() -> showEmployees(parsed));
            } catch (Exception e) {
                mainHandler.post(() -> showError("Could not load data.\n" + e.getMessage()));
            }
        });
    }

    private String download(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("GET");
        try {
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned HTTP " + code);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private void showEmployees(List<JSONObject> data) {
        progressBar.setVisibility(View.GONE);
        if (data.isEmpty()) {
            showError("No employees found.");
            return;
        }
        employees.clear();
        employees.addAll(data);
        listView.setAdapter(new EmployeeAdapter(employees));
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);
    }

    /** Two-line list rows built on a built-in Android layout. */
    private class EmployeeAdapter extends ArrayAdapter<JSONObject> {
        EmployeeAdapter(List<JSONObject> items) {
            super(MainActivity.this, android.R.layout.simple_list_item_2, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView != null ? convertView
                    : LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            JSONObject o = getItem(position);
            ((TextView) row.findViewById(android.R.id.text1)).setText(JsonUtils.title(o));
            ((TextView) row.findViewById(android.R.id.text2)).setText(JsonUtils.subtitle(o));
            return row;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
