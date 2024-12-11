package com.example.cheatengineapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
    public void getAddressesmemread(Integer input, TableLayout tableLayout, TextView state, File binaryFile, String pid, File offsetsFile){
        try{
            state.setText("Reading...");

            // Execute the binary
            Integer valueSeeked = input;
            String command = "su -c " + binaryFile.getAbsolutePath() + " " + pid + " " + offsetsFile.getAbsolutePath() + " " + valueSeeked;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read and display the output

            while ((line = reader.readLine()) != null) {
                addRow(tableLayout, line, false);
            }

            state.setText("Finished Reading!");
        }catch (Exception e){
            Log.e("Error in reading addresses", e.toString(), e);
        }
    }

    public void getAddressesnDegreeread(Integer input, TableLayout tableLayout, TextView state, File binaryFile, String pid, File offsetsFile){
        try{
            state.setText("Reading...");

            // Execute the binary
            Integer valueSeeked = input;
            String command = "su -c " + binaryFile.getAbsolutePath() + " " + pid + " " + offsetsFile.getAbsolutePath() + " " + valueSeeked;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read and display the output

            while ((line = reader.readLine()) != null) {
                addRow(tableLayout, line, false);
            }

            state.setText("Finished Reading!");
        }catch (Exception e){
            Log.e("Error in reading addresses", e.toString(), e);
        }
    }

    public class BinaryUtils {

        // Get the appropriate resource ID for the architecture
        private  int getBinaryResourceIdmemread() throws UnsupportedOperationException {
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
        private  int getBinaryResourceIdnDegreeRead() throws UnsupportedOperationException {
            String arch = Build.SUPPORTED_ABIS[0];
            switch (arch) {
                case "arm64-v8a":
                    return R.raw.ndegreeread_arm64_v8a;
                case "armeabi-v7a":
                    return R.raw.ndegreeread_armeabi_v7a;
                case "x86":
                    return R.raw.ndegreeread_x86;
                case "x86_64":
                    return R.raw.ndegreeread_x86_64;
                case "riscv64":
                    return R.raw.ndegreeread_riscv64;
                default:
                    throw new UnsupportedOperationException("Unsupported architecture: " + arch);
            }
        }

        public  File extractBinarymemread(Context context) throws Exception {
            int resourceId = getBinaryResourceIdmemread();

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
        public  File extractBinarynDegreeRead(Context context) throws Exception {
            int resourceId = getBinaryResourceIdnDegreeRead();

            // Copy binary to internal storage
            File binaryFile = new File(context.getFilesDir(), "nDegreeRead");
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
        Button searchButton = findViewById(R.id.SearchButton);
        searchButton.setEnabled(false); //Initially not enabled
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        TextInputEditText input = findViewById(R.id.AddressInput);

        BinaryUtils binutils = new BinaryUtils();
        try {
            // Extract binary from assets
            File binaryFilememread = binutils.extractBinarymemread(this);
            File binaryFilenDegreeRead = binutils.extractBinarynDegreeRead(this);
            List<MemoryMapping> mappings = getRelevantMappings(pid);
            TextView state = findViewById(R.id.State);
            state.setText("Preparing to Read...");

            // Create a file to store offsets
            File offsetsFile = new File(getCacheDir(), "offsets.csv");
            File nDegreeSearchValues = new File(getCacheDir(),"nDegreeAddr.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(offsetsFile))) {
                for (MemoryMapping mapping : mappings) {
                    writer.write(mapping.startAddress + "," + mapping.endAddress);
                    writer.newLine();
                }
            }

            // Enable search Button only if there is some text
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Enable the button if there's text; otherwise, disable it
                    searchButton.setEnabled(s.toString().trim().length() > 0);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not needed
                }
            });


            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String inputText = input.getText().toString().trim();
                    input.setText("");

                    try{
                        int InputValue = Integer.parseInt(inputText);
                        if(tableLayout.getChildCount() == 0){
                            getAddressesmemread(InputValue,tableLayout,state, binaryFilememread,pid,offsetsFile);
                        } else{
                            // Implementing other degrees searches
                            List<String> tableValues = new ArrayList<>();
                            for (int i = 0; i < tableLayout.getChildCount(); i++) {
                                View row = tableLayout.getChildAt(i);
                                if (row instanceof TableRow) {
                                    TableRow tableRow = (TableRow) row;


                                    StringBuilder rowValues = new StringBuilder();
                                    for (int j = 0; j < tableRow.getChildCount(); j++) {
                                        View cell = tableRow.getChildAt(j);
                                        if (cell instanceof TextView) {
                                            TextView textView = (TextView) cell;
                                            rowValues.append(textView.getText().toString()).append(" ");
                                        }
                                    }
                                    // Trim and add the row values to the list
                                    tableValues.add(rowValues.toString().trim());


                                }
                            }


                            // Write the values in a file after opening it as rewritable (FileWriter append flag = false)
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nDegreeSearchValues,false))) {
                                for (String mapping : tableValues) {
                                    writer.write(mapping);
                                    writer.newLine();
                                }
                            }
                            tableLayout.removeAllViews(); // Clear Table Layout to be reused
                            Log.i("started reread","ReReading");
                            getAddressesnDegreeread(InputValue,tableLayout,state,binaryFilenDegreeRead,pid,nDegreeSearchValues);

                        }
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), "Please enter a valid integer!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            /*state.setText("Reading...");

            // Execute the binary
            Integer valueSeeked = 27;
            String command = "su -c " + binaryFile.getAbsolutePath() + " " + pid + " " + offsetsFile.getAbsolutePath() + " " + valueSeeked;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read and display the output
            TableLayout tableLayout = findViewById(R.id.tableLayout);
            while ((line = reader.readLine()) != null) {
                addRow(tableLayout, line, false);
            }

            state.setText("Finished Reading!");*/

        } catch (Exception e) {
            Log.e("Error in main", e.toString(), e);
        }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}