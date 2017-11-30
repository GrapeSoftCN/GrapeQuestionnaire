package Test;

import httpServer.booter;
import nlogger.nlogger;

public class TestQuest {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("GrapeQuestionnaireTest");
            System.setProperty("AppName", "GrapeQuestionnaireTest");
            booter.start(1008);
        } catch (Exception e) {
            nlogger.logout(e);
        }
    }
}
