package com.example.mungala;

import java.util.Calendar;
import java.util.Random;

public class SimpleBot implements Bot {
    @Override
    public int selectHole() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random random = new Random(Calendar.getInstance().getTimeInMillis());
            return random.nextInt(6);
        }

}
