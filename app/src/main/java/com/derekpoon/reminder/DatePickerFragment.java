package com.derekpoon.reminder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.widget.DatePicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by derekpoon on 17/01/2018.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    /*
    Recommended method to pass a value from Fragment to Activity is use a callback to get a signal from
    the Fragment.
     */

    //Add an interface and register it
    public static interface OnCompleteListener {
        public abstract void onComplete(String selectedDate);
    }

    private OnCompleteListener mListener;

    // make sure the Activity implemented it
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle == null) {
            Calendar cal = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(getActivity(),
                    this, year, month, day);
            dp.getDatePicker().setMaxDate(today.getTimeInMillis());

            return dp;
        }

        String dobVal2 = bundle.getString("DOB","");
        System.out.println(dobVal2);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String dateInString = dobVal2;

        Date date = null;
        try {
            date = sdf.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        System.out.println(year + " " + month + " " + day);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dp = new DatePickerDialog(getActivity(),
                this, year, month, day);
//        dp.setTitle("Pick a date: ");
//        DatePickerDialog dp = new DatePickerDialog(getActivity(),
//                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,this,year,month,day);

        //set a max date to today so future dates cannot be selected
        dp.getDatePicker().setMaxDate(today.getTimeInMillis());

        // Create a new instance of DatePickerDialog and return it
//        return new DatePickerDialog(getActivity(), this, year, month, day);
        return dp;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = sdf.format(cal.getTime());

        //send that value back to the Activity via your callback.
        this.mListener.onComplete(formattedDate);

    }
}