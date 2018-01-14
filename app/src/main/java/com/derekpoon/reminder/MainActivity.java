package com.derekpoon.reminder;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Button btnCreateNote;
    private Button btnDelete;
    private static final int PERMISSION_REQUEST_STORAGE = 0;
    private Context context = this;
    private EditText userInput;
    private RecyclerView rv;
    private ArrayList<Item> itemList;
    private ItemArrayAdapter itemArrayAdapter;
    private ArrayList<Item> itemArray2;
    private String infilename = "internalfile";
    private File myInternalFile;
    private String dobVal;
    private String dayRemain;
    private int dayRemainInt = 0;
    private int age;
    private TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkStoragePermission();

//        btnCreateNote = (Button)findViewById(R.id.create_note);
//        btnCreateNote.setOnClickListener(btnHandler);
//        btnDelete = (Button)findViewById(R.id.deleteData);
//        btnDelete.setOnClickListener(btnHandler);

        //initialize the arraylist of items
        itemList = new ArrayList<Item>();
        itemArray2 = new ArrayList<Item>();

        itemArrayAdapter = new ItemArrayAdapter(R.layout.card_item_temp, itemList);
        rv = (RecyclerView)findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(itemArrayAdapter);

        itemArrayAdapter.setListener(new ItemArrayAdapter.Listener() {
            public void onClick(int position) {
                Toast.makeText(MainActivity.this,"You clicked " + position, Toast.LENGTH_SHORT).show();
                editExistingEntry(position);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                if (direction == ItemTouchHelper.LEFT) {    //if swipe left

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); //alert for confirm to delete
                    builder.setMessage("Delete this birthday?");    //set message

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() { //when click on DELETE
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            itemArrayAdapter.notifyItemRemoved(position);    //item removed from recylcerview
//                            sqldatabase.execSQL("delete from " + TABLE_NAME + " where _id='" + (position + 1) + "'"); //query for delete
                            itemList.remove(position);  //then remove item
                            writeToFile();
                            loadFromFile();

                            return;
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            itemArrayAdapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                            itemArrayAdapter.notifyItemRangeChanged(position, itemArrayAdapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                            return;
                        }
                    }).show();  //show alert dialog
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rv);

        myInternalFile = new File(context.getFilesDir(), infilename);
        if (!myInternalFile.exists()) {
            try {
                myInternalFile.createNewFile();
                Toast.makeText(MainActivity.this,"File created", Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            loadFromFile();
            updateArray();
            Log.d("LIST BEFORE", itemList.toString());
            updateDaysLeft();
            Log.d("LIST AFTER", itemList.toString());
//            System.out.println("Before sort: " + itemList);
            Collections.sort(itemList);
//            System.out.println("After sort: " + itemList);
            CompareDaysLeft compdaysremain = new CompareDaysLeft();
            Collections.sort(itemList, compdaysremain);
            itemArrayAdapter.notifyDataSetChanged();
        }
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                //Code to run when the Add button is clicked
                addName();
                return true;
            case R.id.action_settings:
                displayDeleteDataAlert();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View view, int position) {
        Toast.makeText(MainActivity.this,"Load successful", Toast.LENGTH_SHORT).show();
    }

    class CompareDaysLeft implements Comparator<Item> {
        public int compare(Item day1, Item day2) {

            int d1 = day1.getDaysLeft();
            int d2 = day2.getDaysLeft();

            return d1 - d2;
        }
    }

    public void updateDaysLeft() {

        for (int i = 0; i < itemList.size(); i++) {
            Calendar today = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            Date date = null;
            Calendar birthday = Calendar.getInstance();

            try {
                date = sdf.parse(itemList.get(i).getDob());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            birthday.setTime(date);

            int bdayMonth = birthday.get(Calendar.MONTH);
            int currMonth = today.get(Calendar.MONTH);
            int bdayDay = birthday.get(Calendar.DAY_OF_MONTH);
            int currDay = today.get(Calendar.DAY_OF_MONTH);
            int bdayYear = birthday.get(Calendar.YEAR);
            int currYear = today.get(Calendar.YEAR);

            System.out.println(today.getTime());
            System.out.println("");

            //if birth is in the same year as query
            if (bdayMonth == currMonth) {
                if (bdayDay == currDay) {
                    dayRemain = "0";
                    dayRemainInt = 0;
                    age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
                }
                if (bdayDay < currDay) {
                    age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR) + 1;

                    birthday.get(Calendar.DAY_OF_MONTH);
                    birthday.get(Calendar.MONTH);
                    birthday.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1);

                    Date d1 = birthday.getTime();
                    Date d2 = today.getTime();

                    long diff = d1.getTime() - d2.getTime();

                    long diffDays = (diff / (24 * 60 * 60 * 1000));

                    dayRemain = String.valueOf(diffDays);
                    dayRemainInt = Integer.parseInt(dayRemain) + 1;

                    System.out.print("Next birthday is in " + diffDays + " days");
                }
                if (bdayDay > currDay) {
                    age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

                    birthday.get(Calendar.DAY_OF_MONTH);
                    birthday.get(Calendar.MONTH);
                    birthday.set(Calendar.YEAR, today.get(Calendar.YEAR));

                    Date d1 = birthday.getTime();
                    Date d2 = today.getTime();

                    long diff = d1.getTime() - d2.getTime();

                    long diffDays = (diff / (24 * 60 * 60 * 1000));

                    dayRemain = String.valueOf(diffDays);
                    dayRemainInt = Integer.parseInt(dayRemain) + 1;

                    System.out.print("Next birthday is in " + diffDays + " days");
                }
            }
            if (bdayMonth < currMonth) {
                age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
                age = age + 1;
                birthday.get(Calendar.DAY_OF_MONTH);
                birthday.get(Calendar.MONTH);
                birthday.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1);

                Date d11 = birthday.getTime();
                Date d22 = today.getTime();

                long diff = d11.getTime() - d22.getTime();

                long diffDays = diff / (24 * 60 * 60 * 1000);

                dayRemain = String.valueOf(diffDays);
                dayRemainInt = Integer.parseInt(dayRemain);

                System.out.print("Next birthday is in " + diffDays + " days");

            }
            if (bdayMonth > currMonth) {
                age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
                birthday.get(Calendar.DAY_OF_MONTH);
                birthday.get(Calendar.MONTH);
                birthday.set(Calendar.YEAR, today.get(Calendar.YEAR));

                Date d11 = birthday.getTime();
                Date d22 = today.getTime();

                long diff = d11.getTime() - d22.getTime();

                long diffDays = diff / (24 * 60 * 60 * 1000);

                dayRemain = String.valueOf(diffDays);
                dayRemainInt = Integer.parseInt(dayRemain) + 1;

                System.out.print("Next birthday is in " + diffDays + " days");
            }
            itemList.get(i).setDaysLeft(dayRemainInt);
            itemList.get(i).setAge(age);
        }
    }

