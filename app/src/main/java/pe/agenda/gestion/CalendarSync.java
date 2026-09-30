package pe.agenda.gestion;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarSync {
    public static final String PREFERRED_ACCOUNT="adi.ernesto.ams@gmail.com";
    public static class CalendarChoice {public long id;public String label,account;public CalendarChoice(long i,String l,String a){id=i;label=l;account=a;}@Override public String toString(){return label+(account.isEmpty()?"":" · "+account);}}

    public static List<CalendarChoice> calendars(Context ctx){
        List<CalendarChoice> out=new ArrayList<>();
        String[] p={CalendarContract.Calendars._ID,CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,CalendarContract.Calendars.ACCOUNT_NAME,CalendarContract.Calendars.ACCOUNT_TYPE,CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL};
        try(Cursor c=ctx.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,p,CalendarContract.Calendars.VISIBLE+"=1 AND "+CalendarContract.Calendars.SYNC_EVENTS+"=1",null,null)){
            if(c!=null)while(c.moveToNext()){
                int access=c.getInt(4);if(access<CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)continue;
                String label=n(c,1),acc=n(c,2),type=n(c,3);CalendarChoice cc=new CalendarChoice(c.getLong(0),label.isEmpty()?"Calendario":label,acc);
                if(PREFERRED_ACCOUNT.equalsIgnoreCase(acc))out.add(0,cc);else if("com.google".equals(type))out.add(Math.min(1,out.size()),cc);else out.add(cc);
            }
        }catch(SecurityException ignored){}
        return out;
    }
    public static CalendarChoice preferredCalendar(Context ctx){for(CalendarChoice c:calendars(ctx))if(PREFERRED_ACCOUNT.equalsIgnoreCase(c.account))return c;return null;}

    public static int syncYear(Context ctx,DB db,int year,long calendarId)throws Exception{
        long now=System.currentTimeMillis();int count=0;
        count+=pullMarkedEvents(ctx,db,year,calendarId,now);
        for(EventItem e:db.listYear(year)){
            if(!e.syncEnabled||e.startDate==null||e.startDate.isEmpty())continue;
            if(e.magUid==null||e.magUid.isEmpty())e.magUid=DB.uidFor(e.managementYear,e.startDate,e.title);
            if(e.calendarEventId==null){long eid=insert(ctx,e,calendarId);db.markSynced(e.id,eid,calendarId,now);count++;}
            else{
                Uri u=ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,e.calendarEventId);
                try(Cursor c=ctx.getContentResolver().query(u,new String[]{CalendarContract.Events._ID},null,null,null)){
                    if(c==null||!c.moveToFirst()){long eid=insert(ctx,e,calendarId);db.markSynced(e.id,eid,calendarId,now);count++;}
                    else if(e.modifiedAt>e.lastSyncedAt){update(ctx,e,calendarId);db.markSynced(e.id,e.calendarEventId,calendarId,now);count++;}
                }
            }
        }
        return count;
    }

    private static int pullMarkedEvents(Context ctx,DB db,int year,long calendarId,long now){
        int count=0;Calendar start=Calendar.getInstance();start.clear();start.set(year,0,1,0,0,0);Calendar end=Calendar.getInstance();end.clear();end.set(year+1,0,1,0,0,0);
        String sel=CalendarContract.Events.CALENDAR_ID+"=? AND "+CalendarContract.Events.DTSTART+">=? AND "+CalendarContract.Events.DTSTART+"<? AND "+CalendarContract.Events.DELETED+"=0";
        String[] args={String.valueOf(calendarId),String.valueOf(start.getTimeInMillis()),String.valueOf(end.getTimeInMillis())};
        String[] pr={CalendarContract.Events._ID,CalendarContract.Events.TITLE,CalendarContract.Events.DESCRIPTION,CalendarContract.Events.DTSTART,CalendarContract.Events.DTEND,CalendarContract.Events.ALL_DAY,CalendarContract.Events.EVENT_LOCATION};
        try(Cursor c=ctx.getContentResolver().query(CalendarContract.Events.CONTENT_URI,pr,sel,args,CalendarContract.Events.DTSTART+" ASC")){
            if(c==null)return 0;
            while(c.moveToNext()){
                long eid=c.getLong(0);String desc=n(c,2);if(!isManaged(desc))continue;
                EventItem remote=fromCalendarRow(c,year);Meta m=parseMeta(desc);remote.magUid=m.uid.isEmpty()?DB.uidFor(year,remote.startDate,remote.title):m.uid;remote.category=m.category.isEmpty()?DB.inferCategory(remote.title,remote.details):m.category;remote.priority=m.priority.isEmpty()?"SE_ACERCA":m.priority;remote.status=m.status.isEmpty()?"PENDIENTE":m.status;remote.prepStatus=m.prep.isEmpty()?"PENDIENTE":m.prep;remote.responsible=m.responsible;remote.syncEnabled=true;remote.source="Google Calendar / ChatGPT";
                EventItem local=db.getByUid(remote.magUid);if(local==null)local=db.getByCalendarEventId(eid);
                if(local!=null&&local.modifiedAt>local.lastSyncedAt)continue;
                db.upsertFromCalendar(remote,eid,calendarId,now);count++;
            }
        }catch(Exception ignored){}
        return count;
    }

    private static EventItem fromCalendarRow(Cursor c,int year){
        EventItem e=new EventItem();e.managementYear=year;e.title=n(c,1);String desc=n(c,2);e.place=n(c,6);long start=c.getLong(3),end=c.isNull(4)?start:c.getLong(4);boolean all=c.getInt(5)==1;SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
        if(all){df.setTimeZone(TimeZone.getTimeZone("UTC"));e.startDate=df.format(new Date(start));Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));cal.setTimeInMillis(end);cal.add(Calendar.DATE,-1);e.endDate=df.format(cal.getTime());e.time="";}
        else{df.setTimeZone(TimeZone.getDefault());e.startDate=df.format(new Date(start));e.endDate=df.format(new Date(end));e.time=new SimpleDateFormat("HH:mm",Locale.US).format(new Date(start));}
        e.details=stripMeta(desc);return e;
    }

    private static boolean isManaged(String d){return d!=null&&(d.contains("[365 Agenda]")||d.contains("[Mi Agenda Gestión]"));}
    private static String stripMeta(String d){if(d==null)return "";int a=d.indexOf("\n\n[365 Agenda]");if(a<0)a=d.indexOf("\n\n[Mi Agenda Gestión]");if(a<0)a=d.indexOf("[365 Agenda]");if(a<0)a=d.indexOf("[Mi Agenda Gestión]");return a>=0?d.substring(0,a).trim():d.trim();}
    private static class Meta{String uid="",category="",priority="",status="",prep="",responsible="";}
    private static Meta parseMeta(String d){Meta m=new Meta();if(d==null)return m;for(String line:d.split("\\r?\\n")){String x=line.trim();int k=x.indexOf(':');if(k<0)continue;String key=x.substring(0,k).trim().toLowerCase(Locale.ROOT),val=x.substring(k+1).trim();if(key.equals("uid"))m.uid=val;else if(key.startsWith("categoria")||key.startsWith("categoría"))m.category=val.toUpperCase(Locale.ROOT);else if(key.startsWith("prioridad"))m.priority=val.toUpperCase(Locale.ROOT);else if(key.startsWith("estado"))m.status=val.toUpperCase(Locale.ROOT).replace(' ','_');else if(key.startsWith("prepar"))m.prep=val.toUpperCase(Locale.ROOT).replace(' ','_');else if(key.startsWith("responsable"))m.responsible=val;}return m;}

    private static long insert(Context ctx,EventItem e,long calId){ContentValues v=values(e,calId);Uri u=ctx.getContentResolver().insert(CalendarContract.Events.CONTENT_URI,v);if(u==null)throw new IllegalStateException("No se pudo crear el evento en Google Calendar");long id=Long.parseLong(u.getLastPathSegment());syncReminder(ctx,id,e.reminderMinutes);return id;}
    private static void update(Context ctx,EventItem e,long calId){ctx.getContentResolver().update(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,e.calendarEventId),values(e,calId),null,null);syncReminder(ctx,e.calendarEventId,e.reminderMinutes);}
    private static ContentValues values(EventItem e,long calId){
        ContentValues v=new ContentValues();v.put(CalendarContract.Events.CALENDAR_ID,calId);v.put(CalendarContract.Events.TITLE,e.title);v.put(CalendarContract.Events.EVENT_LOCATION,e.place);
        StringBuilder d=new StringBuilder();if(e.details!=null&&!e.details.trim().isEmpty())d.append(e.details.trim());if(e.notes!=null&&!e.notes.trim().isEmpty())d.append(d.length()>0?"\n\n":"").append("Notas: ").append(e.notes.trim());d.append("\n\n[365 Agenda]\nUID: ").append(e.magUid).append("\nGestion: ").append(e.managementYear).append("\nCategoria: ").append(e.category).append("\nPrioridad: ").append(e.priority).append("\nEstado: ").append(e.computedStatus()).append("\nPreparacion: ").append(e.prepStatus).append("\nResponsable: ").append(e.responsible==null?"":e.responsible).append("\n[/365 Agenda]");v.put(CalendarContract.Events.DESCRIPTION,d.toString());
        if(e.time==null||e.time.isEmpty()){v.put(CalendarContract.Events.ALL_DAY,1);v.put(CalendarContract.Events.DTSTART,utcDay(e.startDate));String end=e.endDate==null||e.endDate.isEmpty()?e.startDate:e.endDate;Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC"));c.setTimeInMillis(utcDay(end));c.add(Calendar.DATE,1);v.put(CalendarContract.Events.DTEND,c.getTimeInMillis());v.put(CalendarContract.Events.EVENT_TIMEZONE,"UTC");}
        else{v.put(CalendarContract.Events.ALL_DAY,0);long s=localTime(e.startDate,e.time);v.put(CalendarContract.Events.DTSTART,s);String end=e.endDate==null||e.endDate.isEmpty()?e.startDate:e.endDate;long ee=localTime(end,e.time)+60*60*1000L;v.put(CalendarContract.Events.DTEND,ee);v.put(CalendarContract.Events.EVENT_TIMEZONE,TimeZone.getDefault().getID());}
        return v;
    }
    private static long utcDay(String iso){String[] p=iso.split("-");Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC"));c.clear();c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]),0,0,0);return c.getTimeInMillis();}
    private static long localTime(String iso,String hm){String[] p=iso.split("-");String[] t=hm.split(":");Calendar c=Calendar.getInstance();c.clear();c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]),Integer.parseInt(t[0]),Integer.parseInt(t[1]),0);return c.getTimeInMillis();}
    private static void syncReminder(Context ctx,long eventId,int minutes){try{ctx.getContentResolver().delete(CalendarContract.Reminders.CONTENT_URI,CalendarContract.Reminders.EVENT_ID+"=?",new String[]{String.valueOf(eventId)});if(minutes>=0){ContentValues r=new ContentValues();r.put(CalendarContract.Reminders.EVENT_ID,eventId);r.put(CalendarContract.Reminders.MINUTES,minutes);r.put(CalendarContract.Reminders.METHOD,CalendarContract.Reminders.METHOD_ALERT);ctx.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI,r);}}catch(Exception ignored){}}
    private static String n(Cursor c,int i){String s=c.getString(i);return s==null?"":s;}
}
