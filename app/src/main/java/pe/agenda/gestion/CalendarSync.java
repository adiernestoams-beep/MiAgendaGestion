package pe.agenda.gestion;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarSync {
    public static class CalendarChoice { public long id; public String label, account; public CalendarChoice(long i,String l,String a){id=i;label=l;account=a;} @Override public String toString(){return label+(account.isEmpty()?"":" · "+account);} }

    public static List<CalendarChoice> calendars(Context ctx){
        List<CalendarChoice> out=new ArrayList<>();
        String[] p={CalendarContract.Calendars._ID,CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,CalendarContract.Calendars.ACCOUNT_NAME,CalendarContract.Calendars.ACCOUNT_TYPE,CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL};
        try(Cursor c=ctx.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,p,CalendarContract.Calendars.VISIBLE+"=1 AND "+CalendarContract.Calendars.SYNC_EVENTS+"=1",null,null)){
            if(c!=null)while(c.moveToNext()){int access=c.getInt(4);if(access>=CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR){String label=c.getString(1),acc=c.getString(2),type=c.getString(3);CalendarChoice cc=new CalendarChoice(c.getLong(0),label==null?"Calendario":label,acc==null?"":acc);if("com.google".equals(type))out.add(0,cc);else out.add(cc);}}
        }catch(SecurityException ignored){}
        return out;
    }

    public static int syncYear(Context ctx, DB db, int year, long calendarId) throws Exception {
        int count=0; long now=System.currentTimeMillis();
        for(EventItem e:db.listYear(year)){
            if(!e.syncEnabled || e.startDate.isEmpty())continue;
            if(e.calendarEventId==null){long eid=insert(ctx,e,calendarId);db.markSynced(e.id,eid,calendarId,now);count++;}
            else {
                Cursor c=ctx.getContentResolver().query(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,e.calendarEventId),projection(),null,null,null);
                if(c==null || !c.moveToFirst()){if(c!=null)c.close();long eid=insert(ctx,e,calendarId);db.markSynced(e.id,eid,calendarId,now);count++;continue;}
                if(e.modifiedAt<=e.lastSyncedAt){pull(db,e,c,now);count++;}
                else {c.close();update(ctx,e,calendarId);db.markSynced(e.id,e.calendarEventId,calendarId,now);count++;}
                if(!c.isClosed())c.close();
            }
        }
        return count;
    }
    private static String[] projection(){return new String[]{CalendarContract.Events.TITLE,CalendarContract.Events.DESCRIPTION,CalendarContract.Events.DTSTART,CalendarContract.Events.DTEND,CalendarContract.Events.ALL_DAY,CalendarContract.Events.EVENT_LOCATION};}
    private static void pull(DB db,EventItem e,Cursor c,long now){
        String title=n(c,0),desc=n(c,1),place=n(c,5);long start=c.getLong(2);long end=c.isNull(3)?start:c.getLong(3);boolean all=c.getInt(4)==1;String sd,ed,time="";SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
        if(all){df.setTimeZone(TimeZone.getTimeZone("UTC"));sd=df.format(new Date(start));Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));cal.setTimeInMillis(end);cal.add(Calendar.DATE,-1);ed=df.format(cal.getTime());}
        else {df.setTimeZone(TimeZone.getDefault());sd=df.format(new Date(start));ed=df.format(new Date(end));time=new SimpleDateFormat("HH:mm",Locale.US).format(new Date(start));}
        int marker=desc.indexOf("\n\n[Mi Agenda Gestión]");if(marker>=0)desc=desc.substring(0,marker);
        db.updateFromCalendar(e.id,title,desc,sd,ed,time,place,now);
    }
    private static String n(Cursor c,int i){String s=c.getString(i);return s==null?"":s;}
    private static long insert(Context ctx,EventItem e,long calId){ContentValues v=values(e,calId);Uri u=ctx.getContentResolver().insert(CalendarContract.Events.CONTENT_URI,v);if(u==null)throw new IllegalStateException("No se pudo crear el evento en Calendar");long id=Long.parseLong(u.getLastPathSegment());syncReminder(ctx,id,e.reminderMinutes);return id;}
    private static void update(Context ctx,EventItem e,long calId){ctx.getContentResolver().update(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,e.calendarEventId),values(e,calId),null,null);syncReminder(ctx,e.calendarEventId,e.reminderMinutes);}
    private static ContentValues values(EventItem e,long calId){ContentValues v=new ContentValues();v.put(CalendarContract.Events.CALENDAR_ID,calId);v.put(CalendarContract.Events.TITLE,e.title);String desc=e.details+(e.notes.isEmpty()?"":"\n\nNotas: "+e.notes)+"\n\n[Mi Agenda Gestión]\nPrioridad: "+e.priority+"\nEstado: "+e.status+"\nPreparación: "+e.prepStatus;v.put(CalendarContract.Events.DESCRIPTION,desc);v.put(CalendarContract.Events.EVENT_LOCATION,e.place);
        if(e.time==null||e.time.isEmpty()){v.put(CalendarContract.Events.ALL_DAY,1);v.put(CalendarContract.Events.DTSTART,utcDay(e.startDate));String end=e.endDate==null||e.endDate.isEmpty()?e.startDate:e.endDate;Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC"));c.setTimeInMillis(utcDay(end));c.add(Calendar.DATE,1);v.put(CalendarContract.Events.DTEND,c.getTimeInMillis());v.put(CalendarContract.Events.EVENT_TIMEZONE,"UTC");}
        else {v.put(CalendarContract.Events.ALL_DAY,0);long s=localTime(e.startDate,e.time);v.put(CalendarContract.Events.DTSTART,s);String end=e.endDate==null||e.endDate.isEmpty()?e.startDate:e.endDate;long ee=localTime(end,e.time)+60*60*1000L;v.put(CalendarContract.Events.DTEND,ee);v.put(CalendarContract.Events.EVENT_TIMEZONE,TimeZone.getDefault().getID());}
        return v;
    }
    private static long utcDay(String iso){String[] p=iso.split("-");Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC"));c.clear();c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]),0,0,0);return c.getTimeInMillis();}
    private static long localTime(String iso,String hm){String[] p=iso.split("-");String[] t=hm.split(":");Calendar c=Calendar.getInstance();c.clear();c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]),Integer.parseInt(t[0]),Integer.parseInt(t[1]),0);return c.getTimeInMillis();}
    private static void syncReminder(Context ctx,long eventId,int minutes){try{ctx.getContentResolver().delete(CalendarContract.Reminders.CONTENT_URI,CalendarContract.Reminders.EVENT_ID+"=?",new String[]{String.valueOf(eventId)});if(minutes>=0){ContentValues r=new ContentValues();r.put(CalendarContract.Reminders.EVENT_ID,eventId);r.put(CalendarContract.Reminders.MINUTES,minutes);r.put(CalendarContract.Reminders.METHOD,CalendarContract.Reminders.METHOD_ALERT);ctx.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI,r);}}catch(Exception ignored){}}
}
