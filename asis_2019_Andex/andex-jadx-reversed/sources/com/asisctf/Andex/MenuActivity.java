package com.asisctf.Andex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {
    Button profile;
    Button shop;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_menu);
        this.shop = (Button) findViewById(R.id.shop);
        this.profile = (Button) findViewById(R.id.profile);
        this.shop.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MenuActivity.this.startActivity(new Intent(MenuActivity.this.getApplicationContext(), ShopItemActivity.class));
            }
        });
        this.profile.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MenuActivity.this.startActivity(new Intent(MenuActivity.this.getApplicationContext(), ProfileActivity.class));
            }
        });
    }
}
