package ll1;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> parserArray = new ArrayList<String>();

        parserArray.add("E->TӖ");
        parserArray.add("Ӗ->+TӖ|Ɛ");
        parserArray.add("T->FŤ");
        parserArray.add("Ť->*FŤ|Ɛ");
        parserArray.add("F->(E)|i");
        Character start = 'E';

        Parser cfg = new Parser();
        cfg.setParserArray(parserArray);
        cfg.setStart(start);
        cfg.init();
    }
}
