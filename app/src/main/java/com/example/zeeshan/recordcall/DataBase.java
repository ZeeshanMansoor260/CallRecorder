package com.example.zeeshan.recordcall;

import io.realm.RealmObject;

/**
 * Created by Zeeshan on 7/5/2016.
 */
public class DataBase extends RealmObject {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
}
