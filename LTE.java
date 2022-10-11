import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

class LTE {
    private static Buffer buffer;
    private static Buffer clipboard;
    private static CommandLine cmd;
    private static ArrayList<Command> commands;
    private static boolean done;
    private static boolean toggle;
    private static Scanner sc;

    private static class Command {
        private String command;
        private String pattern;
        private String description;
        private int totalArgs;

        public Command(String command, int totalArgs) {
            this.command = command;
            this.totalArgs = totalArgs;
        }

        public Command(String command, int totalArgs, String pattern, String description) {
            this(command, totalArgs);
            this.pattern = pattern;
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Command other)) return false;
            return other.command.equals(this.command);
        }

        @Override
        public String toString() {
            return command + " " + pattern + " " + description;
        }
    }

    public static void main(String[] args) {
        System.out.println("LTE Project");

        buffer = new Buffer();
        clipboard = new Buffer();
        sc = new Scanner(System.in);
        cmd = new CommandLine(sc);
        commands = new ArrayList<>();
        generateCommand();

        int count = 1;
        while (!done) {
            String prompt = "LTE:" + buffer.getFileName() + ":" + count++;
            int lineNumber = 1;
            if (toggle) {
                if (!buffer.getList().isEmpty()) lineNumber = buffer.getList().getIndex() + 1;
                prompt += ":" + lineNumber;
            }
            prompt += " >>: ";

            System.out.print(prompt);

            cmd.readLine();
            String command = cmd.getCommand();
            String[] arguments = cmd.getArguments();
            Command curCmd = new Command(command, arguments.length);

            int index = commands.indexOf(curCmd);
            if (index < 0) {
                System.out.println("Invalid command");
                continue;
            }

            int totalArgs = commands.get(index).totalArgs;
            if (totalArgs != arguments.length) {
                System.out.println("Invalid number of arguments for command " + command + ": required " + totalArgs + ", actual " + arguments.length);
                continue;
            }

            try {
                switch (cmd.getCommand()) {
                    case "q!": {
                        forceQuit();
                        break;
                    }
                    case "q": {
                        quit();
                        break;
                    }
                    case "r": {
                        read();
                        break;
                    }
                    case "w": {
                        write();
                        break;
                    }
                    case "f": {
                        changeFileName(arguments[0]);
                        break;
                    }
                    case "t": {
                        top();
                        break;
                    }
                    case "b": {
                        bottom();
                        break;
                    }
                    case "g": {
                        int line = Integer.parseInt(arguments[0]);
                        gotoLine(line);
                        break;
                    }
                    case "-": {
                        goToPreviousLine();
                        break;
                    }
                    case "+": {
                        goToNextLine();
                        break;
                    }
                    case "=": {
                        printLineNumber();
                        break;
                    }
                    case "n": {
                        toggleLineNumber();
                        break;
                    }
                    case "#": {
                        printTotalCharAndLine();
                        break;
                    }
                    case "p": {
                        printLine();
                        break;
                    }
                    case "pr": {
                        int start = Integer.parseInt(arguments[0]);
                        int stop = Integer.parseInt(arguments[1]);
                        printRange(start, stop);
                        break;
                    }
                    case "?": {
                        searchBackward(arguments[0]);
                        break;
                    }
                    case "/": {
                        searchForward(arguments[0]);
                        break;
                    }
                    case "d": {
                        deleteCurrent();
                        break;
                    }
                    case "dr": {
                        int start = Integer.parseInt(arguments[0]);
                        int stop = Integer.parseInt(arguments[1]);
                        deleteRange(start, stop);
                        break;
                    }
                    case "c": {
                        copyCurrent();
                        break;
                    }
                    case "cr": {
                        int start = Integer.parseInt(arguments[0]);
                        int stop = Integer.parseInt(arguments[1]);
                        copyRange(start, stop);
                        break;
                    }
                    case "pa": {
                        pasteAbove();
                        break;
                    }
                    case "pb": {
                        pasteBelow();
                        break;
                    }
                    case "ia": {
                        insertAbove();
                        break;
                    }
                    case "ic": {
                        insertAt();
                        break;
                    }
                    case "ib": {
                        insertBelow();
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Invalid Arguments");
            }
        }
    }


    // need to fix
    // Reading a file will overwrite the contents of the current buffer.
    // If the dirty bit is set, prompt the user to save the contents of the current buffer before reading the file.
    private static void read() {
        if (buffer.hasChanged()) {
            // update clipboard
            clipboard.setList(new DLList<>(buffer.getList()));
            buffer.clear();
        }

        readFile(cmd.getArguments()[0]);
        buffer.setDirty(true);
    }

    private static void readFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            String s;
            while ((s = br.readLine()) != null) {
                buffer.getList().insertLast(s);
            }

            br.close();
        } catch (Exception e) {
            System.out.println("==>> FILE DOES NOT EXIST <<==");
        }
    }

    private static void write() {
        if (isEmpty()) return;

        try {
            DLList<String> data = buffer.getList();
            BufferedWriter bw = new BufferedWriter(new FileWriter(buffer.getFileName()));
            bw.write(data.toString());

            bw.close();
            buffer.setDirty(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void changeFileName(String fileName) {
        buffer.setFileName(fileName);
    }

    private static void quit() {
        if (buffer.hasChanged()) {
            System.out.print("Save " + buffer.getFileName() + "(y/n)?: ");

            cmd.readLine();
            String command = cmd.getCommand();

            if (command.equalsIgnoreCase("y")) write();
        }

        forceQuit();
    }

    private static void forceQuit() {
        done = true;
    }

    private static void top() {
        gotoLine(1);
    }

    private static void bottom() {
        gotoLine(buffer.getList().getSize());
    }

    private static void gotoLine(int num) {
        if (isEmpty()) return;

        if (!buffer.getList().seek(num - 1)) {
            System.out.println("==>> RANGE ERROR - num MUST BE [1.." + buffer.getList().getSize() + "] <<==");
        }

    }

    private static boolean goToPreviousLine() {
        if (isEmpty()) return false;

        boolean prev = buffer.getList().previous();
        if (!prev) {
            System.out.println("==>> ALREADY AT TOP OF BUFFER <<==");
        }

        return prev;
    }

    private static boolean goToNextLine() {
        if (isEmpty()) return false;

        boolean next = buffer.getList().next();
        if (!next) {
            System.out.println("==>> ALREADY AT BOTTOM OF BUFFER <<==");
        }
        return next;
    }

    private static void printLineNumber() {
        int lineNumber = 1;
        if (!buffer.getList().isEmpty()) lineNumber = buffer.getList().getIndex() + 1;
        System.out.println(lineNumber);
    }

    private static void toggleLineNumber() {
        toggle = !toggle;
    }

    private static void printTotalCharAndLine() {
        int lines = 0;
        int chars = 0;

        if (!buffer.getList().isEmpty()) {
            lines = buffer.getList().getSize();
            chars = buffer.getList().toString().length() - lines + 1;
        }

        System.out.print("Number of lines: " + lines);
        System.out.println(" Number of characters: " + chars);
    }

    private static void printLine() {
        int index = buffer.getList().getIndex() + 1;
        printRange(index, index);
    }

    private static void printRange(int start, int end) {
        if (isEmpty()) return;

        if (start > end) {
            System.out.println("START SHOULD NOT BE GREATER THAN END");
            return;
        }

        if (!checkRange(start) || !checkRange(end)) {
            System.out.println("==>> RANGE ERROR - start stop MUST BE [1.." + buffer.getList().getSize() + "] <<==");
            return;
        }

        String res = buffer.getList().getDataInRange(start - 1, end - 1);

        System.out.println(res);
    }

    private static void searchBackward(String pattern) {
        if (!goToPreviousLine()) return;

        int start = buffer.getList().getIndex() + 1;
        boolean found = false;
        do {
            if (!contains(pattern)) continue;

            found = true;
            System.out.print("Continue? (y/n): ");
            cmd.readLine();
            if (!cmd.getCommand().equalsIgnoreCase("y")) return;
        } while (buffer.getList().previous());


        if (!found) {
            buffer.getList().seek(start);
            System.out.println("==>> STRING " + pattern + " NOT FOUND <<==");
        }
    }

    private static void searchForward(String pattern) {
        if(isEmpty()) return;

        int start = buffer.getList().getIndex();
        boolean found = false;
        do {
            if (!contains(pattern)) continue;

            found = true;
            System.out.print("Continue? (y/n): ");
            cmd.readLine();
            if (!cmd.getCommand().equalsIgnoreCase("y")) return;
        } while (buffer.getList().next());


        if (!found) {
            buffer.getList().seek(start);
            System.out.println("==>> STRING " + pattern + " NOT FOUND <<==");
        }
    }


    private static boolean deleteRange(int start, int end) {
        if (!copyRange(start, end)) return false;

        DLList<String> data = buffer.getList();
        data.seek(start - 1);

        for (; start <= end; start++) {
            data.deleteAt();
        }
        return true;
    }

    private static boolean deleteCurrent() {
        int index = buffer.getList().getIndex() + 1;
        return deleteRange(index, index);
    }

    private static boolean copyRange(int start, int end) {
        if (!checkRangeDeleteCopy(start, end)) return false;

        String cp = buffer.getList().getDataInRange(start - 1, end - 1);

        DLList<String> clipboardData = clipboard.getList();
        if (!clipboardData.isEmpty()) clipboardData.clear();
        clipboardData.insertLast(cp);

        return true;
    }

    private static boolean copyCurrent() {
        int index = buffer.getList().getIndex() + 1;
        return copyRange(index, index);
    }

    private static void pasteAbove() {
        if (clipboardIsEmpty()) return;

        String lines = clipboard.getList().getData();
        insert(lines, true);
    }

    private static void pasteBelow() {
        if (clipboardIsEmpty()) return;

        String lines = clipboard.getList().getData();
        insert(lines, false);
    }

    private static void insertAbove() {
        String lines = readLine();
        insert(lines, true);
    }

    private static void insertAt() {
        int index = buffer.getList().getIndex();
        insertBelow();
        buffer.getList().seek(index);
    }

    private static void insertBelow() {
        String lines = readLine();
        insert(lines, false);
    }

    private static boolean contains(String pattern) {
        String data = buffer.getList().getData();
        if (data == null) return false;

        return data.contains(pattern);
    }

    private static String readLine() {
        StringBuilder str = new StringBuilder();
        while (true) {
            cmd.readLine();
            String line = cmd.getCommand();
            if (line.equals(".")) break;
            str.append(line).append("\n");
        }

        return !str.isEmpty() ? str.toString() : null;
    }

    private static boolean checkRangeDeleteCopy(int start, int end) {
        if (isEmpty()) return false;

        if (start > end || !checkRange(start) || !checkRange(end)) {
            System.out.println("==>> INDICES OUT OF RANGE <<==");
            return false;
        }

        return true;
    }

    private static boolean checkRange(int i) {
        return i >= 1 && i <= buffer.getList().getSize();
    }

    private static boolean isEmpty() {
        if (buffer.getList().isEmpty()) {
            System.out.println("==>> BUFFER IS EMPTY <<==");
            return true;
        }

        return false;
    }

    private static boolean clipboardIsEmpty() {
        if (clipboard.getList().isEmpty()) {
            System.out.println("==>> CLIPBOARD EMPTY <<==");
            return true;
        }

        return false;
    }

    private static void insert(String str, boolean above) {
        if (str == null) return;

        String[] lines = str.split("\n");
        DLList<String> data = buffer.getList();

        if (above) {
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i];
                data.insertAt(line);
            }
        } else {
            for (String line : lines) {
                data.addBelowCurrent(line);
            }
        }
        buffer.setDirty(true);
    }


    private static void generateCommand() {
        commands.add(new Command("h", 0, "", "Display help"));
        commands.add(new Command("r", 1, "filespec", "Read a file into the current buffer"));
        commands.add(new Command("w", 0, "", "Write the current buffer to a file on disk"));
        commands.add(new Command("f", 1, "filespec", "Change the name of the current buffer"));
        commands.add(new Command("q", 0, "", "Quit the line editor"));
        commands.add(new Command("q!", 0, "", "Quit the line editor without saving"));
        commands.add(new Command("t", 0, "", "Go to the first line in the buffer"));
        commands.add(new Command("b", 0, "", "Go to the last line in the buffer"));
        commands.add(new Command("g", 1, "num", "Go to line num in the buffer"));
        commands.add(new Command("-", 0, "", "Go to the previous line"));
        commands.add(new Command("+", 0, "", "Go to the next line"));
        commands.add(new Command("=", 0, "", "Print the current line number"));
        commands.add(new Command("n", 0, "", "Toggle line number displayed"));
        commands.add(new Command("#", 0, "", "Print the number of lines and characters in the buffer"));
        commands.add(new Command("p", 0, "", "Print the current line"));
        commands.add(new Command("pr", 2, "start stop", "Print several lines"));
        commands.add(new Command("?", 1, "pattern", "Search backwards for a pattern"));
        commands.add(new Command("/", 1, "pattern", "Search forwards for a pattern"));
        commands.add(new Command("d", 0, "", "Delete the current line from buffer and copy into the clipboard (CUT)"));
        commands.add(new Command("dr", 2, "start stop", "Delete several lines from buffer and copy into the clipboard (CUT"));
        commands.add(new Command("c", 0, "", "Copy current line into clipboard (COPY)"));
        commands.add(new Command("cr", 2, "start stop", "Copy lines between start and stop into the clipboard (COPY)"));
        commands.add(new Command("pa", 0, "", "Paste the contents of the clipboard above the current line (PASTE)"));
        commands.add(new Command("pb", 0, "", "Paste the contents of the clipboard below the current line (PASTE)"));
        commands.add(new Command("ia", 0, "", "Insert new lines of text above the current line until ”.” appears on its own line"));
        commands.add(new Command("ic", 0, "", "Insert new lines of text at the current line until ”.” appears on its own line (REPLACE current line)"));
        commands.add(new Command("ib", 0, "", "Insert new lines of text after the current line until ”.” appears on its own line"));
    }
}