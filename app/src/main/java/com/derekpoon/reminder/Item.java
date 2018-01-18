package com.derekpoon.reminder;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by derekpoon on 08/12/2017.
 */

public class Item implements Serializable, Comparable<Item> {

    private int profile;
    private String name;
    private String dob;
    private int daysLeft;
    private int age;
    private String daysLeftText;

//    public Item(String n) {
//        name = n;
//    }

    public Item(int profile, String name, String dob, int daysLeft, int age) {
        this.profile = profile;
        this.name = name;
        this.dob = dob;
        this.daysLeft = daysLeft;
        this.age = age;
    }

    public Item(String daysLeftText) {
        this.daysLeftText = daysLeftText;

    }

    public String getDaysLeftText() {
        return daysLeftText;
    }

    public void setDaysLeftText(String daysLeftText) {
        this.daysLeftText = daysLeftText;
    }

    @Override
    public int compareTo(Item that) {
        int daysLeft1 = this.getDaysLeft();
        int daysLeft2 = that.getDaysLeft();

        if (daysLeft1 > daysLeft2) {
            return 1;
        } else if (daysLeft1 < daysLeft2) {
            return -1;
        } else {
            return 0;
        }
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "remain: " + daysLeft;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

}
