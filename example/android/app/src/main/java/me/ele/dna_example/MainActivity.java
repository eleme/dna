package me.ele.dna_example;

import android.os.Bundle;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;
import me.ele.dna_annotations.DnaMethod;

public class MainActivity extends FlutterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);

    }

    @DnaMethod
    public String getVersion2() {
        return android.os.Build.VERSION.RELEASE;
    }
}
