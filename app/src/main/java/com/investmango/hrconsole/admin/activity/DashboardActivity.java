package com.investmango.hrconsole.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.service.LoginActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView loginDashboard = findViewById(R.id.loginDashboard);
        loginDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the behavior for the click event
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}