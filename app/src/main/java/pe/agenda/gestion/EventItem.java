package pe.agenda.gestion;

import java.util.ArrayList;
import java.util.List;

public class EventItem {
    public long id;
    public int managementYear;
    public String title="", details="", startDate="", endDate="", time="", place="", responsible="";
    public String priority="SE_ACERCA", status="PENDIENTE", prepStatus="PENDIENTE", notes="", source="Actividad manual";
    public boolean syncEnabled=true;
    public Long calendarEventId=null, calendarId=null;
    public int reminderMinutes=1440;
    public long modifiedAt=0, lastSyncedAt=0;
    public List<Subtask> subtasks=new ArrayList<>();

    public static class Subtask {
        public long id;
        public String title="";
        public boolean done=false;
        public Subtask() {}
        public Subtask(String t, boolean d){title=t;done=d;}
    }
}
