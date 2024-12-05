package com.example.cheatengineapp;

import android.app.ActivityManager;
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

public class MainActivity extends AppCompatActivity {



    // Functions to help in detecting root
    //https://stackoverflow.com/a/39420232

    public static boolean isRootAvailable(){
        for(String pathDir : System.getenv("PATH").split(":")){
            if(new File(pathDir, "su").exists()) {
                return true;
            }
        }
        return false;
    }
    public static boolean isRootGiven(){
        if (isRootAvailable()) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = in.readLine();
                if (output != null && output.toLowerCase().contains("uid=0"))
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (process != null)
                    process.destroy();
            }
        }

        return false;
    }
    private TextView createTextView(String text, boolean isHeader) {
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

        // Add the row to the table
        tableLayout.addView(row);
    }
    private void clearTableExceptHeader(TableLayout tableLayout) {
        // Remove all rows except the first one (header)
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }
    }
    private List<String> getThirdPartyPackages() {
        List<String> thirdPartyPackages = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "pm list packages -3"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Remove "package:" prefix
                if (line.startsWith("package:")) {
                    thirdPartyPackages.add(line.replace("package:", "").trim());
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e("MainActivity", "Couldn't retrieve third-party packages", e);
        }
        return thirdPartyPackages;
    }



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



    }
}