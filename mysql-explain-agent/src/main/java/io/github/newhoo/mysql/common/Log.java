package io.github.newhoo.mysql.common;

public class Log {

    public static void debug(String msg, Object... args) {
        if (Config.isDebug) {
            System.out.printf("[mysql-explain] " + msg + "\n", args);
        }
    }

    public static void info(String msg, Object... args) {
        System.out.printf("[mysql-explain] " + msg + "\n", args);
    }

    public static void error(Exception e, String msg, Object... args) {
        System.err.printf("[mysql-explain] " + msg + "\n", args);
        e.printStackTrace();
    }
}
