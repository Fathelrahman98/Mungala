package com.example.mungala;

import android.view.View;

/**
 * a format is defined by two methods one to get an index from a hole view, the other to get a hole view from an index.
 */
public interface BoardFormat {

    View getView(int x);
    int getIndex(View v);

}
