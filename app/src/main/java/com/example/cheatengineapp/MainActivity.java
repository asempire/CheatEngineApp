package com.example.cheatengineapp;


import static com.example.cheatengineapp.Helpers.clearTableExceptHeader;
import static com.example.cheatengineapp.Helpers.getThirdPartyPackages;
import static com.example.cheatengineapp.Helpers.isRootGiven;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.example.cheatengineapp.Helpers.*;
public class MainActivity extends AppCompatActivity {

    private TableRow selectedRow;
    //private Button SelectItem = findViewById(R.id.SelectItem);

    // Functions to help in detecting root
    //https://stackoverflow.com/a/39420232

    public TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(isHeader ? 18 : 16); // Larger font for headers
        textView.setTypeface(null, isHeader ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        return textView;
    }

    private void addRow(TableLayout tableLayout, String user, String pid, String name, boolean isHeader) {
        TableRow row = new TableRow(this);

        // Create TextViews for each column
        TextView userTextView = createTextView(user, isHeader);
        TextView pidTextView = createTextView(pid, isHeader);
        TextView nameTextView = createTextView(name, isHeader);

        // Add TextViews to the row
        row.addView(userTextView);
        row.addView(pidTextView);
        row.addView(nameTextView);

        if (!isHeader) {
            // Set a click listener for selectable rows
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectRow(row);
                }
            });
        }
        // Add the row to the table
        tableLayout.addView(row);
    }

    private void selectRow(TableRow row) {
        // Deselect the currently selected row if there is one
        if (selectedRow != null) {
            selectedRow.setBackgroundColor(Color.TRANSPARENT);
        }

        // Highlight the new row and set it as selected
        row.setBackgroundColor(Color.LTGRAY);
        selectedRow = row;
        Button SelectItem = findViewById(R.id.SelectItem);
        SelectItem.setEnabled(true);

    }





    // On create behaviour
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(!isRootGiven()){
            Toast.makeText(getApplicationContext(), "Root is Not Available Or Not Granted", Toast.LENGTH_LONG).show();
            finish();
        }

        //LinearLayout lst = findViewById(R.id.linearLayout);

        //ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();

        /*for(ActivityManager.RunningAppProcessInfo processInfo : runningProcesses){
            TextView textView = new TextView(this);
            textView.setText(processInfo.processName);
            lst.addView(textView);
            Log.d("MainActivity", "Running Processes: " + processInfo.processName);
        }*/




        TableLayout tableLayout = findViewById(R.id.tableLayout);
        addRow(tableLayout, "USER", "PID", "NAME", true);
        Button AllProcesses = findViewById(R.id.all);
        Button ThirdPartyProcesses =findViewById(R.id.third_party);
        //Button SelectItem = findViewById(R.id.SelectItem);
        Button SelectItem = findViewById(R.id.SelectItem);
        SelectItem.setEnabled(false);

        List<String> thirdPartyPackages = getThirdPartyPackages();

        AllProcesses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    clearTableExceptHeader(tableLayout);
                    Process process = Runtime.getRuntime().exec(new String[]{"su","-c","ps","-A"});
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    reader.readLine(); //Skip first Line

                    while((line = reader.readLine()) != null){
                        String[] parts = line.trim().split("\\s+", 9); // Limit to 9 parts to handle spaces in NAME
                        if (parts.length >= 9) {

                            String user = parts[0];
                            String pid = parts[1];
                            String name = parts[8];
                            addRow(tableLayout, user, pid, name, false);

                        }
                    }
                    reader.close();

                } catch (Exception e){
                    Log.d("MainActivity","Couldn't retrieve processes",e);
                }
            }
        });

        ThirdPartyProcesses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    clearTableExceptHeader(tableLayout);
                    Process process = Runtime.getRuntime().exec(new String[]{"su","-c","ps","-A"});
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    reader.readLine(); //Skip first Line

                    while((line = reader.readLine()) != null){
                        String[] parts = line.trim().split("\\s+", 9); // Limit to 9 parts to handle spaces in NAME
                        if (parts.length >= 9) {

                            String user = parts[0];
                            String pid = parts[1];
                            String name = parts[8];
                            if(thirdPartyPackages.contains(name)) {
                                addRow(tableLayout, user, pid, name, false);
                            }
                        }
                    }
                    reader.close();

                } catch (Exception e){
                    Log.d("MainActivity","Couldn't retrieve processes",e);
                }
            }
        });

        SelectItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedRow != null) {
                    TextView userTextView = (TextView) selectedRow.getChildAt(0); // Get the 'USER' column
                    TextView pidTextView = (TextView) selectedRow.getChildAt(1);  // Get the 'PID' column
                    TextView nameTextView = (TextView) selectedRow.getChildAt(2); // Get the 'NAME' column

                    String user = userTextView.getText().toString();
                    String pid = pidTextView.getText().toString();
                    String processName = nameTextView.getText().toString();

                    // Create an Intent to launch ProcessInteract
                    Intent intent = new Intent(MainActivity.this, ProcessInteract.class);
                    intent.putExtra("USER", user);
                    intent.putExtra("PID", pid);
                    intent.putExtra("NAME", processName);

                    startActivity(intent);
                }
            }

        });

    }
}