package com.example.cheatengineapp;

import android.content.Context;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class Helpers extends Context {
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


    public static void clearTableExceptHeader(TableLayout tableLayout) {
        // Remove all rows except the first one (header)
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }
    }
    public static List<String> getThirdPartyPackages() {
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
}
