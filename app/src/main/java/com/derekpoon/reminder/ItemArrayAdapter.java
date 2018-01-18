package com.derekpoon.reminder;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by derekpoon on 08/12/2017.
 */

public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ViewHolder> {

    //All methods in this adapter are required for a bare minimum recyclerview adapter
    private int listItemLayout;
    private ArrayList<Item> itemList;

    private Listener listener;

    /*
    set up and interface for listener
     */

    public static interface Listener {
        public void onClick(int position);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    // Constructor of the class
    public ItemArrayAdapter(int layoutId, ArrayList<Item> itemList) {
        listItemLayout = layoutId;
        this.itemList = itemList;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    // specify the row layout file and click for each row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(listItemLayout, parent, false);
        ViewHolder myViewHolder = new ViewHolder(view);
        return myViewHolder;
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
        ImageView profile = holder.profile;
        ImageView party = holder.party;
        TextView item = holder.item;
        TextView dob = holder.dob;
        TextView daysLeft = holder.daysLeft;
        TextView daysLeftText = holder.daysLeftText;
        TextView age = holder.age;
//        profile.setImageResource(R.drawable.default_profile);
        profile.setImageResource(itemList.get(listPosition).getProfile());
        item.setText(itemList.get(listPosition).getName());
        dob.setText(itemList.get(listPosition).getDob().toString());

        //bind the click listener to the viewholder
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) { listener.onClick(listPosition);
                }
            }
        });

        if (itemList.get(listPosition).getDaysLeft() == 0) {
            daysLeft.setText("Today");
            daysLeft.setTypeface(null, Typeface.BOLD);
            daysLeft.setTextColor(Color.parseColor("#23DB23"));
            daysLeftText.setVisibility(View.INVISIBLE);
            party.setVisibility(View.VISIBLE);
        } else {
            daysLeft.setText(String.valueOf(itemList.get(listPosition).getDaysLeft()));
            party.setVisibility(View.INVISIBLE);
        }
        age.setText(String.valueOf(itemList.get(listPosition).getAge()));
    }

    // Static inner class to initialize the views of rows
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView profile, party;
        public TextView item, dob, daysLeft, age, daysLeftText;
        private CardView cardView;


        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            cardView = (CardView)itemView.findViewById(R.id.card_view);
            profile = (ImageView) itemView.findViewById(R.id.row_profile);
            item = (TextView) itemView.findViewById(R.id.row_item);
            item.setTypeface(null, Typeface.BOLD);
            item.setTextColor(Color.parseColor("#57A5F4"));
            dob = (TextView) itemView.findViewById(R.id.row_dob);
            daysLeft = (TextView) itemView.findViewById(R.id.row_daysLeft);
            daysLeftText = (TextView) itemView.findViewById(R.id.row_daysLeftTest);
            age = (TextView) itemView.findViewById(R.id.row_age);
            age.setTypeface(null, Typeface.BOLD);
            party = (ImageView)itemView.findViewById(R.id.row_party);
        }

        @Override
        public void onClick(View view) {
            Log.d("onclick", "onClick " + getLayoutPosition() + " " + item.getText());
        }
    }
}