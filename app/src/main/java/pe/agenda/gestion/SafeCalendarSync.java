package pe.agenda.gestion;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CalendarContract;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * 365 Agenda v7: Google Calendar is authoritative when the user taps Refresh.
 * Never upload seeded activities during a refresh. Keep private notes/tasks/Drive
 * links, and hide obsolete calendar copies rather than permanently deleting data.
 */
public final class SafeCalendarSync {
    private SafeCalendarSync() {}
    private static final String ACCOUNT = "adi.ernesto.ams@gmail.com";
    private static final TimeZone LIMA = TimeZone.getTimeZone("America/Lima");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    static final class Remote {
        long id;
        String title="", date="", end="", time="", place="", details="";
        String audience="", category="", priority="", status="";
    }

    /** Strictly select the intended, writable Google calendar. */
    public static long findCalendar(Context ctx) {
        String[] p={CalendarContract.Calendars._ID,CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL};
        try(Cursor c=ctx.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,p,
            CalendarContract.Calendars.VISIBLE+"=1 AND "+CalendarContract.Calendars.SYNC_EVENTS+"=1",null,null)) {
            if(c!=null)while(c.moveToNext()){
                if(ACCOUNT.equalsIgnoreCase(val(c,1)) && "com.google".equals(val(c,2))
                        && c.getInt(3)>=CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)
                    return c.getLong(0);
            }
        }
        return -1;
    }

    public static int pull(Context ctx, AgendaDb3 db, long calendarId, int year) throws Exception {
        if(calendarId<0)throw new IllegalStateException("Activa el calendario Google de "+ACCOUNT);
        SQLiteDatabase sql=db.getWritableDatabase();
        ensureHiddenColumn(sql);
        Calendar a=Calendar.getInstance(LIMA);a.clear();a.set(year,0,1);
        Calendar b=Calendar.getInstance(LIMA);b.clear();b.set(year+1,0,1);
        String sel=CalendarContract.Events.CALENDAR_ID+"=? AND "+
            CalendarContract.Events.DTSTART+">=? AND "+CalendarContract.Events.DTSTART+"<? AND "+
            CalendarContract.Events.DELETED+"=0";
        String[] args={String.valueOf(calendarId),String.valueOf(a.getTimeInMillis()),String.valueOf(b.getTimeInMillis())};
        String[] pr={CalendarContract.Events._ID,CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_LOCATION};
        List<Remote> remote=new ArrayList<>();
        try(Cursor c=ctx.getContentResolver().query(CalendarContract.Events.CONTENT_URI,pr,sel,args,CalendarContract.Events.DTSTART+" ASC")){
            if(c==null)throw new IllegalStateException("No fue posible leer Google Calendar");
            while(c.moveToNext()){
                String title=val(c,1).trim();
                if(title.isEmpty())continue;
                Remote e=new Remote();e.id=c.getLong(0);e.title=title;e.place=val(c,6);
                String desc=val(c,2);e.details=stripMetadata(desc);
                e.audience=meta(desc,"Tipo");e.category=meta(desc,"Categoría");
                if(e.category.isEmpty())e.category=meta(desc,"Categoria");
                e.priority=meta(desc,"Prioridad");e.status=meta(desc,"Estado");
                long start=c.getLong(3),end=c.isNull(4)?start:c.getLong(4);
                boolean all=c.getInt(5)==1;
                e.date=date(start,all?UTC:LIMA);
                if(all){
                    Calendar ce=Calendar.getInstance(UTC);ce.setTimeInMillis(end);
                    ce.add(Calendar.DAY_OF_MONTH,-1);
                    e.end=date(ce.getTimeInMillis(),UTC);
                }else{
                    e.end=date(end,LIMA);
                    SimpleDateFormat hm=new SimpleDateFormat("HH:mm",Locale.US);
                    hm.setTimeZone(LIMA);e.time=hm.format(new java.util.Date(start));
                }
                if(e.end.compareTo(e.date)<0)e.end=e.date;
                remote.add(e);
            }
        }
        // If Calendar Provider has not downloaded the user's calendar yet, do not
        // hide or rewrite any local record. Force Google sync on the device first.
        if(remote.isEmpty())throw new IllegalStateException("Google Calendar aún no descargó eventos. Sincronízalo en Android y vuelve a pulsar ↻ Calendar.");

        Set<Long> seen=new HashSet<>();
        int updated=0;
        for(Remote r:remote){
            AgendaDb3.Event local=byCalendarId(sql,r.id);
            if(local==null)local=findSafeMatch(sql,r,year);
            if(local==null) {
                local=new AgendaDb3.Event();local.year=year;local.title=r.title;local.startDate=r.date;
                local.endDate=r.end;local.time=r.time;local.place=r.place;local.details=r.details;
                local.source="Google Calendar";local.category=cat(r);
                local.calendarEventId=String.valueOf(r.id);
                local.id=db.insert(local);
            } else {
                ContentValues v=new ContentValues();
                v.put("title",r.title);v.put("year",year);v.put("start_date",r.date);
                v.put("end_date",r.end);v.put("event_time",r.time);v.put("place",r.place);
                // Calendar descriptions never overwrite the private APUNTES field.
                if(!r.details.isEmpty())v.put("details",r.details);
                v.put("calendar_event_id",String.valueOf(r.id));v.put("sync_enabled",1);
                v.put("sync_hidden",0);
                if(!r.category.isEmpty())v.put("category",cat(r));
                if(!r.priority.isEmpty() && (r.priority.equals("URGENTE")||r.priority.equals("PUEDE_ESPERAR")||r.priority.equals("SE_ACERCA")))
                    v.put("priority",r.priority);
                if(!r.audience.isEmpty())v.put("audience",r.audience.equalsIgnoreCase("INTERNO")?"INTERNO":"PUBLICO");
                sql.update("events",v,"id=?",new String[]{String.valueOf(local.id)});
            }
            seen.add(local.id);
            // Merge stale seed/copies of the same activity into the kept record.
            for(AgendaDb3.Event other:findEquivalentLocal(sql,r,year,local.id)){
                mergePrivateData(sql,local.id,other.id);
                hide(sql,other.id);
                seen.add(other.id);
            }
            updated++;
        }

        // Deleted or moved calendar records must never be re-uploaded from the
        // tablet. Soft-hide them so APUNTES and subtasks remain recoverable.
        try(Cursor c=sql.query("events",new String[]{"id","calendar_event_id"},
                "year=? AND calendar_event_id<>'' AND sync_hidden=0",
                new String[]{String.valueOf(year)},null,null,null)){
            List<Long> missing=new ArrayList<>();
            while(c.moveToNext())if(!seen.contains(c.getLong(0)))missing.add(c.getLong(0));
            for(Long id:missing)hide(sql,id);
        }
        return updated;
    }

    private static void ensureHiddenColumn(SQLiteDatabase sql) {
        boolean found=false;
        try(Cursor c=sql.rawQuery("PRAGMA table_info(events)",null)){
            while(c.moveToNext())if("sync_hidden".equals(c.getString(1)))found=true;
        }
        if(!found)sql.execSQL("ALTER TABLE events ADD COLUMN sync_hidden INTEGER NOT NULL DEFAULT 0");
    }

    private static AgendaDb3.Event byCalendarId(SQLiteDatabase sql,long remoteId){
        try(Cursor c=sql.query("events",new String[]{"id"},"calendar_event_id=?",
                new String[]{String.valueOf(remoteId)},null,null,"sync_hidden ASC, id ASC","1")){
            if(c.moveToFirst())return get(sql,c.getLong(0));
        }
        return null;
    }

    private static AgendaDb3.Event get(SQLiteDatabase sql,long id){
        try(Cursor c=sql.query("events",null,"id=?",new String[]{String.valueOf(id)},null,null,null,"1")){
            if(!c.moveToFirst())return null;
            AgendaDb3.Event e=new AgendaDb3.Event();e.id=id;
            e.title=column(c,"title");e.startDate=column(c,"start_date");e.endDate=column(c,"end_date");
            e.notes=column(c,"notes");e.driveFolderUrl=column(c,"drive_folder_url");
            e.driveVideoUrl=column(c,"drive_video_url");e.driveFotosUrl=column(c,"drive_fotos_url");
            e.calendarEventId=column(c,"calendar_event_id");e.source=column(c,"source");
            return e;
        }
    }

    private static AgendaDb3.Event findSafeMatch(SQLiteDatabase sql,Remote r,int year){
        AgendaDb3.Event best=null;int bestScore=-1,second=-1;
        for(AgendaDb3.Event e:findCandidates(sql,year)){
            int s=score(e,r);
            if(s>bestScore){second=bestScore;bestScore=s;best=e;}else if(s>second)second=s;
        }
        return bestScore>=80 && bestScore>second ? best : null;
    }

    private static List<AgendaDb3.Event> findEquivalentLocal(SQLiteDatabase sql,Remote r,int year,long excluded){
        List<AgendaDb3.Event> out=new ArrayList<>();
        for(AgendaDb3.Event e:findCandidates(sql,year))
            if(e.id!=excluded && score(e,r)>=85)out.add(e);
        return out;
    }

    private static List<AgendaDb3.Event> findCandidates(SQLiteDatabase sql,int year){
        List<AgendaDb3.Event> out=new ArrayList<>();
        try(Cursor c=sql.query("events",new String[]{"id","title","start_date","end_date","notes",
            "drive_folder_url","drive_video_url","drive_fotos_url","calendar_event_id","source"},
            "year=?",new String[]{String.valueOf(year)},null,null,null)){
            while(c.moveToNext()){
                AgendaDb3.Event e=new AgendaDb3.Event();
                e.id=c.getLong(0);e.title=val(c,1);e.startDate=val(c,2);e.endDate=val(c,3);
                e.notes=val(c,4);e.driveFolderUrl=val(c,5);e.driveVideoUrl=val(c,6);
                e.driveFotosUrl=val(c,7);e.calendarEventId=val(c,8);e.source=val(c,9);
                out.add(e);
            }
        }
        return out;
    }

    private static int score(AgendaDb3.Event e,Remote r){
        if(e.startDate.isEmpty())return -1;
        if(e.startDate.length()<7||r.date.length()<7||!e.startDate.substring(0,7).equals(r.date.substring(0,7)))return -1;
        // If bound to a DIFFERENT live Calendar event, do not steal that entry.
        String a=key(e.title),b=key(r.title);
        boolean equal=a.equals(b)&&!a.isEmpty();
        boolean uni=isUniversity(e.title)&&isUniversity(r.title);
        boolean robo=robotLevel(e.title).equals(robotLevel(r.title))&&!robotLevel(r.title).isEmpty();
        if(uni)return 95;
        if(equal)return e.startDate.equals(r.date)?100:90;
        String oldName=norm(e.title),newName=norm(r.title);
        if(oldName.contains("convencion de estudiantes") && newName.contains("convencion de estudiantes"))return 92;
        if(oldName.startsWith("mural letrado") && newName.startsWith("mural letrado"))return 92;
        if(oldName.contains("eleccion") && oldName.contains("municip") && newName.contains("eleccion") && newName.contains("municip"))return 92;
        if(oldName.contains("informe de avance de logro a padres")
                && newName.contains("informe de logros de aprendizaje")
                && newName.contains("3") && newName.contains("4") && newName.contains("5"))return 88;
        if(oldName.contains("robot") && oldName.contains("secundaria") && oldName.contains("primaria")
                && newName.contains("robot") && newName.contains("secundaria")
                && !newName.contains("primaria"))return 86;
        if(robo && oldName.contains("robot") && newName.contains("robot"))return 88;
        return -1;
    }
    private static boolean isUniversity(String t){
        String n=norm(t);
        // Preparation tasks are separate calendar entries: never merge them
        // into the parent event, even if their titles mention the university.
        if(n.matches(".*(flyer|video publicitario|decoracion|afiche|visita al local|promocion|publicidad).*"))return false;
        return n.contains("padres")&&(n.contains("universidad")||n.contains("escuela"))
            &&(n.matches(".*\\b(ii|segunda|2)\\b.*"));
    }
    private static String robotLevel(String t){
        String n=norm(t);if(!n.contains("robot"))return "";
        if(n.contains("secundaria")&&!n.contains("primaria"))return "s";
        if(n.contains("primaria")&&!n.contains("secundaria"))return "p";
        return "";
    }
    private static String norm(String s){
        return Normalizer.normalize(s==null?"":s,Normalizer.Form.NFD)
            .replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();
    }
    private static String key(String s){return norm(s);}
    private static void mergePrivateData(SQLiteDatabase sql,long keepId,long oldId){
        if(keepId==oldId)return;
        AgendaDb3.Event keep=get(sql,keepId),old=get(sql,oldId);
        if(keep==null||old==null)return;
        ContentValues v=new ContentValues();
        if(!old.notes.trim().isEmpty()&&!keep.notes.contains(old.notes.trim())){
            v.put("notes",keep.notes.trim().isEmpty()?old.notes:keep.notes+"\n\n[APUNTES CONSERVADOS DE REGISTRO ANTERIOR]\n"+old.notes);
        }
        if(keep.driveFolderUrl.isEmpty()&&!old.driveFolderUrl.isEmpty())v.put("drive_folder_url",old.driveFolderUrl);
        if(keep.driveVideoUrl.isEmpty()&&!old.driveVideoUrl.isEmpty())v.put("drive_video_url",old.driveVideoUrl);
        if(keep.driveFotosUrl.isEmpty()&&!old.driveFotosUrl.isEmpty())v.put("drive_fotos_url",old.driveFotosUrl);
        if(v.size()>0)sql.update("events",v,"id=?",new String[]{String.valueOf(keepId)});
        try(Cursor c=sql.query("tasks",new String[]{"id","title","done"},"event_id=?",
                new String[]{String.valueOf(oldId)},null,null,null)){
            while(c.moveToNext()){
                String task=val(c,1);
                try(Cursor check=sql.query("tasks",new String[]{"id"},"event_id=? AND title=?",
                        new String[]{String.valueOf(keepId),task},null,null,null,"1")){
                    if(check.moveToFirst())continue;
                }
                ContentValues taskv=new ContentValues();taskv.put("event_id",keepId);
                taskv.put("title",task);taskv.put("done",c.getInt(2));
                sql.insert("tasks",null,taskv);
            }
        }
    }
    private static void hide(SQLiteDatabase sql,long id){
        ContentValues v=new ContentValues();v.put("sync_hidden",1);v.put("sync_enabled",0);
        sql.update("events",v,"id=?",new String[]{String.valueOf(id)});
    }

    /** Push only a user-initiated edit, NEVER an automatic bulk upload. */
    public static void pushEdited(Context ctx,AgendaDb3 db,AgendaDb3.Event e,long calId){
        if(calId<0||e==null||!e.syncEnabled||e.startDate.isEmpty())return;
        ContentResolver cr=ctx.getContentResolver();
        long remoteId=-1;
        try{remoteId=Long.parseLong(e.calendarEventId);}catch(Exception ignored){}
        if(remoteId<0){
            // Never resurrect an old seeded event just because it was edited.
            if(!"Actividad manual".equals(e.source))return;
        }
        String priorDescription="";
        if(remoteId>=0){
            Uri u=ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,remoteId);
            try(Cursor c=cr.query(u,new String[]{CalendarContract.Events.CALENDAR_ID,
                  CalendarContract.Events.DESCRIPTION},null,null,null)){
                if(c==null||!c.moveToFirst()||c.getLong(0)!=calId)return;
                priorDescription=val(c,1);
            }
        }
        ContentValues v=new ContentValues();
        v.put(CalendarContract.Events.TITLE,e.title);
        v.put(CalendarContract.Events.EVENT_LOCATION,e.place);
        v.put(CalendarContract.Events.CALENDAR_ID,calId);
        // Preserve the existing public description and all unrelated metadata.
        String desc=priorDescription.isEmpty()?e.details:stripMetadata(priorDescription);
        if(desc==null)desc="";
        desc=desc+"\n\n[365 Agenda]\nGestión: "+e.year+"\nPrioridad: "+e.priority+
            "\nEstado: "+e.status+"\nTipo: "+e.audience+"\nCategoría: "+e.category+"\n[/365 Agenda]";
        v.put(CalendarContract.Events.DESCRIPTION,desc);
        boolean all=e.time==null||e.time.trim().isEmpty();
        v.put(CalendarContract.Events.ALL_DAY,all?1:0);
        v.put(CalendarContract.Events.EVENT_TIMEZONE,all?"UTC":"America/Lima");
        if(all){
            v.put(CalendarContract.Events.DTSTART,epoch(e.startDate,"",UTC));
            String last=e.endDate.isEmpty()?e.startDate:e.endDate;
            Calendar end=Calendar.getInstance(UTC);end.setTimeInMillis(epoch(last,"",UTC));
            end.add(Calendar.DAY_OF_MONTH,1);
            v.put(CalendarContract.Events.DTEND,end.getTimeInMillis());
        }else{
            long start=epoch(e.startDate,e.time,LIMA);
            v.put(CalendarContract.Events.DTSTART,start);
            v.put(CalendarContract.Events.DTEND,start+3600000L);
        }
        if(remoteId>=0)cr.update(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,remoteId),v,null,null);
        else{
            Uri newUri=cr.insert(CalendarContract.Events.CONTENT_URI,v);
            if(newUri!=null){e.calendarEventId=String.valueOf(ContentUris.parseId(newUri));db.update(e);}
        }
    }
    private static long epoch(String iso,String hm,TimeZone tz){
        Calendar c=Calendar.getInstance(tz);c.clear();
        String[] d=iso.split("-");int hour=0,min=0;
        if(hm!=null&&!hm.isEmpty()){
            String[] x=hm.split(":");hour=Integer.parseInt(x[0]);min=x.length>1?Integer.parseInt(x[1]):0;
        }
        c.set(Integer.parseInt(d[0]),Integer.parseInt(d[1])-1,Integer.parseInt(d[2]),hour,min);
        return c.getTimeInMillis();
    }
    private static String stripMetadata(String d){
        if(d==null)return "";
        return d.replaceAll("(?s)\\[365 Agenda\\].*?\\[/365 Agenda\\]","")
            .replaceAll("(?s)\\[Mi Agenda Gestión\\].*?\\[/Mi Agenda Gestión\\]","").trim();
    }
    private static String meta(String d,String name){
        if(d==null)return "";
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?im)^\\s*"+java.util.regex.Pattern.quote(name)+"\\s*:\\s*([^\\r\\n]+)").matcher(d);
        return m.find()?m.group(1).trim().toUpperCase(Locale.ROOT):"";
    }
    private static String cat(Remote r){
        String c=r.category;
        if(c.equals("DEPORTIVO")||c.equals("ESPIRITUAL")||c.equals("PSICOLOGICO")||c.equals("ACADEMICO"))return c;
        return AgendaDb3.inferCategory(r.title);
    }
    private static String column(Cursor c,String name){
        int i=c.getColumnIndex(name);return i<0?"":val(c,i);
    }
    private static String val(Cursor c,int i){String v=c.isNull(i)?"":c.getString(i);return v==null?"":v;}
    private static String date(long millis,TimeZone zone){
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);f.setTimeZone(zone);
        return f.format(new java.util.Date(millis));
    }
}
