package com.asisctf.Andex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.asisctf.Andex.models.UserProfileModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    TextView blnc;
    TextView unam;
    Button update;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_profile);
        getProfile(Utils.user_profile_url);
        this.unam = (TextView) findViewById(R.id.uname);
        this.blnc = (TextView) findViewById(R.id.rol);
        this.update = (Button) findViewById(R.id.update);
        this.update.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this.getApplicationContext(), UpdateProfileActivity.class);
                intent.addFlags(268435456);
                ProfileActivity.this.startActivity(intent);
            }
        });
    }

    private void getProfile(String str) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).getUserProf(getSharedPreferences("register", 0).getString("uuid", null), str).enqueue(new Callback<UserProfileModel>() {
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                UserProfileModel userProfileModel = (UserProfileModel) response.body();
                try {
                    if (userProfileModel.code == 200) {
                        TextView textView = ProfileActivity.this.unam;
                        StringBuilder sb = new StringBuilder();
                        sb.append("UserName: ");
                        sb.append(userProfileModel.data.username);
                        textView.setText(sb.toString());
                        TextView textView2 = ProfileActivity.this.blnc;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("balance:  ");
                        sb2.append(userProfileModel.data.blc_currency);
                        textView2.setText(sb2.toString());
                        Utils.usern = userProfileModel.data.username;
                        return;
                    }
                    Context applicationContext = ProfileActivity.this.getApplicationContext();
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("code:  ");
                    sb3.append(userProfileModel.code);
                    Toast.makeText(applicationContext, sb3.toString(), 0).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<UserProfileModel> call, Throwable th) {
                Toast.makeText(ProfileActivity.this.getApplicationContext(), th.getMessage(), 0).show();
            }
        });
    }
}
