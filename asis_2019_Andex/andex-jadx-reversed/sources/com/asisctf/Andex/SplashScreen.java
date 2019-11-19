package com.asisctf.Andex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.asisctf.Andex.models.RegisterationModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {
    TextView name;
    Button send;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_splash_scren);
        if (VERSION.SDK_INT >= 23) {
            isStoragePermissionGranted();
        }
        this.name = (TextView) findViewById(R.id.name);
        this.send = (Button) findViewById(R.id.send);
        SharedPreferences sharedPreferences = getSharedPreferences("register", 0);
        sharedPreferences.getString("uuid", null);
        sharedPreferences.getString("rolid", null);
        this.send.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SplashScreen splashScreen = SplashScreen.this;
                splashScreen.registerNewDevice(splashScreen.name.getText().toString());
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (VERSION.SDK_INT < 23) {
            return true;
        }
        String str = "android.permission.WRITE_EXTERNAL_STORAGE";
        String str2 = "android.permission.READ_EXTERNAL_STORAGE";
        if (checkSelfPermission(str) == 0 && checkSelfPermission(str2) == 0) {
            return true;
        }
        ActivityCompat.requestPermissions(this, new String[]{str, str2}, 1);
        return false;
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (iArr.length > 0) {
            int i2 = iArr[0];
        }
    }

    /* access modifiers changed from: private */
    public void registerNewDevice(String str) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).getReg(str).enqueue(new Callback<RegisterationModel>() {
            public void onResponse(Call<RegisterationModel> call, Response<RegisterationModel> response) {
                String str = "rolid";
                RegisterationModel registerationModel = (RegisterationModel) response.body();
                try {
                    if (registerationModel.code == 200) {
                        Editor edit = SplashScreen.this.getSharedPreferences("register", 0).edit();
                        Utils.enkey = CryptoHandler.b64Decode(registerationModel.data.encryption_key);
                        edit.putString("enkey", registerationModel.data.encryption_key);
                        edit.putString(str, registerationModel.data.role_id);
                        edit.putString("uuid", registerationModel.data.uuid);
                        edit.apply();
                        Intent intent = new Intent(SplashScreen.this.getApplicationContext(), ConfigurationActivity.class);
                        intent.putExtra(str, registerationModel.data.role_id);
                        SplashScreen.this.startActivity(intent);
                        return;
                    }
                    Context applicationContext = SplashScreen.this.getApplicationContext();
                    StringBuilder sb = new StringBuilder();
                    sb.append("code:  ");
                    sb.append(registerationModel.code);
                    Toast.makeText(applicationContext, sb.toString(), 0).show();
                } catch (Exception e) {
                    Toast.makeText(SplashScreen.this.getApplicationContext(), "Failed", 0).show();
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<RegisterationModel> call, Throwable th) {
                Toast.makeText(SplashScreen.this.getApplicationContext(), "Failed", 0).show();
            }
        });
    }
}
