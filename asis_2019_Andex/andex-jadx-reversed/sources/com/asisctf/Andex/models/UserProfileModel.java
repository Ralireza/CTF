package com.asisctf.Andex.models;

public class UserProfileModel {
    public int code;
    public Data data;

    public class Data {
        public String blc_currency;
        public int id;
        public String role_id;
        public String username;
        public String uuid;

        public Data() {
        }
    }
}
