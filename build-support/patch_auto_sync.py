from pathlib import Path
import re
root=Path('app/src/main/java/pe/agenda/gestion')
safe=root/'SafeCalendarSync.java'
s=safe.read_text()
def sub(old,new):
    global s
    assert old in s,'SafeSync anchor missing: '+old[:65]
    s=s.replace(old,new,1)
sub('if(remote.isEmpty())throw new IllegalStateException("Google Calendar aún no descargó eventos. Sincronízalo en Android y vuelve a pulsar ↻ Calendar.");',
    'if(remote.isEmpty())return 0; // Never delete local data when Google is not downloaded.')
sub('        Set<Long> seen=new HashSet<>();\n        int updated=0;',
    '        Set<Long> seen=new HashSet<>();\n        Set<String> activeIds=new HashSet<>();\n        for(Remote item:remote)activeIds.add(String.valueOf(item.id));\n        int updated=0;')
sub('''            for(AgendaDb3.Event other:findEquivalentLocal(sql,r,year,local.id)){
                mergePrivateData(sql,local.id,other.id);
                hide(sql,other.id);
                seen.add(other.id);
            }''',
'''            for(AgendaDb3.Event other:findEquivalentLocal(sql,r,year,local.id)){
                // Similar titles do NOT imply that two live Google entries are duplicates.
                if(!other.calendarEventId.isEmpty() && activeIds.contains(other.calendarEventId))continue;
                mergePrivateData(sql,local.id,other.id);
                hide(sql,other.id);
                seen.add(other.id);
            }''')
sub('''        // Deleted or moved calendar records must never be re-uploaded from the
        // tablet. Soft-hide them so APUNTES and subtasks remain recoverable.
        try(Cursor c=sql.query("events",new String[]{"id","calendar_event_id"},
                "year=? AND calendar_event_id<>'' AND sync_hidden=0",
                new String[]{String.valueOf(year)},null,null,null)){
            List<Long> missing=new ArrayList<>();
            while(c.moveToNext())if(!seen.contains(c.getLong(0)))missing.add(c.getLong(0));
            for(Long id:missing)hide(sql,id);
        }
        return updated;''',
'''        // Avoid deleting visible events during partial Android calendar download.
        // Require two identical snapshots at least 10 seconds apart.
        List<String> ids=new ArrayList<>(activeIds);
        java.util.Collections.sort(ids);
        String snapshot=android.text.TextUtils.join(",",ids);
        android.content.SharedPreferences prefs=ctx.getSharedPreferences("365_google_mirror",Context.MODE_PRIVATE);
        String key="snapshot_"+year;
        long last=prefs.getLong(key+"_at",0);
        boolean stable=snapshot.equals(prefs.getString(key,"")) && System.currentTimeMillis()-last>=10000L;
        prefs.edit().putString(key,snapshot).putLong(key+"_at",System.currentTimeMillis()).apply();
        if(stable){
            try(Cursor c=sql.query("events",new String[]{"id","calendar_event_id","source"},
                    "year=? AND sync_hidden=0",new String[]{String.valueOf(year)},null,null,null)){
                List<Long> obsolete=new ArrayList<>();
                while(c.moveToNext()){
                    long id=c.getLong(0);
                    if(seen.contains(id))continue;
                    String linked=val(c,1),source=val(c,2);
                    // Hand-made local-only entries are retained until the user publishes them.
                    if(linked.isEmpty() && source.equalsIgnoreCase("Actividad manual"))continue;
                    obsolete.add(id);
                }
                for(Long id:obsolete)hide(sql,id); // APUNTES and subtasks stay recoverable.
            }
        }
        return updated;''')
sub('''        if(e.startDate.length()<7||r.date.length()<7||!e.startDate.substring(0,7).equals(r.date.substring(0,7)))return -1;''',
'''        if(e.startDate.length()<7||r.date.length()<7)return -1;
        if(!e.startDate.substring(0,4).equals(r.date.substring(0,4)))return -1;
        boolean sameMonth=e.startDate.substring(0,7).equals(r.date.substring(0,7));
        if(!sameMonth && norm(e.title).contains("reunion mensual"))return -1;''')
sub('''        if(uni)return 95;
        if(equal)return e.startDate.equals(r.date)?100:90;''',
'''        if(uni)return 95;
        if(equal)return e.startDate.equals(r.date)?100:(sameMonth?90:82);
        if(!sameMonth)return -1;''')
safe.write_text(s)

db=root/'AgendaDb3.java'
s=db.read_text()
assert 'DB_VERSION = 3' in s
db.write_text(s.replace('DB_VERSION = 3','DB_VERSION = 4',1))

act=root/'FinalActivity.java'
s=act.read_text()
assert '        db.ensureOfficialOctober2026();' in s
s=s.replace('        db.ensureOfficialOctober2026();\n','',1)
assert '        buildUi();\n        requestCalendarIfNeeded();' in s
s=s.replace('        buildUi();\n        requestCalendarIfNeeded();',
'''        buildUi();
        AutoSyncJobService.schedule(this);
        requestCalendarIfNeeded();''',1)
start=s.index('    private void syncOneToCalendar(AgendaDb3.Event e){')
opening=s.index('{',start)
depth=0
for end in range(opening,len(s)):
    if s[end]=='{':depth+=1
    elif s[end]=='}':
        depth-=1
        if depth==0:break
else:raise ValueError('syncOneToCalendar unbalanced')
s=s[:start]+'''    private void syncOneToCalendar(AgendaDb3.Event e){
        // One-way sync: only ChatGPT/Google Calendar edits modify the public agenda.
        // APUNTES, subtasks and Drive links remain private in the tablet.
    }'''+s[end+1:]
needle='    @Override protected void onSaveInstanceState(Bundle out) {'
assert needle in s
s=s.replace(needle,'''    private final android.os.Handler autoHandler=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable onCalendarUpdate=()->syncCalendar(false);
    private final android.database.ContentObserver observer=new android.database.ContentObserver(
            new android.os.Handler(android.os.Looper.getMainLooper())){
        @Override public void onChange(boolean selfChange){
            autoHandler.removeCallbacks(onCalendarUpdate);
            autoHandler.postDelayed(onCalendarUpdate,1800);
        }
    };
    private boolean observing=false;
    @Override protected void onResume(){
        super.onResume();
        AutoSyncJobService.schedule(this);
        try{
            if(!observing && checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED){
                getContentResolver().registerContentObserver(CalendarContract.Events.CONTENT_URI,true,observer);
                observing=true;
                autoHandler.postDelayed(onCalendarUpdate,1000);
            }
        }catch(Exception ignored){}
    }
    @Override protected void onPause(){
        autoHandler.removeCallbacks(onCalendarUpdate);
        if(observing){try{getContentResolver().unregisterContentObserver(observer);}catch(Exception ignored){}observing=false;}
        super.onPause();
    }
'''+needle,1)
act.write_text(s)
print('365 Agenda v8: automatic pull, preserved private data, no remote bulk push')