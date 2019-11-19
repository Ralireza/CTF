package com.asisctf.Andex.models;

public class RegisterationModel {
    public int code;
    public mData data;

    public class mData {
        public String encryption_key;
        public String role_id;
        public String uuid;

        public mData() {
        }
    }
}
