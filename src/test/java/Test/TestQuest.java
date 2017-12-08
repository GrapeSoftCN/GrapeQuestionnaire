package Test;

import httpServer.booter;
import nlogger.nlogger;

public class TestQuest {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("GrapeQuestionnaire");
            System.setProperty("AppName", "GrapeQuestionnaire");
            booter.start(1008);
        } catch (Exception e) {
            nlogger.logout(e);
        }
    }
}
