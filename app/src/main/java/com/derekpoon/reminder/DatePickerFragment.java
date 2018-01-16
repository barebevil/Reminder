package com.derekpoon.reminder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/*
When using Alert dialogs, good idea to wrap it in a instance of DialogFragment
a subclass of Fragment.

Having the dialog managed by the FragmentManager gives you more options for presenting the dialog.

In addition, a bare AlertDialog will vanish if the device is rotated.
If the AlertDialog is wrapped in a fragment, then the dialog will be
re-created after rotation.
 */

public class DatePickerFragment extends DialogFragment {

    public static final String EXTRA_DATE =
            "com.bignerdranch.android.criminalintent.date";

    private static final String ARG_DATE = "date";

    private DatePicker mDatePicker;

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_date, null);

        mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
        mDatePicker.init(year, month, day, null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int year = mDatePicker.getYear();
                                int month = mDatePicker.getMonth();
                                int day = mDatePicker.getDayOfMonth();
                                Date date = new GregorianCalendar(year, month, day).getTime();
                                sendResult(Activity.RESULT_OK, date);
                            }
                        })
                .create();
    }

    private void sendResult(int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return; }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}

//public class DatePickerFragment extends DialogFragment
//        implements DatePickerDialog.OnDateSetListener {
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        View view = getView();
//
//        DatePicker datePicker = (DatePicker) view.findViewById(R.id.datepicker);
//        datePicker.updateDate(2000, 5, 22);
//
//        // Use the current date as the default date in the picker
////        final Calendar c = Calendar.getInstance();
////        int year = c.get(Calendar.YEAR);
////        int month = c.get(Calendar.MONTH);
////        int day = c.get(Calendar.DAY_OF_MONTH);
//
//        int year = datePicker.getYear();
//        int month = datePicker.getMonth();
//        int day = datePicker.getDayOfMonth();
//
//        // Create a new instance of DatePickerDialog and return it
//        return new DatePickerDialog(getActivity(), this, year, month, day);
//    }
//
//    public void onDateSet(DatePicker view, int year, int month, int day) {
//        // Do something with the date chosen by the user
//    }
//}