//    @Override
//    public void onResume() {  // After a pause OR at startup
//        loadData();
//        super.onResume();
//        //Refresh your stuff here
//    }

    /*
    check for write external permissions
     */

    public void checkStoragePermission() {
        //checks if the app has the permission to write to storage
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void deleteData() {
        itemList.clear();
        itemArrayAdapter.notifyDataSetChanged();
        writeToFile();
    }

//    View.OnClickListener btnHandler = new View.OnClickListener() {
//        public void onClick(View v) {
//
//            switch (v.getId()) {
//                case R.id.create_note:
//                    addName();
//                    break;
//                case R.id.deleteData:
//                    displayDeleteDataAlert();
//                    break;
//            }
//        }
//    };

    public void editExistingEntry(int position) {
        LayoutInflater li = LayoutInflater.from(context);
        final View promptsView = li.inflate(R.layout.prompts_display_birthday, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        mName = (TextView) promptsView.findViewById(R.id.user_name);
        mName.setText("Name: " + itemList.get(position).getName());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
//                                    result.setText(userInput.getText());
                                showDatePickerDialog(promptsView);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        //show keyboard
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // show it
        alertDialog.show();

    }

    public void addName() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        final View promptsView = li.inflate(R.layout.prompts_name, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        userInput = (EditText) promptsView
                .findViewById(R.id.editTextName);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
//                                    result.setText(userInput.getText());
                                showDatePickerDialog(promptsView);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        //show keyboard
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // show it
        alertDialog.show();
    }


    public void addtoArray() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(("dd MMM yyyy"));
        Date date = null;
        Calendar birthday = Calendar.getInstance();

        try {
            date = sdf.parse(dobVal);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        birthday.setTime(date);

        int bdayMonth = birthday.get(Calendar.MONTH);
        int currMonth = today.get(Calendar.MONTH);
        int bdayDay = birthday.get(Calendar.DAY_OF_MONTH);
        int currDay = today.get(Calendar.DAY_OF_MONTH);

        System.out.println(today.getTime());
        System.out.println("");

        //if birth is in the same month as query
        if (bdayMonth == currMonth) {
            if (bdayDay == currDay) {
                dayRemain = "0";
                age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
            }
            if (bdayDay < currDay) {
                age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR) + 1;

                birthday.get(Calendar.DAY_OF_MONTH);
                birthday.get(Calendar.MONTH);
                birthday.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1);

                Date d1 = birthday.getTime();
                Date d2 = today.getTime();

                long diff = d1.getTime() - d2.getTime();

                long diffDays = (diff / (24 * 60 * 60 * 1000));

                dayRemain = String.valueOf(diffDays);
                dayRemainInt = Integer.parseInt(dayRemain) + 1;

                System.out.print("Next birthday is in " + diffDays + " days");
            }
            if (bdayDay > currDay) {
                age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

                birthday.get(Calendar.DAY_OF_MONTH);
                birthday.get(Calendar.MONTH);
                birthday.set(Calendar.YEAR, today.get(Calendar.YEAR));

                Date d1 = birthday.getTime();
                Date d2 = today.getTime();

                long diff = d1.getTime() - d2.getTime();

                long diffDays = (diff / (24 * 60 * 60 * 1000));

                dayRemain = String.valueOf(diffDays);
                dayRemainInt = Integer.parseInt(dayRemain) + 1;

                System.out.print("Next birthday is in " + diffDays + " days");
            }
        }
        if (bdayMonth < currMonth) {
            age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
            age = age + 1;
            birthday.get(Calendar.DAY_OF_MONTH);
            birthday.get(Calendar.MONTH);
            birthday.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1);

            Date d11 = birthday.getTime();
            Date d22 = today.getTime();

            long diff = d11.getTime() - d22.getTime();

            long diffDays = diff / (24 * 60 * 60 * 1000);

            dayRemain = String.valueOf(diffDays);
            dayRemainInt = Integer.parseInt(dayRemain) + 1;

            System.out.print("Next birthday is in " + diffDays + " days");
        }
        if (bdayMonth > currMonth) {
            age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
            birthday.get(Calendar.DAY_OF_MONTH);
            birthday.get(Calendar.MONTH);
            birthday.set(Calendar.YEAR, today.get(Calendar.YEAR));

            Date d11 = birthday.getTime();
            Date d22 = today.getTime();

            long diff = d11.getTime() - d22.getTime();

            long diffDays = diff / (24 * 60 * 60 * 1000);

            dayRemain = String.valueOf(diffDays);
            dayRemainInt = Integer.parseInt(dayRemain) + 1;

            System.out.print("Next birthday is in " + diffDays + " days");
        }
        itemList.add(new Item(R.drawable.default_profile, userInput.getText().toString(), dobVal, dayRemainInt, age));
        System.out.println("Before sort: " + itemList);
        Collections.sort(itemList);
        System.out.println("After sort: " + itemList);
        CompareDaysLeft compdaysremain = new CompareDaysLeft();
        Collections.sort(itemList, compdaysremain);
        itemArrayAdapter.notifyDataSetChanged();
    }

    public void writeToFile() {

        try {
            FileOutputStream fos = context.openFileOutput(infilename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(itemList);
            os.close();
            fos.close();
            Toast.makeText(MainActivity.this,"Save successful", Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadFromFile() {

        if (!myInternalFile.exists()) {
            Toast.makeText(MainActivity.this, "Load failed. File does not exist", Toast.LENGTH_SHORT).show();
        } else {
            try {
                FileInputStream fis = context.openFileInput(infilename);
                ObjectInputStream is = new ObjectInputStream(fis);
                itemArray2 = (ArrayList<Item>) is.readObject();
                is.close();
                fis.close();
                Toast.makeText(MainActivity.this,"Load successful", Toast.LENGTH_SHORT).show();
                itemArrayAdapter.notifyDataSetChanged();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void updateArray() {
        for (int i = 0; i < itemArray2.size(); i++) {
            itemList.add(itemArray2.get(i));
        }
    }

    public void displayDeleteDataAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set title
        alertDialogBuilder.setTitle("Erase data?");

        // set dialog message
        alertDialogBuilder
                .setMessage("Selecting yes, will erase data stored!")
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
//                            MainActivity.this.finish();
                        deleteData();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * This callback method, call DatePickerFragment class,
     * DatePickerFragment class returns calendar view.
     * @param view
     */
    public void datePicker(View view){

        DatePickerFragment fragment = new DatePickerFragment();
        //fragment.show(getSupportFragmentManager());
    }

    /**
     * To set date on TextView
     * @param calendar
     */
    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        dobVal = String.valueOf(dateFormat.format(calendar.getTime()));

        addtoArray();
        writeToFile();
        loadFromFile();
    }

    /**
     * To receive a callback when the user sets the date.
     * @param view
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        setDate(cal);
    }

    /**
     * Create a DatePickerFragment class that extends DialogFragment.
     * Define the onCreateDialog() method to return an instance of DatePickerDialog
     */
    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);


            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener)
                            getActivity(), year, month, day);
        }
    }
}
