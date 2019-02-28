package client;

import client.model.HeroName;

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

    /**
     * created By Omidekz
     */
    private static final String _END_ = "\u001B[0m";
    public static final String RED = "\033[31m";
    public static final String FULL_WHITE = "\033[40m";
    public static final String UNDER_LINE = "\033[4m";
    public static final String FULL_BLUE = "\033[44m";
    public static final String FULL_PUSKOSI = "\033[41m";
    public static final String FULL_SOFT_GREEN = "\033[46m";
    public static final int WALL_TYPE = 0;
    public static final int OUR_RES_TYPE = 1;
    public static final int THEM_RES_TYPE = 2;
    public static final int OBJECTIVE_TYPE = 3;
    public static final int NORMAL_CELL_TYPE = 4;
    public static String Log(String log, String Color) {
        return Color + log + _END_;
    }

    public static String Log(String log, HeroName name) {
        switch (name) {
            case GUARDIAN:
                return Log(log, YELLOW);
            case HEALER:
                return Log(log, GREEN);
            case SENTRY:
                return Log(log, PURPLE);
            default:
                return Log(log, RED);
        }
    }

    public static String Log(String log, int type) {
        switch (type) {
            case WALL_TYPE:
                return Log(log, FULL_WHITE);
            case OUR_RES_TYPE:
                return Log(log, FULL_BLUE);
            case THEM_RES_TYPE:
                return Log(log, FULL_PUSKOSI);
            case NORMAL_CELL_TYPE:
                return Log(log, _END_);
            default:
                return Log(log, FULL_SOFT_GREEN);
        }
    }

}
