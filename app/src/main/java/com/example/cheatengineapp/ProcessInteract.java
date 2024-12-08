package com.example.cheatengineapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessInteract extends AppCompatActivity {

    public TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(isHeader ? 18 : 16); // Larger font for headers
        textView.setTypeface(null, isHeader ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        return textView;
    }

    private void addRow(TableLayout tableLayout, String value, boolean isHeader) {
        TableRow row = new TableRow(this);

        // Create TextViews for each column
        TextView userTextView = createTextView(value, isHeader);


        // Add TextViews to the row
        row.addView(userTextView);


        // Add the row to the table
        tableLayout.addView(row);
    }

    public class BinaryUtils {

        // Get the appropriate resource ID for the architecture
        private  int getBinaryResourceId() throws UnsupportedOperationException {
            String arch = Build.SUPPORTED_ABIS[0];
            switch (arch) {
                case "arm64-v8a":
                    return R.raw.memread_arm64_v8a;
                case "armeabi-v7a":
                    return R.raw.memread_armeabi_v7a;
                case "x86":
                    return R.raw.memread_x86;
                case "x86_64":
                    return R.raw.memread_x86_64;
                case "riscv64":
                    return R.raw.memread_riscv64;
                default:
                    throw new UnsupportedOperationException("Unsupported architecture: " + arch);
            }
        }

        public  File extractBinary(Context context) throws Exception {
            int resourceId = getBinaryResourceId();

            // Copy binary to internal storage
            File binaryFile = new File(context.getFilesDir(), "memread");
            InputStream in = context.getResources().openRawResource(resourceId);
            FileOutputStream out = new FileOutputStream(binaryFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

            // Make the binary executable
            binaryFile.setExecutable(true);
            Log.i("extractBinary","Extracted binary");
            return binaryFile;
        }
    }


    public static class MemoryMapping {
        String startAddress;
        String endAddress;

        public MemoryMapping(String start, String end) {
            this.startAddress = start;
            this.endAddress = end;
        }


    }

    public static List<MemoryMapping> getRelevantMappings(String pid) {
        List<MemoryMapping> mappings = new ArrayList<>();
        String command = "su -c cat /proc/" + pid + "/maps"; // Command to read the maps file with su

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");

                // Example line format:
                // 7ffdadd57000-7ffdae556000 rw-p 00000000 00:00 0 [stack]

                // Skip invalid lines or malformed entries
                if (parts.length < 6) continue;

                // Extract permissions and offset/inode
                String range = parts[0];
                String permissions = parts[1];
                String inode = parts[4];

                // Check for `rw` permissions and inode == 0
                if (permissions.startsWith("rw") && "0".equals(inode)) {
                    // Extract start and end addresses
                    String[] addresses = range.split("-");
                    if (addresses.length == 2) {
                        mappings.add(new MemoryMapping(addresses[0], addresses[1]));
                    }
                }
            }

            // Wait for the process to finish and check for errors
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Log.i("getRelevantMappings","Error: Command execution failed with exit code " + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mappings;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_process_interact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String user = intent.getStringExtra("USER");
        String pid = intent.getStringExtra("PID");
        String processName = intent.getStringExtra("NAME");

        Button back = findViewById(R.id.BackButton);

        //TableLayout tableLayout = findViewById(R.id.tableLayout);


        BinaryUtils binutils = new BinaryUtils();
        try {
            File binaryFile = binutils.extractBinary(this);
            List<MemoryMapping> mappings = getRelevantMappings(pid);
            TextView state = findViewById(R.id.State);
            state.setText("Reading...");
            TableLayout tableLayout = findViewById(R.id.tableLayout);

            for (MemoryMapping mapping : mappings) {
                //addRow(tableLayout,mapping.startAddress,mapping.endAddress);
                // Here we execute the relevant binary
                Log.i("Offsets in work","offsets: " + mapping.startAddress+ " " +mapping.endAddress);
                Integer valueSeeked = 27;
                Log.i("Binary Path",  binaryFile.getAbsolutePath());
                String command = "su -c " + binaryFile.getAbsolutePath() + " " + pid + " " + mapping.startAddress + " " + mapping.endAddress + " " + valueSeeked;
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while((line = reader.readLine()) != null){
                    addRow(tableLayout,line,false);
                }

            }
            state.setText("Finished Reading!");


        } catch (Exception e) {
            Log.i("Error in main",e.toString());
            e.printStackTrace();
        }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}