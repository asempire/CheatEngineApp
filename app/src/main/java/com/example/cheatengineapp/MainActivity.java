package com.example.cheatengineapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

        Button GetIdRoot = findViewById(R.id.GetIDAsRoot);
        Button GetIdUser = findViewById(R.id.GetIDAsUser);
        TextView IdText = findViewById(R.id.idValue);

        GetIdRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Process process = Runtime.getRuntime().exec(new String[]{"su","-c","id"});
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = input.readLine();
                    IdText.setText(line);
                } catch (Exception e){
                    Log.e("MainActivity", "Error in id exec as root", e);
                }
            }
        });

        GetIdUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Process process = Runtime.getRuntime().exec(new String[]{"id"});
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = input.readLine();
                    IdText.setText(line);
                } catch (Exception e){
                    Log.e("MainActivity", "Error in id exec as user", e);
                }
            }
        });
    }
}