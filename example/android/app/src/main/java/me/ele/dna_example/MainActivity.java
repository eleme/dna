package me.ele.dna_example;

import android.os.Bundle;
import android.util.Log;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;
import me.ele.dna.DnaClient;

public class MainActivity extends FlutterActivity {
    public static String TAG = "DNA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);
        DnaClient.getClient().setiResultCallBack(e -> {
            Log.i(TAG, "error msg:" + e.getMessage());
        });

    }
}
