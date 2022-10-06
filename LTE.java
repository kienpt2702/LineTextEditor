import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

class LTE {
    private static Buffer buffer;
    private static Buffer clipboard;
    private static CommandLine cmd;
    private static ArrayList<Command> commands;
    private static boolean done;

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

            // this help the finding in array to check same command name
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
        cmd = new CommandLine();
        commands = new ArrayList<>();
        generateCommand();


        while (!done) {
            System.out.print("LTE:> ");

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
            }
        }
    }

    public static void help() {

    }


    private static void read() {
        System.out.println(buffer.hasChanged());
        if (buffer.hasChanged()) {
            buffer.clear();
            // update clipboard
            clipboard.setList(new DLList<>(buffer.getList()));
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
                System.out.println(buffer.getList().getData());
            }

            br.close();
        } catch (Exception e) {
            System.out.println("==>> FILE DOES NOT EXIST <<==");
        }
    }

    private static void write() {
        DLList<String> data = buffer.getList();

        if (data.isEmpty()) {
            System.out.println("==>> BUFFER IS EMPTY <<==");
            return;
        }

        try {
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
        if(buffer.hasChanged()) {
            System.out.print("Save " + buffer.getFileName() + "(y/n)?: ");

            cmd.readLine();
            String command = cmd.getCommand();

            if(command.equalsIgnoreCase("y")) write();
        }

        forceQuit();
    }

    private static void forceQuit() {
        done = true;
    }

    private static void generateCommand() {
        commands.add(new Command("h", 0, "", "Display help"));
        commands.add(new Command("r", 1, "filespec", "Read a file into the current buffer"));
        commands.add(new Command("w", 0, "", "Write the current buffer to a file on disk"));
        commands.add(new Command("f", 1, "filespec", "Change the name of the current buffer"));
        commands.add(new Command("q", 0, "", "Quit the line editor"));
        commands.add(new Command("q!", 0, "", "Quit the line editor without saving"));
    }
}