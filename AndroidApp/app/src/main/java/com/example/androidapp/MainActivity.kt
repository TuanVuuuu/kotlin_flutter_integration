package com.example.androidapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Mở màn hình Flutter khi activity được tạo
        startActivity(
            FlutterActivity.createDefaultIntent(this)
        )

        setContentView(R.layout.activity_main)
    }
}
