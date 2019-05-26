package com.example.mungala;

import android.widget.Button;
import android.widget.ImageButton;

public class Hole {

    private ImageButton button;
    private int numberOfMarbleBalls;

    Hole(ImageButton button) {
        this.button = button;
        numberOfMarbleBalls = 5;
        setImage();
        //button.setText(""+numberOfMarbleBalls);
    }

    public int playHole() throws HoleNotAvailableException{
        int x = numberOfMarbleBalls;
        if (!isHoleAvailabe()) {
            throw new HoleNotAvailableException();
        }
        numberOfMarbleBalls = 0;
        setImage();
        //button.setText(""+numberOfMarbleBalls);
        return x;
    }

    public boolean isHoleTaken() {
        if (numberOfMarbleBalls == 2 || numberOfMarbleBalls == 4) {
            return true;
        }
        return false;
    }

    public boolean isHoleAvailabe() {
        return numberOfMarbleBalls > 1;
    }

    public int clearHole() throws HoleNotClearableException{
        int x = numberOfMarbleBalls;
        if (x > 1) {
            throw new HoleNotClearableException();
        }
        numberOfMarbleBalls = 0;
        setImage();
        //button.setText(""+numberOfMarbleBalls);
        return x;
    }

    public void addMarbleBall() {
        numberOfMarbleBalls++;
        setImage();
        //button.setText(""+numberOfMarbleBalls);
    }

    class HoleNotAvailableException extends Exception {
        public HoleNotAvailableException() {
            super("hole can't be played! \n few number of marble balls");
        }
    }

    class HoleNotClearableException extends Exception {
        public HoleNotClearableException() {
            super("hole shouldn't be cleared! \n large number of marble balls");
        }
    }

    void setImage() {
        switch (numberOfMarbleBalls) {
            case 0:
                button.setImageResource(R.drawable.hole);
                break;
            case 1:
                button.setImageResource(R.drawable.hole1);
                break;
            case 2:
                button.setImageResource(R.drawable.hole2);
                break;
            case 3:
                button.setImageResource(R.drawable.hole3);
                break;
            case 4:
                button.setImageResource(R.drawable.hole4);
                break;
            case 5:
                button.setImageResource(R.drawable.hole5);
                break;
            case 6:
                button.setImageResource(R.drawable.hole6);
                break;
            case 7:
                button.setImageResource(R.drawable.hole7);
                break;
            case 8:
                button.setImageResource(R.drawable.hole8);
                break;
            case 9:
                button.setImageResource(R.drawable.hole9);
                break;
            case 10:
                button.setImageResource(R.drawable.hole10);
                break;
            case 11:
                button.setImageResource(R.drawable.hole11);
                break;
            case 12:
                button.setImageResource(R.drawable.hole12);
                break;
            case 13:
                button.setImageResource(R.drawable.hole13);
                break;
            case 14:
                button.setImageResource(R.drawable.hole14);
                break;
            case 15:
                button.setImageResource(R.drawable.hole15);
                break;
            case 16:
                button.setImageResource(R.drawable.hole16);
                break;
            case 17:
                button.setImageResource(R.drawable.hole17);
                break;
            case 18:
                button.setImageResource(R.drawable.hole18);
                break;
            case 19:
                button.setImageResource(R.drawable.hole19);
                break;
            case 20:
                button.setImageResource(R.drawable.hole20);
                break;
            case 21:
                button.setImageResource(R.drawable.hole21);
                break;
            case 22:
                button.setImageResource(R.drawable.hole22);
                break;
            case 23:
                button.setImageResource(R.drawable.hole23);
                break;
            case 24:
                button.setImageResource(R.drawable.hole24);
                break;
            case 25:
                button.setImageResource(R.drawable.hole25);
                break;
            case 26:
                button.setImageResource(R.drawable.hole26);
                break;
            case 27:
                button.setImageResource(R.drawable.hole27);
                break;
            case 28:
                button.setImageResource(R.drawable.hole28);
                break;
            case 29:
                button.setImageResource(R.drawable.hole29);
                break;
            case 30:
                button.setImageResource(R.drawable.hole30);
                break;
            case 31:
                button.setImageResource(R.drawable.hole31);
                break;
            case 32:
                button.setImageResource(R.drawable.hole32);
                break;
            case 33:
                button.setImageResource(R.drawable.hole33);
                break;
            case 34:
                button.setImageResource(R.drawable.hole34);
                break;
            case 35:
                button.setImageResource(R.drawable.hole35);
                break;
            case 36:
                button.setImageResource(R.drawable.hole36);
                break;
            case 37:
                button.setImageResource(R.drawable.hole37);
                break;
            case 38:
                button.setImageResource(R.drawable.hole38);
                break;
            case 39:
                button.setImageResource(R.drawable.hole39);
                break;
            case 40:
                button.setImageResource(R.drawable.hole40);
                break;
            case 41:
                button.setImageResource(R.drawable.hole41);
                break;
            case 42:
                button.setImageResource(R.drawable.hole42);
                break;
            case 43:
                button.setImageResource(R.drawable.hole43);
                break;
            case 44:
                button.setImageResource(R.drawable.hole44);
                break;
            case 45:
                button.setImageResource(R.drawable.hole45);
                break;
            case 46:
                button.setImageResource(R.drawable.hole46);
                break;
            case 47:
                button.setImageResource(R.drawable.hole47);
                break;
            case 48:
                button.setImageResource(R.drawable.hole48);
                break;
            case 49:
                button.setImageResource(R.drawable.hole49);
                break;
            case 50:
                button.setImageResource(R.drawable.hole50);
                break;
            default:
                button.setImageResource(R.drawable.hole);
        }
    }
}
