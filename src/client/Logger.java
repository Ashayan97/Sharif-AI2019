package client;

public class Logger {
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";

    public static void log(String log){
        System.out.println(BLUE+" "+log+"\u001B[0m");
    }
    public static void log(String log , String Color){
        System.out.println(Color+" "+log+"\u001B[0m");
    }
}
