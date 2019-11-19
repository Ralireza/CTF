package com.asisctf.Andex.models;

import java.util.List;

public class ShopItemsModel {
    public int code;
    public List<Items> data;

    public class Items {
        public String description;
        public String img_addr;
        public int item_id;
        public int price;
        public int stock;
        public String title;

        public Items() {
        }
    }
}
