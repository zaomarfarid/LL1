package one;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GS {
    // Production of string input

    private ArrayList<String> gsArray;

    public HashMap<Character, ArrayList<String>> expressionSet;
    // Nonterminal
    public HashSet<Character> VnSet;
    // Terminal
    public HashSet<Character> VtSet;
    // Start character
    public Character S;
    // Total number of production
    private HashMap<Character, Integer> numSet;
    // first set
    public HashMap<Character, HashSet<Character>> firstSet;
    // The first set of symbol strings
    public HashMap<String, HashSet<Character>> firstSetX;
    // follow set
    public HashMap<Character, HashSet<Character>> followSet;
    // select set
    private HashMap<Character, HashMap<String, HashSet<Character>>> selectMap;
    // Predictive analysis table
    public String[][] table;

    public GS() {
        gsArray = new ArrayList<String>();
        VnSet = new HashSet<Character>();
        VtSet = new HashSet<Character>();
        firstSet = new HashMap<Character, HashSet<Character>>();
        firstSetX = new HashMap<String, HashSet<Character>>();
        followSet = new HashMap<Character, HashSet<Character>>();
        selectMap = new HashMap<Character, HashMap<String, HashSet<Character>>>();
        numSet = new HashMap<Character, Integer>();

    }

    public void init() {
        initExpression();
        getVnVt();
        // Construct the first set of non-terminal symbols
        for (char c : VnSet) {
            getFirst(c);
        }
        // Construct the follow set of start characters
        // getFollow(S);
        // Construct non-terminal follow set
        for (char c : VnSet) {
            ArrayList<String> itemArr = expressionSet.get(c);
            for (String itemStr : itemArr) {
                getFirstSetX(itemStr);
            }
        }
        for (char c : VnSet) {
            getFollow(c);
        }

        for (char c : VnSet) {
            HashSet<Character> follow = followSet.get(c);
            follow.remove('^');
        }

        getSelect();
        createTable();
        output();
    }

    public void setgsArray(ArrayList<String> gsArray) {
        this.gsArray = gsArray;

    }

    public void setStart(Character start) {
        this.S = start;
    }

    public void initExpression() {
        Integer num = 0;
        expressionSet = new HashMap<Character, ArrayList<String>>();
        for (String gsItem : gsArray) {
            num = 0;
            String[] temp = gsItem.split("->");
            String lefts = temp[0];
            String rights = temp[1];
            char left = lefts.charAt(0);
            String[] right = rights.split("\\|");
            ArrayList<String> expArr = new ArrayList<String>();
            for (String s : right) {
                expArr.add(s);
                num++;
            }
            numSet.put(left, num);
            expressionSet.put(left, expArr);
        }
    }

    public void getVnVt() {
        for (String gsItem : gsArray) {// Separate non-terminal symbols from the left
            String[] temp = gsItem.split("->");
            char left = temp[0].charAt(0);
            VnSet.add(left);
        }
        for (String gsItem : gsArray) {// Separate non-terminal symbols from the right
            String[] temp = gsItem.split("->");
            String rights = temp[1];
            for (int i = 0; i < rights.length(); i++) {
                char charItem = rights.charAt(i);
                if (!VnSet.contains(charItem) && charItem != '|' && charItem != '^') {
                    VtSet.add(charItem);
                }
            }
        }
    }

    public void getFirst(char c) {

        if (firstSet.containsKey(c)) {
            return;
        }
        HashSet<Character> first = new HashSet<>();
        for (String itemStr : expressionSet.get(c)) {
            if (itemStr.equals("^")) {
                first.add('^');
            } else if (VtSet.contains(itemStr.charAt(0))) {
                first.add(itemStr.charAt(0));
            } else {
                for (char cur : itemStr.toCharArray()) {
                    if (!firstSet.containsKey(cur)) {
                        getFirst(cur);
                    }
                    HashSet<Character> curFirst = firstSet.get(cur);
                    first.addAll(curFirst);
                    if (!curFirst.contains('^')) {
                        break;
                    }
                }

            }
        }
        firstSet.put(c, first);
        return;
    }

    public void getFirstSetX(String s) {
        if (firstSetX.containsKey(s)) {
            return;
        }
        HashSet<Character> first = new HashSet<>();
        int i = 0;
        while (i < s.length()) {
            char cur = s.charAt(i);
            //if (!firstSet.containsKey(cur))
            // getFirst(cur);
            if (VtSet.contains(cur)) {
                first.add(cur);
                break;
            } else if (s.equals("^")) {
                first.add('^');
                break;
            } else {
                HashSet<Character> rightSet = firstSet.get(cur);
                // Add the first set corresponding to cur to the first set of the string
                first.addAll(rightSet);
                // If cur can reach ^, continue
                if (rightSet.contains('^')) {
                    i++;
                } else// Can't reach ^, then exit the loop
                {
                    break;
                }
                // If the tail is reached, the first set of all symbols contains an empty string
                if (i == s.length()) {
                    first.add('^');
                }
            }
        }
        firstSetX.put(s, first);
    }

    public void getFollow(Character c) {
        if (followSet.containsKey(c)) {
            return;
        }
        HashSet<Character> follow = new HashSet<Character>();
        if (c == S) {
            follow.add('#');
        }
        Iterator<Character> iterator = VnSet.iterator();
        while (iterator.hasNext()) {// Scan each left itemChar
            Character itemChar = iterator.next();
            ArrayList<String> itemArr = expressionSet.get(itemChar);
            for (String itemStr : itemArr) {
                char val_c = c.charValue();
                if (itemStr.indexOf(val_c) >= 0) {
                    for (int i = 0; i < itemStr.length(); i++) {
                        if (itemStr.charAt(i) == val_c) {// Prevent the situation where the right part is SaSb, that is, multiple occurrences in the right part							
                            if (i + 1 < itemStr.length()) {
                                Character temp = itemStr.charAt(i + 1);// A symbol after the non-terminal symbol s
                                if (temp == c) {
                                    continue;
                                }
                                if (VtSet.contains(temp))// The end is the terminator
                                {
                                    follow.add(temp);
                                } else if (VnSet.contains(temp)) {// The following is a non-terminal
                                    int j;
                                    for (j = i + 1; j < itemStr.length(); j++) {
                                        Character temps = itemStr.charAt(j);
                                        if (VtSet.contains(temps)) {
                                            follow.add(temps);
                                            break;
                                        }
                                        HashSet<Character> first = firstSet.get(temps);
                                        follow.addAll(first);
                                        if (!first.contains('^')) {
                                            break;
                                        }
                                    }
                                    if (j == itemStr.length()) {// The latter is all blank
                                        getFollow(itemChar);
                                        HashSet<Character> tempFollow = followSet.get(itemChar);
                                        follow.addAll(tempFollow);
                                    }
                                }
                            } else {
                                if (c == itemChar) {
                                    continue;// Prevent yourself from reaching your own endless loop
                                }
                                getFollow(itemChar);
                                HashSet<Character> tempFollow = followSet.get(itemChar);
                                follow.addAll(tempFollow);
                            }
                        }
                    }
                }

            }
        }

        followSet.put(c, follow);
    }

    public void getSelect() {
        Iterator<Character> iterator = VnSet.iterator();
        while (iterator.hasNext()) {// For each non-terminal characterChar
            Character itemChar = iterator.next();
            HashMap<String, HashSet<Character>> selectItemMap = new HashMap<String, HashSet<Character>>();
            ArrayList<String> itemArr = expressionSet.get(itemChar);
            for (String itemStr : itemArr) {// for each production right itemStr
                HashSet<Character> selectSet = new HashSet<Character>();
                HashSet<Character> temp = firstSetX.get(itemStr);
                if (itemStr.equals("^")) {
                    HashSet<Character> follow = followSet.get(itemChar);
                    selectSet.addAll(follow);
                } else if (temp.contains('^')) {
                    temp.remove('^');
                    selectSet.addAll(temp);
                    HashSet<Character> follow = followSet.get(itemChar);
                    selectSet.addAll(follow);
                } else {
                    selectSet.addAll(temp);
                }
                selectItemMap.put(itemStr, selectSet);

            }
            selectMap.put(itemChar, selectItemMap);
        }
    }

    public void createTable() {
        HashSet<Character> head = new HashSet<Character>(VtSet);
        head.add('#');
        Object[] VtArray = head.toArray();
        Object[] VnArray = VnSet.toArray();
        // Initialize the predictive analysis table
        table = new String[VnArray.length + 1][VtArray.length + 1];
        table[0][0] = "Vn/Vt";
        // Initialize the first column of the first row
        for (int i = 0; i < VtArray.length; i++) {
            table[0][i + 1] = VtArray[i].toString();
        }
        for (int i = 0; i < VnArray.length; i++) {
            table[i + 1][0] = VnArray[i] + "";
        }
        // set all errors
        for (int i = 0; i < VnArray.length; i++) {
            for (int j = 0; j < VtArray.length; j++) {
                table[i + 1][j + 1] = "  ";
            }
        }
        int i = 1, j = 1;
        Iterator<Character> iterator = VnSet.iterator();
        while (iterator.hasNext()) {
            Character itemChar = iterator.next();
            ArrayList<String> itemArr = expressionSet.get(itemChar);
            j = 1;
            for (Character temp : head) {
                for (String itemStr : itemArr) {
                    HashMap<String, HashSet<Character>> temp1 = selectMap.get(itemChar);
                    HashSet<Character> temp2 = temp1.get(itemStr);
                    if (temp2.contains(temp)) {
                        table[i][j] = itemStr;
                        break;
                    }
                }
                j++;
            }
            i++;
        }

    }

    public void output() {
        System.out.println("********* first episode ********");
        for (Character c : VnSet) {
            HashSet<Character> set = firstSet.get(c);
            System.out.printf("%10s", c + "  ->   ");
            for (Character var : set) {
                System.out.print(var);
            }
            System.out.println();
        }
        System.out.println("********** first episode **********");
        System.out.println("********** follow episode *********");

        for (Character c : VnSet) {
            HashSet<Character> set = followSet.get(c);
            System.out.print("Follow " + c + ":");
            for (Character var : set) {
                System.out.print(var);
            }
            System.out.println();
        }
        System.out.println("********** follow episode **********");

        System.out.println("********** LL1 Predictive Analysis Table ********");

        for (int i = 0; i < VnSet.size() + 1; i++) {
            for (int j = 0; j < VtSet.size() + 2; j++) {
                System.out.printf("%6s", table[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("********** LL1 Predictive Analysis Table ********");

    }

    public Character getStart() {
        return S;
    }

    public HashMap<Character, ArrayList<String>> getExpression() {
        return expressionSet;
    }

    public HashMap<Character, HashMap<String, HashSet<Character>>> getSelectMap() {
        return selectMap;
    }
}
