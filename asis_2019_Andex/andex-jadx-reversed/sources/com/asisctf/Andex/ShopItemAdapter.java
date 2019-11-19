package com.asisctf.Andex;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import com.asisctf.Andex.models.ShopItemsModel.Items;
import java.util.List;

public class ShopItemAdapter extends Adapter<ViewHolder> {
    /* access modifiers changed from: private */
    public Context context;
    List<Items> list;

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        public TextView lName;
        public LinearLayout linearLayout;
        public TextView link;
        public Button share;

        public ViewHolder(View view) {
            super(view);
            this.link = (TextView) view.findViewById(R.id.link_self);
            this.lName = (TextView) view.findViewById(R.id.linkName);
            this.linearLayout = (LinearLayout) view.findViewById(R.id.linear_number);
            this.share = (Button) view.findViewById(R.id.btn_share_user);
        }
    }

    public ShopItemAdapter(Context context2, List<Items> list2) {
        this.context = context2;
        this.list = list2;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shop_item, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final List<Items> list2 = this.list;
        TextView textView = viewHolder.link;
        StringBuilder sb = new StringBuilder();
        sb.append("price: ");
        sb.append(((Items) list2.get(i)).price);
        String str = "";
        sb.append(str);
        textView.setText(sb.toString());
        TextView textView2 = viewHolder.lName;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(((Items) list2.get(i)).title);
        sb2.append(str);
        textView2.setText(sb2.toString());
        viewHolder.share.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ShopItemAdapter.this.context.getApplicationContext(), ShopOrderActivity.class);
                intent.addFlags(268435456);
                intent.putExtra("id", ((Items) list2.get(i)).item_id);
                ShopItemAdapter.this.context.startActivity(intent);
            }
        });
    }

    public int getItemCount() {
        return this.list.size();
    }
}
