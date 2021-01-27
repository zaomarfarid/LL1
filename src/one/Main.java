package one;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> gsArray = new ArrayList<String>();
        gsArray.add("S->MH|a");
        gsArray.add("H->LS0|^");
        gsArray.add("K->dML|^");
        gsArray.add("L->eHf");
        gsArray.add("M->K|bLM");
        Character start = 'S';

        GS test = new GS();
        test.setgsArray(gsArray);
        test.setStart(start);
        test.init();
    }
}
