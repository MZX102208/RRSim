import java.io.*;
import java.util.*;

public class Main {

    private static List<Person> people = new ArrayList<>();
    private static Table[] tables;
    private static List<Person> queue = new ArrayList<>();
    private static List<MatchResult> results = new ArrayList<>();
    private static List<String> skipArr = new ArrayList<>();


    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("1) Start new game");
        System.out.println("2) Continue");

        int selected = input.nextInt();
        input.nextLine();
        switch (selected) {
            case 1:
                newGame(input);
                break;
            case 2:
                readFromFile(input);
                break;
            default:
                System.out.println("***Invalid input***");
        }

        boolean isRunning = true;
        while (stillPlaying() && isRunning) {
            System.out.println("1) Print table states");
            System.out.println("2) Finish Table");
            System.out.println("3) Start Table");
            System.out.println("4) Print scores");
            System.out.println("5) Manage inactive");
            System.out.println("6) Empty Table");
            System.out.println("7) Set to table");
            System.out.println("8) Add person");
            System.out.println("9) Add tables");
            System.out.println("10) Exit and Save");
            System.out.println("11) Print queue");

            selected = input.nextInt();
            input.nextLine();
            switch (selected) {
                case 1:
                    printTables();
                    break;
                case 2:
                    System.out.print("Which table: ");
                    int finishTableNum = input.nextInt() - 1;
                    input.nextLine();
                    finishTable(input, finishTableNum);
                    break;
                case 3:
                    System.out.print("Which table: ");
                    int startNewTableNum = input.nextInt() - 1;
                    input.nextLine();
                    startTable(startNewTableNum);
                    break;
                case 4:
                    printResults();
                    break;
                case 5:
                    manageInActive(input);
                    break;
                case 6:
                    System.out.print("Which table: ");
                    int emptyTableNum = input.nextInt() - 1;
                    input.nextLine();
                    removeTable(emptyTableNum);
                    break;
                case 7:
                    System.out.print("Which table: ");
                    int addTableNum = input.nextInt() - 1;
                    input.nextLine();
                    System.out.print("Input player 1: ");
                    String p1Name = input.nextLine();
                    System.out.print("Input player 2: ");
                    String p2Name = input.nextLine();
                    addToTable(addTableNum, p1Name, p2Name);
                    break;
                case 8:
                    addPeopleToListDuringGame(input);
                    break;
                case 9:
                    System.out.print("How many: ");
                    int numTables = input.nextInt();
                    input.nextLine();
                    addMoreTables(numTables);
                    break;
                case 10:
                    isRunning = false;
                    break;
                case 11:
                    printQueue();
                    break;
                default:
                    System.out.println("***Invalid input***");
            }
            writeToFile();
        }
        printResults();
    }

    public static void printQueue() {
        for (int i = 0; i < queue.size(); i++) {
            if (skipArr.contains(queue.get(i).name)) System.out.print("(Inactive) ");
            System.out.println(queue.get(i).name + "--Team: " + queue.get(i).team);
        }
    }

    public static void manageInActive(Scanner input) {
        boolean isManaging = true;
        while (isManaging) {
            System.out.println("1) Add Inactive");
            System.out.println("2) Remove Inactive");
            System.out.println("3) Exit");
            int userInput = Integer.parseInt(input.nextLine());
            switch (userInput) {
                case 1:
                    addInactive(input);
                    break;
                case 2:
                    removeInactive(input);
                    break;
                case 3:
                    isManaging = false;
                    break;
                default:
                    System.out.println("Invalid input.");
                    break;
            }
        }
    }

    public static void addInactive(Scanner input) {
        boolean isAdding = true;
        String name;

        while (isAdding) {
            System.out.println("Add person to inactive list (Type \"exit\" to return):");
            name = input.nextLine();
            if (!name.equals("exit")) {
                skipArr.add(name);
                System.out.println(name + " is made inactive.");
            } else {
                isAdding = false;
            }
        }
    }

    public static void removeInactive(Scanner input) {
        boolean isAdding = true;
        String name;

        while (isAdding) {
            System.out.println("Remove person from inactive list (Type \"exit\" to return):");
            name = input.nextLine();
            if (!name.equals("exit")) {
                skipArr.remove(name);
                System.out.println(name + " is made active.");
            } else {
                isAdding = false;
            }
        }
    }

    public static void addMoreTables(int numTables) {
        int prevNumTables = tables.length;
        Table[] newArr = new Table[prevNumTables + numTables];
        for (int i = 0; i < prevNumTables; i++) {
            newArr[i] = tables[i];
        }
        tables = newArr;
        for (int i = prevNumTables; i < tables.length; i++) {
            tables[i] = new Table();
            startTable(i);
        }
    }

    public static void writeToFile() {
        try {
            FileWriter writer = new FileWriter("SaveData.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            for (Person p : people) {
                bufferedWriter.write(p.name);
                bufferedWriter.newLine();
                bufferedWriter.write("" + p.team);
                bufferedWriter.newLine();
            }
            bufferedWriter.write("exit");
            bufferedWriter.newLine();

            for (MatchResult history : results) {
                bufferedWriter.write(history.p1Name);
                bufferedWriter.newLine();
                bufferedWriter.write(history.p2Name);
                bufferedWriter.newLine();
                bufferedWriter.write(history.result);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile(Scanner input) {
        try {
            Scanner s = new Scanner(new File("SaveData.txt"));
            String name;
            while (!(name = s.nextLine()).equals("exit")) {
                Person p = new Person(name);
                p.team = Integer.parseInt(s.nextLine());
                people.add(p);
            }
            while(s.hasNext()) {
                Person p1 = findPlayerByName(s.nextLine());
                Person p2 = findPlayerByName(s.nextLine());
                String result = s.nextLine();

                int p1Games = Integer.parseInt(result.split("-")[0]);
                int p2Games = Integer.parseInt(result.split("-")[1]);
                p1.numGameWins += p1Games;
                p2.numGameWins += p2Games;
                p1.played.put(p2.name, p1Games + "-" + p2Games);
                p2.played.put(p1.name, p2Games + "-" + p1Games);

                if (p1Games > p2Games) p1.numMatchWins++;
                else p2.numMatchWins++;
                results.add(new MatchResult(p1.name, p2.name, result));
            }
            for (Person p : people) {
                if (!(p.played.keySet().size() == people.size() - 1)) {
                    queue.add(p);
                }
            }
            Collections.shuffle(queue);

            System.out.println("# of tables: ");
            tables = new Table[input.nextInt()];
            input.nextLine();

            for (int i = 0; i < tables.length; i++) {
                tables[i] = new Table();
                startTable(i);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Person findPlayerByName(String name) {
        for (Person p : people) {
            if (p.name.equals(name)) return p;
        }
        return null;
    }

    public static void newGame(Scanner input) {
        boolean isCreating = true;
        int numTeams = 1;
        int numTables = 1;
        while (isCreating) {
            System.out.println("Creating a new game...");
            System.out.println("# of Teams: " + numTeams + ", # of Tables: " + numTables);
            System.out.println("1) Add people");
            System.out.println("2) Set Teams");
            System.out.println("3) Set tables");
            System.out.println("4) Exit");

            int selected = input.nextInt();
            input.nextLine();
            switch (selected) {
                case 1:
                    addPeopleToList(input);
                    break;
                case 2:
                    numTeams = promptNumTeams(input);
                    input.nextLine();
                    break;
                case 3:
                    numTables = promptNumTables(input);
                    input.nextLine();
                    break;
                case 4:
                    isCreating = false;
                    break;
                default:
                    System.out.println("***Invalid input***");
            }
        }
        generateRR(numTeams, numTables);
    }

    public static void addPeopleToList(Scanner input) {
        boolean isAdding = true;
        String name;

        while (isAdding) {
            System.out.println("Add new person (Type \"exit\" to return):");
            name = input.nextLine();
            if (!name.equals("exit")) {
                Person p = new Person(name);
                System.out.print("What is their rating: ");
                p.rating = input.nextInt();
                input.nextLine();
                people.add(p);
            } else {
                isAdding = false;
            }
        }
    }

    public static void addPeopleToListDuringGame(Scanner input) {
        boolean isAdding = true;
        String name;

        while (isAdding) {
            System.out.println("Add new person (Type \"exit\" to return):");
            name = input.nextLine();
            if (!name.equals("exit")) {
                Person p = new Person(name);
                System.out.print("What is their team (Starts at 1): ");
                p.team = input.nextInt() - 1;
                input.nextLine();
                people.add(p);
            } else {
                isAdding = false;
            }
        }
    }

    public static void removeTable(int tableNum) {
        System.out.println("Removing " + tables[tableNum].p1.name + " and " + tables[tableNum].p2.name);
        queue.add(tables[tableNum].p1);
        queue.add(tables[tableNum].p2);
        tables[tableNum].p1 = null;
        tables[tableNum].p2 = null;
    }

    public static void addToTable(int tableNum, String p1Name, String p2Name) {
        Person p1 = findPlayerByName(p1Name);
        Person p2 = findPlayerByName(p2Name);
        if (p1 == null || p2 == null) {
            System.out.println("Cannot find players with those names");
            return;
        }
        System.out.println(p1.name + " and " + p2.name + " are playing on Table " + (tableNum + 1));
        tables[tableNum].p1 = p1;
        tables[tableNum].p2 = p2;
        queue.remove(p1);
        queue.remove(p2);
    }

    public static int promptNumTeams(Scanner input) {
        System.out.print("New # of Teams: ");
        int numTeams = input.nextInt();

        if (numTeams < 1) {
            System.out.println("# of Teams cannot be less than 1.");
            numTeams = 1;
        }

        return numTeams;
    }

    public static int promptNumTables(Scanner input) {
        System.out.print("New # of Tables: ");
        int numTables = input.nextInt();

        if (numTables < 1) {
            System.out.println("# of Tables cannot be less than 1.");
            numTables = 1;
        }

        return numTables;
    }

    public static void generateRR(int numTeams, int numTables) {
        tables = new Table[numTables];
        for (int i = 0; i < tables.length; i++) {
            tables[i] = new Table();
        }

        Collections.shuffle(people);
        Collections.sort(people);
        int i = 0;
        for (Person p : people) {
            p.team = i;
            i = (i + 1) % numTeams;
            queue.add(p);
        }
        Collections.shuffle(queue);
        printTeams();
        for (int j = 0; j < tables.length; j++) {
            startTable(j);
        }
    }

    public static void printTeams() {
        Map<Integer, List<String>> teamMap = new TreeMap<>();
        for (Person p : people) {
            if (!teamMap.containsKey(p.team))
                teamMap.put(p.team, new ArrayList<>());
            teamMap.get(p.team).add(p.name);
        }

        for (int teamNum : teamMap.keySet()) {
            System.out.println("Team " + teamNum + ":");
            for (String s : teamMap.get(teamNum)) System.out.println(" - " + s);
        }
    }

    public static void startTableWithPrompt(int tableNum, Scanner input) {
        boolean isGood = false;
        while (!isGood) {
            startTable(tableNum);
            System.out.println("Is this good? (Y/N)");
            String userInput = input.nextLine().toLowerCase();
            if (userInput.equals("y")) isGood = true;
            else removeTable(tableNum);
        }
    }

    public static void startTable(int tableNum) {
        if (tables[tableNum].p1 != null) {
            System.out.println("Table still has a game going on.");
            return;
        }
        if (tableNum < 0 || tableNum > tables.length - 1) {
            System.out.println("Invalid table number");
            return;
        }
        for (int i = 0; i < queue.size(); i++) {
            if (skipArr.contains(queue.get(i).name)) {
                continue;
            }
            for (int j = i + 1; j < queue.size(); j++) {
                if (!queue.get(i).played.containsKey(queue.get(j).name) && queue.get(i).team == queue.get(j).team) {
                    System.out.println(queue.get(i).name + " and " + queue.get(j).name + " are playing on Table " + (tableNum + 1));
                    tables[tableNum].p1 = queue.remove(i);
                    tables[tableNum].p2 = queue.remove(j - 1);
                    return;
                }
            }
        }
        System.out.println("No matches are able to be made. ");
    }

    public static void finishTable(Scanner input, int tableNum) {
        Person p1 = tables[tableNum].p1;
        Person p2 = tables[tableNum].p2;

        System.out.println("What is the score for: ");
        System.out.println(p1.name + " - " + p2.name);

        String score = input.nextLine();
        if (!score.equals("skip") && !score.matches("\\d+-\\d+")) {
            System.out.println("Invalid input.");
            return;
        }
        if (!score.equals("skip")) {
            String[] respectiveWins = score.split("-");
            int p1Wins = Integer.parseInt(respectiveWins[0]);
            int p2Wins = Integer.parseInt(respectiveWins[1]);

            p1.numGameWins += p1Wins;
            p2.numGameWins += p2Wins;
            if (p1Wins > p2Wins) {
                p1.numMatchWins++;
            } else {
                p2.numMatchWins++;
            }
            p1.played.put(p2.name, p1Wins + "-" + p2Wins);
            p2.played.put(p1.name, p2Wins + "-" + p1Wins);
            results.add(new MatchResult(p1.name, p2.name, score));
        }

        if (!(p2.played.keySet().size() == people.size() - 1))
            queue.add(p2);
        if (!(p1.played.keySet().size() == people.size() - 1))
            queue.add(p1);

        tables[tableNum].p1 = null;
        tables[tableNum].p2 = null;
        startTableWithPrompt(tableNum, input);
    }

    public static void printTables() {
        System.out.println("-----------------------------------------------------------------");
        for (int i = 0; i < tables.length; i++) {
            System.out.println("Table " + (i + 1) + ":");
            if (tables[i].p1 != null && tables[i].p2 != null)
                System.out.println(tables[i].p1.name + " *vs* " + tables[i].p2.name);
            else
                System.out.println("***UNUSED***");
            System.out.println("-----------------------------------------------------------------");
        }
    }

    public static boolean stillPlaying() {
        for (Table t : tables) {
            if (t.p1 != null) return true;
        }
        return !queue.isEmpty();
    }

    public static void printResults() {
        Map<Integer, List<Person>> teams = new TreeMap<>();
        for (Person p : people) {
            if (!teams.containsKey(p.team))
                teams.put(p.team, new ArrayList<>());
            teams.get(p.team).add(p);
        }
        for (int teamNum : teams.keySet()) {
            sortList(teams.get(teamNum));
            System.out.println("Team " + teamNum + " -------------------");
            for (Person p : teams.get(teamNum)) {
                System.out.println(p.name + " M:" + p.numMatchWins + ", G:" + p.numGameWins + ", T: " +  p.played.keySet().size());
            }
        }
    }

    public static void sortList(List<Person> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size() - (i + 1); j++) {
                Person p1 = list.get(j);
                Person p2 = list.get(j + 1);
                if (p1.numMatchWins < p2.numMatchWins) {
                    list.set(j, p2);
                    list.set(j + 1, p1);
                } else if (p1.numMatchWins == p2.numMatchWins) {
                    if (p1.numGameWins < p2.numGameWins) {
                        list.set(j, p2);
                        list.set(j + 1, p1);
                    }
                }
            }
        }
    }

}

class Person implements Comparable<Person> {
    Map<String, String> played = new HashMap<>();
    int numMatchWins;
    int numGameWins;
    int team;
    int rating = 0;
    String name;

    public Person(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Person o) {
        return o.rating - rating;
    }
}

class Table {
    Person p1;
    Person p2;
}

class MatchResult {
    String p1Name;
    String p2Name;
    String result;

    public MatchResult(String p1Name, String p2Name, String result) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.result = result;
    }
}
