package com.asisctf.Andex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.asisctf.Andex.models.ConfigModel;
import java.io.File;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigurationActivity extends AppCompatActivity {
    TextView state;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_main);
        this.state = (TextView) findViewById(R.id.state);
        getConfiguration(getIntent().getStringExtra("rolid"));
    }

    private void getConfiguration(String str) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).getConf(getSharedPreferences("register", 0).getString("uuid", null), str).enqueue(new Callback<ConfigModel>() {
            public void onResponse(Call<ConfigModel> call, Response<ConfigModel> response) {
                ConfigModel configModel = (ConfigModel) response.body();
                try {
                    if (configModel.code == 200) {
                        ConfigurationActivity.this.getDex1(configModel.data.dex);
                        return;
                    }
                    Context applicationContext = ConfigurationActivity.this.getApplicationContext();
                    StringBuilder sb = new StringBuilder();
                    sb.append("code:  ");
                    sb.append(configModel.code);
                    Toast.makeText(applicationContext, sb.toString(), 0).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ConfigurationActivity.this.getApplicationContext(), "Failed", 0).show();
                }
            }

            public void onFailure(Call<ConfigModel> call, Throwable th) {
                Toast.makeText(ConfigurationActivity.this.getApplicationContext(), "Failed", 0).show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void getDex1(String str) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).getDex(getSharedPreferences("register", 0).getString("uuid", null), str).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ConfigurationActivity.this.state.setText("Decrypting . . . ");
                    ConfigurationActivity.this.writeToDisk((ResponseBody) response.body());
                    File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    StringBuilder sb = new StringBuilder();
                    sb.append(externalStoragePublicDirectory.getAbsolutePath());
                    sb.append("/conf/1plain.dex");
                    String sb2 = sb.toString();
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(externalStoragePublicDirectory.getAbsolutePath());
                    sb3.append("/conf/1.dex");
                    CryptoHandler.decrypt(sb2, sb3.toString(), Utils.enkey);
                    Class classFromDex = DexJob.getClassFromDex(sb2, "com.asisctf.config.SayHelloToYourLittleFriend", ConfigurationActivity.this.getApplicationContext());
                    String[] strArr = (String[]) classFromDex.getMethod("config", new Class[]{Context.class}).invoke(classFromDex.newInstance(), new Object[]{ConfigurationActivity.this.getApplicationContext()});
                    if (strArr.length == 0) {
                        ConfigurationActivity.this.state.setText("can't load configuration, review your registeriation");
                        return;
                    }
                    Utils.config = strArr;
                    Utils.shop_item_url = strArr[0];
                    Utils.shop_order_url = strArr[1];
                    Utils.user_profile_url = strArr[2];
                    ConfigurationActivity.this.startActivity(new Intent(ConfigurationActivity.this.getApplicationContext(), MenuActivity.class));
                } catch (Exception e) {
                    Toast.makeText(ConfigurationActivity.this.getApplicationContext(), "Failed", 0).show();
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable th) {
                Toast.makeText(ConfigurationActivity.this.getApplicationContext(), th.getMessage(), 0).show();
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0098 A[SYNTHETIC, Splitter:B:34:0x0098] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x009d A[Catch:{ IOException -> 0x00ad }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a5 A[Catch:{ IOException -> 0x00ad }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00aa A[Catch:{ IOException -> 0x00ad }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean writeToDisk(okhttp3.ResponseBody r12) {
        /*
            r11 = this;
            java.lang.String r0 = "conf"
            r1 = 0
            java.io.File r2 = new java.io.File     // Catch:{ IOException -> 0x00ad }
            java.lang.String r3 = android.os.Environment.DIRECTORY_DOWNLOADS     // Catch:{ IOException -> 0x00ad }
            java.io.File r3 = android.os.Environment.getExternalStoragePublicDirectory(r3)     // Catch:{ IOException -> 0x00ad }
            r2.<init>(r3, r0)     // Catch:{ IOException -> 0x00ad }
            boolean r3 = r2.exists()     // Catch:{ IOException -> 0x00ad }
            if (r3 != 0) goto L_0x001f
            boolean r3 = r2.mkdirs()     // Catch:{ IOException -> 0x00ad }
            if (r3 != 0) goto L_0x001f
            java.lang.String r3 = "Oops! Failed create conf directory"
            android.util.Log.e(r0, r3)     // Catch:{ IOException -> 0x00ad }
        L_0x001f:
            java.io.File r0 = new java.io.File     // Catch:{ IOException -> 0x00ad }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x00ad }
            r3.<init>()     // Catch:{ IOException -> 0x00ad }
            java.lang.String r2 = r2.getPath()     // Catch:{ IOException -> 0x00ad }
            r3.append(r2)     // Catch:{ IOException -> 0x00ad }
            java.lang.String r2 = java.io.File.separator     // Catch:{ IOException -> 0x00ad }
            r3.append(r2)     // Catch:{ IOException -> 0x00ad }
            java.lang.String r2 = "1.dex"
            r3.append(r2)     // Catch:{ IOException -> 0x00ad }
            java.lang.String r2 = r3.toString()     // Catch:{ IOException -> 0x00ad }
            r0.<init>(r2)     // Catch:{ IOException -> 0x00ad }
            r2 = 4096(0x1000, float:5.74E-42)
            r3 = 0
            byte[] r2 = new byte[r2]     // Catch:{ IOException -> 0x00a1, all -> 0x0093 }
            long r4 = r12.contentLength()     // Catch:{ IOException -> 0x00a1, all -> 0x0093 }
            r6 = 0
            java.io.InputStream r12 = r12.byteStream()     // Catch:{ IOException -> 0x00a1, all -> 0x0093 }
            java.io.FileOutputStream r8 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x0091, all -> 0x008e }
            r8.<init>(r0)     // Catch:{ IOException -> 0x0091, all -> 0x008e }
        L_0x0052:
            int r0 = r12.read(r2)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            r3 = -1
            if (r0 != r3) goto L_0x0066
            r8.flush()     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            r0 = 1
            if (r12 == 0) goto L_0x0062
            r12.close()     // Catch:{ IOException -> 0x00ad }
        L_0x0062:
            r8.close()     // Catch:{ IOException -> 0x00ad }
            return r0
        L_0x0066:
            r8.write(r2, r1, r0)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            long r9 = (long) r0     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            long r6 = r6 + r9
            java.lang.String r0 = "13"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            r3.<init>()     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            java.lang.String r9 = "file download: "
            r3.append(r9)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            r3.append(r6)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            java.lang.String r9 = " of "
            r3.append(r9)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            r3.append(r4)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            java.lang.String r3 = r3.toString()     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            android.util.Log.d(r0, r3)     // Catch:{ IOException -> 0x008c, all -> 0x008a }
            goto L_0x0052
        L_0x008a:
            r0 = move-exception
            goto L_0x0096
        L_0x008c:
            goto L_0x00a3
        L_0x008e:
            r0 = move-exception
            r8 = r3
            goto L_0x0096
        L_0x0091:
            r8 = r3
            goto L_0x00a3
        L_0x0093:
            r0 = move-exception
            r12 = r3
            r8 = r12
        L_0x0096:
            if (r12 == 0) goto L_0x009b
            r12.close()     // Catch:{ IOException -> 0x00ad }
        L_0x009b:
            if (r8 == 0) goto L_0x00a0
            r8.close()     // Catch:{ IOException -> 0x00ad }
        L_0x00a0:
            throw r0     // Catch:{ IOException -> 0x00ad }
        L_0x00a1:
            r12 = r3
            r8 = r12
        L_0x00a3:
            if (r12 == 0) goto L_0x00a8
            r12.close()     // Catch:{ IOException -> 0x00ad }
        L_0x00a8:
            if (r8 == 0) goto L_0x00ad
            r8.close()     // Catch:{ IOException -> 0x00ad }
        L_0x00ad:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.asisctf.Andex.ConfigurationActivity.writeToDisk(okhttp3.ResponseBody):boolean");
    }
}
