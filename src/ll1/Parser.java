package ll1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Parser {
    // Production of string input

    private ArrayList<String> parserArray;

    public HashMap<Character, ArrayList<String>> expressionSet;
    // Nonterminal
    public HashSet<Character> Nonterm;
    // Terminal
    public HashSet<Character> Terminals;
    // Start character
    public Character Start;
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

    public Parser() {
        parserArray = new ArrayList<String>();
        Nonterm = new HashSet<Character>();
        Terminals = new HashSet<Character>();
        firstSet = new HashMap<Character, HashSet<Character>>();
        firstSetX = new HashMap<String, HashSet<Character>>();
        followSet = new HashMap<Character, HashSet<Character>>();
        selectMap = new HashMap<Character, HashMap<String, HashSet<Character>>>();
        numSet = new HashMap<Character, Integer>();

    }

    public void init() {
        initExpression();
        getNontermAndTerm();
        // Construct the first set of non-terminal symbols
        Nonterm.forEach(c -> {
            getFirst(c);
        });
        // Construct the follow set of start characters
        // getFollow(S);
        // Construct non-terminal follow set
        Nonterm.forEach(c -> {
            ArrayList<String> itemArr = expressionSet.get(c);
            itemArr.forEach(itemStr -> {
                getFirstSetX(itemStr);
            });
        });
        Nonterm.forEach(c -> {
            getFollow(c);
        });

        Nonterm.forEach(c -> {
            HashSet<Character> follow = followSet.get(c);
            follow.remove('Ɛ');
        });

        getSelect();
        createTable();
        output();
    }

    public void setParserArray(ArrayList<String> parserArray) {
        this.parserArray = parserArray;

    }

    public void setStart(Character start) {
        this.Start = start;
    }

    public void initExpression() {
        Integer num;
        expressionSet = new HashMap<Character, ArrayList<String>>();
        for (String parserItem : parserArray) {
            num = 0;
            String[] temp = parserItem.split("->");
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

    public void getNontermAndTerm() {
        parserArray.forEach(parserItem -> {
            // Separate non-terminal symbols from the left
            String[] temp = parserItem.split("->");
            char left = temp[0].charAt(0);
            Nonterm.add(left);
        });
        parserArray.forEach(parserItem -> {
            // Separate non-terminal symbols from the right
            String[] temp = parserItem.split("->");
            String rights = temp[1];
            for (int i = 0; i < rights.length(); i++) {
                char charItem = rights.charAt(i);
                if (!Nonterm.contains(charItem) && charItem != '|' && charItem != 'Ɛ') {
                    Terminals.add(charItem);
                }
            }
        });
    }

    public void getFirst(char c) {

        if (firstSet.containsKey(c)) {
            return;
        }
        HashSet<Character> first = new HashSet<>();
        for (String itemStr : expressionSet.get(c)) {
            if (itemStr.equals("Ɛ")) {
                first.add('Ɛ');
            } else if (Terminals.contains(itemStr.charAt(0))) {
                first.add(itemStr.charAt(0));
            } else {
                for (char cur : itemStr.toCharArray()) {
                    if (!firstSet.containsKey(cur)) {
                        getFirst(cur);
                    }
                    HashSet<Character> curFirst = firstSet.get(cur);
                    first.addAll(curFirst);
                    if (!curFirst.contains('Ɛ')) {
                        break;
                    }
                }

            }
        }
        firstSet.put(c, first);
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
            if (Terminals.contains(cur)) {
                first.add(cur);
                break;
            } else if (s.equals("Ɛ")) {
                first.add('Ɛ');
                break;
            } else {
                HashSet<Character> rightSet = firstSet.get(cur);
                // Add the first set corresponding to cur to the first set of the string
                first.addAll(rightSet);
                // If cur can reach Ɛ, continue
                if (rightSet.contains('Ɛ')) {
                    i++;
                } else// Can't reach Ɛ, then exit the loop
                {
                    break;
                }
                // If the tail is reached, the first set of all symbols contains an empty string
                if (i == s.length()) {
                    first.add('Ɛ');
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
        if (c == Start) {
            follow.add('$');
        }
        Iterator<Character> iterator = Nonterm.iterator();
        while (iterator.hasNext()) {// Scan each left itemChar
            Character itemChar = iterator.next();
            ArrayList<String> itemArr = expressionSet.get(itemChar);
            itemArr.forEach(itemStr -> {
                char val_c = c.charValue();
                if (itemStr.indexOf(val_c) >= 0) {
                    for (int i = 0; i < itemStr.length(); i++) {
                        if (itemStr.charAt(i) == val_c) {// Prevent the situation where the right part is SaSb, that is, multiple occurrences in the right part							
                            if (i + 1 < itemStr.length()) {
                                Character temp = itemStr.charAt(i + 1);// A symbol after the non-terminal symbol s
                                if (temp == c) {
                                    continue;
                                }
                                if (Terminals.contains(temp))// The end is the terminator
                                {
                                    follow.add(temp);
                                } else if (Nonterm.contains(temp)) {// The following is a non-terminal
                                    int j;
                                    for (j = i + 1; j < itemStr.length(); j++) {
                                        Character temps = itemStr.charAt(j);
                                        if (Terminals.contains(temps)) {
                                            follow.add(temps);
                                            break;
                                        }
                                        HashSet<Character> first = firstSet.get(temps);
                                        follow.addAll(first);
                                        if (!first.contains('Ɛ')) {
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

            });
        }

        followSet.put(c, follow);
    }

    public void getSelect() {
        Iterator<Character> iterator = Nonterm.iterator();
        while (iterator.hasNext()) {// For each non-terminal characterChar
            Character itemChar = iterator.next();
            HashMap<String, HashSet<Character>> selectItemMap = new HashMap<String, HashSet<Character>>();
            ArrayList<String> itemArr = expressionSet.get(itemChar);
            itemArr.forEach(itemStr -> {
                // for each production right itemStr
                HashSet<Character> selectSet = new HashSet<Character>();
                HashSet<Character> temp = firstSetX.get(itemStr);
                if (itemStr.equals("Ɛ")) {
                    HashSet<Character> follow = followSet.get(itemChar);
                    selectSet.addAll(follow);
                } else if (temp.contains('Ɛ')) {
                    temp.remove('Ɛ');
                    selectSet.addAll(temp);
                    HashSet<Character> follow = followSet.get(itemChar);
                    selectSet.addAll(follow);
                } else {
                    selectSet.addAll(temp);
                }
                selectItemMap.put(itemStr, selectSet);
            });
            selectMap.put(itemChar, selectItemMap);
        }
    }

    public void createTable() {
        HashSet<Character> head = new HashSet<Character>(Terminals);
        head.add('$');
        Object[] terminalsArray = head.toArray();
        Object[] nonTermArray = Nonterm.toArray();
        // Initialize the predictive analysis table
        table = new String[nonTermArray.length + 1][terminalsArray.length + 1];
        table[0][0] = "nT/T";
        // Initialize the first column of the first row
        for (int i = 0; i < terminalsArray.length; i++) {
            table[0][i + 1] = terminalsArray[i].toString();
        }
        for (int i = 0; i < nonTermArray.length; i++) {
            table[i + 1][0] = nonTermArray[i] + "";
        }
        // set all errors
        for (int i = 0; i < nonTermArray.length; i++) {
            for (int j = 0; j < terminalsArray.length; j++) {
                table[i + 1][j + 1] = "  ";
            }
        }
        int i = 1, j = 1;
        Iterator<Character> iterator = Nonterm.iterator();
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
        System.out.println("            --First");
        Nonterm.forEach(c -> {
            HashSet<Character> set = firstSet.get(c);
            System.out.printf("%10s", c + "  ->   ");
            set.forEach(var -> {
                System.out.print(var);
            });
            System.out.println();
        });
        System.out.println("            --Follow");

        Nonterm.forEach(c -> {
            HashSet<Character> set = followSet.get(c);
            System.out.print("Follow " + c + ":");
            set.forEach(var -> {
                System.out.print(var);
            });
            System.out.println();
        });

        System.out.println("            --LL1 Parsing Table");

        for (int i = 0; i < Nonterm.size() + 1; i++) {
            for (int j = 0; j < Terminals.size() + 2; j++) {
                System.out.printf("%6s", table[i][j] + " ");
            }
            System.out.println();
            System.out.println("---------------------------------------------");
        }
    }

    public Character getStart() {
        return Start;
    }

    public HashMap<Character, ArrayList<String>> getExpression() {
        return expressionSet;
    }

    public HashMap<Character, HashMap<String, HashSet<Character>>> getSelectMap() {
        return selectMap;
    }
}
