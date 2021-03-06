package com.derekpoon.reminder;

import android.content.DialogInterface;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DatePickerFragment.OnCompleteListener {

    private TextView introText;
    private static final int PERMISSION_REQUEST_STORAGE = 0;
    private Context context = this;
    private EditText userInput;
    private RecyclerView rv;
    private ArrayList<Item> itemList;
    private ItemArrayAdapter itemArrayAdapter;
    private ArrayList<Item> itemArray2;
    private String infilename = "internalfile";
    private File myInternalFile;
    private String dobVal, dayRemain, mZodiacSign;
    private int dayRemainInt = 0, age, listPos = 0, default_profile_pic;
    private TextView mName, mDob, mAge, mZodiac;
    private boolean editExisting = false;
    private int sortedBy = 0;
    private Button mButtonGreeting;
    ImageView imageView;

    final CharSequence[] greetings = { "Happy birthday! May all your dreams come true.", "Best wishes on your birthday!", "Hope you have a fantastic birthday!", "Happy birthday! Enjoy the cake!" };
    int pickedGreeting = 0;

    public void onComplete(String selectedDate) {
        // After the dialog fragment completes, it calls this callback.
        // use the string here

        //update original DOB value
        dobVal = selectedDate;

        if (editExisting) {
            updateExisting();
            writeToFile();
            loadFromFile();
        } else {
            addtoArray();
            writeToFile();
            loadFromFile();
        }
    }

    public void updateExisting() {
        itemList.get(listPos).setDob(dobVal);
    }

    public void sortDaysLeft() {
        sortedBy = 0;
        CompareDaysLeft compdaysremain = new CompareDaysLeft();
        Collections.sort(itemList, compdaysremain);
        itemArrayAdapter.notifyDataSetChanged();
        savePref();
    }

    public void sortName() {
        sortedBy = 1;
        Collections.sort(itemList, new Comparator<Item>(){
            public int compare(Item i1, Item i2) {
                // ## Ascending order
                return i1.getName().compareToIgnoreCase(i2.getName()); // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(obj2.empId); // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
            }
        });
        itemArrayAdapter.notifyDataSetChanged();
        savePref();
    }

    public void sortAge() {
        sortedBy = 2;
        Collections.sort(itemList, new Comparator<Item>(){
            public int compare(Item i1, Item i2) {
                // ## Ascending order
//                return i1.getName().compareToIgnoreCase(i2.getName()); // To compare string values
                return Integer.valueOf(i1.getAge()).compareTo(i2.getAge()); // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
            }
        });
        itemArrayAdapter.notifyDataSetChanged();
        savePref();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkStoragePermission();

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
                displayEntry(position);
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
            loadPref();

            if (sortedBy == 0) {
                sortDaysLeft();
            }
            if (sortedBy == 1) {
                sortName();
            }
            if (sortedBy == 2) {
                sortAge();
            }
//            Log.d("LIST BEFORE", itemList.toString());
            updateDaysLeft();
//            Log.d("LIST AFTER", itemList.toString());
////            System.out.println("Before sort: " + itemList);
//            Collections.sort(itemList);
////            System.out.println("After sort: " + itemList);
//            CompareDaysLeft compdaysremain = new CompareDaysLeft();
//            Collections.sort(itemList, compdaysremain);
            itemArrayAdapter.notifyDataSetChanged();
        }

        introText = (TextView)findViewById(R.id.intro_text);
        introText.setTextColor(Color.parseColor("#FFFFFF"));
        if (itemList.size() == 0) {
            introText.setVisibility(View.VISIBLE);
        } else {
            introText.setVisibility(View.INVISIBLE);
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
            case R.id.action_order:
                //re order entries dialog
                displayOrderDialog();
                return true;
            case R.id.action_settings:
                displayDeleteDataAlert();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkStarSign() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        Date date = null;
        Calendar birthday = Calendar.getInstance();

        try {
            date = sdf.parse(itemList.get(listPos).getDob());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        birthday.setTime(date);

        int month = birthday.get(Calendar.MONTH);
        int day = birthday.get(Calendar.DAY_OF_MONTH);

        switch (month) {
            case 0:
                if (day < 20) {
                    mZodiacSign = "Capricorn";
                }
                if (day > 19) {
                    mZodiacSign = "Aquarius";
                }
                break;
            case 1:
                if (day < 19) {
                    mZodiacSign = "Aquarius";
                }
                if (day > 18) {
                    mZodiacSign = "Pisces";
                }
                break;
            case 2:
                if (day < 21) {
                    mZodiacSign = "Pisces";
                }
                if (day > 20) {
                    mZodiacSign = "Aries";
                }
                break;
            case 3:
                if (day < 20) {
                    mZodiacSign = "Aries";
                }
                if (day > 19) {
                    mZodiacSign = "Taurus";
                }
                break;
            case 4:
                if (day < 21) {
                    mZodiacSign = "Taurus";
                }
                if (day > 20) {
                    mZodiacSign = "Gemini";
                }
                break;
            case 5:
                if (day < 21) {
                    mZodiacSign = "Gemini";
                }
                if (day > 20) {
                    mZodiacSign = "Cancer";
                }
                break;
            case 6:
                if (day < 23) {
                    mZodiacSign = "Cancer";
                }
                if (day > 22) {
                    mZodiacSign = "Leo";
                }
                break;
            case 7:
                if (day < 23) {
                    mZodiacSign = "Leo";
                }
                if (day > 22) {
                    mZodiacSign = "Virgo";
                }
                break;
            case 8:
                if (day < 23) {
                    mZodiacSign = "Virgo";
                }
                if (day > 22) {
                    mZodiacSign = "Libra";
                }
                break;
            case 9:
                if (day < 23) {
                    mZodiacSign = "Libra";
                }
                if (day > 22) {
                    mZodiacSign = "Scorpio";
                }
                break;
            case 10:
                if (day < 22) {
                    mZodiacSign = "Scorpio";
                }
                if (day > 21) {
                    mZodiacSign = "Sagittarius";
                }
                break;
            case 11:
                if (day < 22) {
                    mZodiacSign = "Sagittarius";
                }
                if (day > 21) {
                    mZodiacSign = "Capricorn";
                }
                break;
        }
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
        introText.setVisibility(View.VISIBLE);
        writeToFile();
    }

    public void displayGreetingDialog() {

        //        LayoutInflater li = LayoutInflater.from(context);
//        final View promptsView = li.inflate(R.layout.prompts_sort_list, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
//        alertDialogBuilder.setView(promptsView);

//        final CharSequence[] greetings = { "Happy birthday! May all your dreams come true.", "Best wishes on your birthday!", "Hope you have a fantastic birthday!", "Happy birthday! Enjoy the cake!" };

        alertDialogBuilder.setTitle("Choose a greeting: ");
        alertDialogBuilder.setSingleChoiceItems(greetings, pickedGreeting,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:
                                pickedGreeting = 0;
                                break;
                            case 1:
                                pickedGreeting = 1;
                                break;
                            case 2:
                                pickedGreeting = 2;
                                break;
                            case 3:
                                pickedGreeting = 3;
                                break;
                        }
                        Toast.makeText(getApplicationContext(), greetings[item],
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Share",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                //share via explicit intent
                                dialog.dismiss();
                                shareGreeting(pickedGreeting);
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public void shareGreeting(int greetingNum) {
        //creates an intent that uses a SEND ACTION
        //this is an IMPLICIT intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        //add messageText to the intent
        intent.putExtra(Intent.EXTRA_TEXT, greetings[pickedGreeting]);

        //this starts the activity specified in the intent
//        startActivity(intent);

        Intent chosenIntent = Intent.createChooser(intent, "Send message...");
        startActivity(chosenIntent);

    }

    public void displayOrderDialog() {

//        LayoutInflater li = LayoutInflater.from(context);
//        final View promptsView = li.inflate(R.layout.prompts_sort_list, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
//        alertDialogBuilder.setView(promptsView);

        final CharSequence[] items = { "Days left", "Name", "Age" };

        alertDialogBuilder.setTitle("Sort list by: ");
        alertDialogBuilder.setSingleChoiceItems(items, sortedBy,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:
                                sortDaysLeft();
                                break;
                            case 1:
                                sortName();
                                break;
                            case 2:
                                sortAge();
                                break;
                        }
                        Toast.makeText(getApplicationContext(), items[item],
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
//                .setNegativeButton("Edit entry",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,int id) {
//
//                            }
//                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void displayEntry(int position) {

        LayoutInflater li = LayoutInflater.from(context);
        final View promptsView = li.inflate(R.layout.prompts_display_birthday, null);

        listPos = position;

        checkStarSign();

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        mName = (TextView) promptsView.findViewById(R.id.user_name);
        mName.setText(itemList.get(position).getName());
        mName.setTextColor(Color.parseColor("#57A5F4"));
        mDob = (TextView) promptsView.findViewById(R.id.date_of_birth);
        mDob.setText(itemList.get(position).getDob());
        mAge = (TextView) promptsView.findViewById(R.id.age);
        mAge.setText(String.valueOf(itemList.get(position).getAge() - 1));
        mZodiac = (TextView) promptsView.findViewById(R.id.zodiac_sign);
        mZodiac.setText(mZodiacSign);

        mButtonGreeting = (Button)promptsView.findViewById(R.id.send_greeting);
        mButtonGreeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayGreetingDialog();
            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Edit entry",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                editChangeName();
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

    public void editChangeName() {
        editExisting = true;
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        final View promptsView = li.inflate(R.layout.prompts_edit_name, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        userInput = (EditText) promptsView
                .findViewById(R.id.editTextName);
        userInput.setText(itemList.get(listPos).getName());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                itemList.get(listPos).setName(userInput.getText().toString());
                                android.app.DialogFragment picker = new DatePickerFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("DOB", itemList.get(listPos).getDob());
                                picker.setArguments(bundle);
                                picker.show((MainActivity.this).getFragmentManager(),"datePicker");
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
        editExisting = false;
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        final View promptsView = li.inflate(R.layout.prompts_name, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        default_profile_pic = R.drawable.default_profile;

        imageView = (ImageView)promptsView.findViewById(R.id.id_profile);
        imageView.setImageResource(default_profile_pic);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayProfileSelect();
            }
        });

        userInput = (EditText) promptsView
                .findViewById(R.id.editTextName);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                android.app.DialogFragment picker = new DatePickerFragment();
                                picker.show((MainActivity.this).getFragmentManager(),"datePicker");
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

    public void displayProfileSelect() {
        LayoutInflater li = LayoutInflater.from(context);
        final View promptsView = li.inflate(R.layout.prompts_profile_image, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Pick a profile image");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
//                .setPositiveButton("Save",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,int id) {
//
//                            }
//                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        ImageView pic1 = (ImageView)promptsView.findViewById(R.id.id_image_1);
        pic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                default_profile_pic = R.drawable.default_profile;
                imageView.setImageResource(default_profile_pic);
                alertDialog.dismiss();
            }
        });

        ImageView pic2 = (ImageView)promptsView.findViewById(R.id.id_image_2);
        pic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                default_profile_pic = R.drawable.profile_female;
                imageView.setImageResource(default_profile_pic);
                alertDialog.dismiss();
            }
        });


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
        itemList.add(new Item(default_profile_pic, userInput.getText().toString(), dobVal, dayRemainInt, age));
        System.out.println("Before sort: " + itemList);
        Collections.sort(itemList);
        System.out.println("After sort: " + itemList);
        CompareDaysLeft compdaysremain = new CompareDaysLeft();
        Collections.sort(itemList, compdaysremain);
        itemArrayAdapter.notifyDataSetChanged();
        introText.setVisibility(View.INVISIBLE);
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

    public void savePref() {
        try {
            // Creates a file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
            File testFile = new File(this.getExternalFilesDir(null), "prefs.txt");
            testFile.delete();
            if (!testFile.exists())
                testFile.createNewFile();

            // Adds a line to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, true /*append*/));

            String temp = String.valueOf(sortedBy);
            writer.write(temp);

            //            writer.write(userInput.getText().toString() + "\n");
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
            MediaScannerConnection.scanFile(this,
                    new String[]{testFile.toString()},
                    null,
                    null);
            Toast.makeText(MainActivity.this,"Save successful", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the prefs.txt file.");
            Toast.makeText(MainActivity.this,"Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadPref() {
        String textFromFile = "";

        // Gets the file from the primary external storage space of the
        // current application.
        File testFile = new File(this.getExternalFilesDir(null), "prefs.txt");

        if (testFile != null) {
            StringBuilder stringBuilder = new StringBuilder();
            // Reads the data from the file
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(testFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    textFromFile += line.toString();
//                    textFromFile += "\n";
                }
                reader.close();
                Toast.makeText(MainActivity.this,"Load successful", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("ReadWriteFile", "Unable to read the TestFile.txt file.");
                Toast.makeText(MainActivity.this,"Load failed. File does not exist.", Toast.LENGTH_SHORT).show();
            }
        }

        System.out.println("");
        System.out.println("SORT PREFERENCE VALUE: " + textFromFile);
        System.out.println("");

        sortedBy = Integer.parseInt(textFromFile);
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
        alertDialogBuilder.setTitle("Delete all birthdays?");

        // set dialog message
        alertDialogBuilder
                .setMessage("Selecting yes, will delete all birthdays!")
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

}
