package com.derekpoon.reminder;

import android.view.View;

/**
 * Created by derekpoon on 25/12/2017.
 */

public interface ItemClickListener {
    void onClick(View view, int position, boolean isLongClick);
}
