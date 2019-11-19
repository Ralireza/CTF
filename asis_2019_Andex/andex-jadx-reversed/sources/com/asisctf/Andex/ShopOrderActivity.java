package com.asisctf.Andex;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.asisctf.Andex.models.ShopOrderModel;
import java.io.File;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopOrderActivity extends AppCompatActivity {
    TextView msg;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_shop_order);
        this.msg = (TextView) findViewById(R.id.msg);
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.shop_order_url);
        sb.append(getIntent().getIntExtra("id", -1));
        getShopOrder(sb.toString());
    }

    private void getShopOrder(String str) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).getShopOrder(getSharedPreferences("register", 0).getString("uuid", null), str).enqueue(new Callback<ShopOrderModel>() {
            public void onResponse(Call<ShopOrderModel> call, Response<ShopOrderModel> response) {
                ShopOrderModel shopOrderModel = (ShopOrderModel) response.body();
                try {
                    if (shopOrderModel.code == 200) {
                        ShopOrderActivity.this.msg.setText(shopOrderModel.data.result);
                        if (!shopOrderModel.data.result.equals("not enough balance") && !shopOrderModel.data.result.equals("we do not have this item right now") && !shopOrderModel.data.result.equals("purchased successfully, but you cannot get anything for free :)")) {
                            Toast.makeText(ShopOrderActivity.this.getApplicationContext(), "downloading dex . . .", 0).show();
                            ShopOrderActivity.this.getDex3(shopOrderModel.data.result);
                            return;
                        }
                        return;
                    }
                    Context applicationContext = ShopOrderActivity.this.getApplicationContext();
                    StringBuilder sb = new StringBuilder();
                    sb.append("code:  ");
                    sb.append(shopOrderModel.code);
                    Toast.makeText(applicationContext, sb.toString(), 0).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<ShopOrderModel> call, Throwable th) {
                Toast.makeText(ShopOrderActivity.this.getApplicationContext(), th.getMessage(), 0).show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void getDex3(String str) {
        String string = getSharedPreferences("register", 0).getString("uuid", null);
        APIInterFace aPIInterFace = (APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class);
        StringBuilder sb = new StringBuilder();
        sb.append("api/get_dex/");
        sb.append(str);
        aPIInterFace.getShopOrderD(string, sb.toString()).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ShopOrderActivity.this.writeToDisk((ResponseBody) response.body());
                    Toast.makeText(ShopOrderActivity.this.getApplicationContext(), "decrypting . . .", 0).show();
                    File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    StringBuilder sb = new StringBuilder();
                    sb.append(externalStoragePublicDirectory.getAbsolutePath());
                    sb.append("/conf/3plain.dex");
                    String sb2 = sb.toString();
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(externalStoragePublicDirectory.getAbsolutePath());
                    sb3.append("/conf/3.dex");
                    CryptoHandler.decrypt(sb2, sb3.toString(), Utils.enkey);
                    DexJob.printAllClassName(sb2, ShopOrderActivity.this.getApplicationContext());
                    Class classFromDex = DexJob.getClassFromDex(sb2, "com.asisctf.config.SayHelloToYourLittleFriend", ShopOrderActivity.this.getApplicationContext());
                    classFromDex.getMethods();
                    ShopOrderActivity.this.msg.setText((String) classFromDex.getMethod("flag_response", new Class[0]).invoke(classFromDex.newInstance(), new Object[0]));
                } catch (Exception e) {
                    Toast.makeText(ShopOrderActivity.this.getApplicationContext(), "Failed", 0).show();
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable th) {
                Toast.makeText(ShopOrderActivity.this.getApplicationContext(), th.getMessage(), 0).show();
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
            java.lang.String r2 = "3.dex"
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
        throw new UnsupportedOperationException("Method not decompiled: com.asisctf.Andex.ShopOrderActivity.writeToDisk(okhttp3.ResponseBody):boolean");
    }
}
