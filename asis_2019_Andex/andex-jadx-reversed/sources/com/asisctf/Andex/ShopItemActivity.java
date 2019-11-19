package com.asisctf.Andex;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.asisctf.Andex.models.ShopItemsModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopItemActivity extends AppCompatActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_shop_item);
        getShopItem(Utils.shop_item_url);
    }

    private void getShopItem(String str) {
        ((APIInterFace) new APIClient(getApplicationContext()).getClient().create(APIInterFace.class)).getShopItem(getSharedPreferences("register", 0).getString("uuid", null), str).enqueue(new Callback<ShopItemsModel>() {
            public void onResponse(Call<ShopItemsModel> call, Response<ShopItemsModel> response) {
                ShopItemsModel shopItemsModel = (ShopItemsModel) response.body();
                try {
                    if (shopItemsModel.code == 200) {
                        if (shopItemsModel.data.size() == 0) {
                            Toast.makeText(ShopItemActivity.this.getApplicationContext(), "EMPTY LIST !!", 1).show();
                        }
                        RecyclerView recyclerView = (RecyclerView) ShopItemActivity.this.findViewById(R.id.mylink_recycler);
                        ShopItemAdapter shopItemAdapter = new ShopItemAdapter(ShopItemActivity.this.getApplicationContext(), shopItemsModel.data);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ShopItemActivity.this.getApplicationContext()));
                        recyclerView.setAdapter(shopItemAdapter);
                        return;
                    }
                    Context applicationContext = ShopItemActivity.this.getApplicationContext();
                    StringBuilder sb = new StringBuilder();
                    sb.append("code:  ");
                    sb.append(shopItemsModel.code);
                    Toast.makeText(applicationContext, sb.toString(), 0).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(Call<ShopItemsModel> call, Throwable th) {
                Toast.makeText(ShopItemActivity.this.getApplicationContext(), th.getMessage(), 0).show();
            }
        });
    }
}
