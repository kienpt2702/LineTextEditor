import java.util.Arrays;
import java.util.Scanner;

public class CommandLine {
    // Lots of stuff goes here
    private String command;
    private String[] arguments;
    private Scanner sc;

    public CommandLine() {
        sc = new Scanner(System.in);
    }

    public void readLine() {
        String[] tokens = sc.nextLine().trim().split(" ");
        command = tokens[0];
        arguments = Arrays.copyOfRange(tokens, 1, tokens.length);
    }

    public String getCommand() {
        return command;
    }

    public String[] getArguments() {
        return arguments;
    }
}
