package one;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> gsArray = new ArrayList<String>();

        gsArray.add("E->TӖ");
        gsArray.add("Ӗ->+TӖ|Ɛ");
        gsArray.add("T->FŤ");
        gsArray.add("Ť->*FŤ|Ɛ");
        gsArray.add("F->(E)|i");
        Character start = 'E';

        GS test = new GS();
        test.setgsArray(gsArray);
        test.setStart(start);
        test.init();
    }
}
