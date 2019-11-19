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
import com.asisctf.Andex.models.UpdateReq;
import com.asisctf.Andex.models.UpdateUserProfileModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileActivity extends AppCompatActivity {
    TextView name;
    Button update;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_update_profile);
        this.name = (TextView) findViewById(R.id.name);
        this.update = (Button) findViewById(R.id.update);
        this.update.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (UpdateProfileActivity.this.name.getText().length() != 0) {
                    String str = Utils.usern;
                    String str2 = Utils.enkey;
                    StringBuilder sb = new StringBuilder();
                    sb.append("{\"username\": \"");
                    sb.append(UpdateProfileActivity.this.name.getText().toString());
                    sb.append("\"}");
                    sb.append(str2);
                    String md5 = CryptoHandler.md5(sb.toString());
                    UpdateReq updateReq = new UpdateReq();
                    updateReq.username = UpdateProfileActivity.this.name.getText().toString();
                    UpdateProfileActivity.this.updateProfile(Utils.user_profile_url, md5, updateReq);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateProfile(String str, String str2, UpdateReq updateReq) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).PostUserProf(getSharedPreferences("register", 0).getString("uuid", null), str2, str, updateReq).enqueue(new Callback<UpdateUserProfileModel>() {
            public void onResponse(Call<UpdateUserProfileModel> call, Response<UpdateUserProfileModel> response) {
                UpdateUserProfileModel updateUserProfileModel = (UpdateUserProfileModel) response.body();
                try {
                    if (updateUserProfileModel.code == 200) {
                        Context applicationContext = UpdateProfileActivity.this.getApplicationContext();
                        StringBuilder sb = new StringBuilder();
                        sb.append(updateUserProfileModel.data.update);
                        sb.append("");
                        Toast.makeText(applicationContext, sb.toString(), 0).show();
                        UpdateProfileActivity.this.startActivity(new Intent(UpdateProfileActivity.this.getApplicationContext(), ProfileActivity.class));
                        return;
                    }
                    Context applicationContext2 = UpdateProfileActivity.this.getApplicationContext();
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("code:  ");
                    sb2.append(updateUserProfileModel.code);
                    Toast.makeText(applicationContext2, sb2.toString(), 0).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<UpdateUserProfileModel> call, Throwable th) {
                Toast.makeText(UpdateProfileActivity.this.getApplicationContext(), th.getMessage(), 0).show();
            }
        });
    }
}
