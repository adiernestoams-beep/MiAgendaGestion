#!/usr/bin/env python3
"""Patch the reconstructed v6 Java code without touching UI/branding."""
from pathlib import Path
import re

root=Path("app/src/main/java/pe/agenda/gestion")
db=root/"AgendaDb3.java"
act=root/"FinalActivity.java"
s=db.read_text(encoding="utf8")
s,num=re.subn(r"(public\s+static\s+final\s+int\s+DB_VERSION\s*=\s*)\d+",r"\g<1>3",s,count=1)
assert num==1,"DB_VERSION missing"
# Migrate schema in place: never drop user's database.
s,num=re.subn(r'"sync_enabled INTEGER DEFAULT 1"\s*\+', '"sync_enabled INTEGER DEFAULT 1," + "sync_hidden INTEGER NOT NULL DEFAULT 0" +',s,count=1)
assert num==1,"events CREATE TABLE schema changed; inspect before release"
needle=re.search(r'(@Override\s+public\s+void\s+onUpgrade\s*\(\s*SQLiteDatabase\s+db\s*,\s*int\s+oldVersion\s*,\s*int\s+newVersion\s*\)\s*\{)',s)
assert needle,"onUpgrade method missing"
stmt=' if(oldVersion<3){try{db.execSQL("ALTER TABLE events ADD COLUMN sync_hidden INTEGER NOT NULL DEFAULT 0");}catch(Exception ignored){}} '
s=s[:needle.end()]+stmt+s[needle.end():]
# All views/search must ignore *hidden* obsolete local copies.
needle='getReadableDatabase().query("events", null, where, args, null, null, order)'
assert needle in s,"List query changed"
s=s.replace(needle,'getReadableDatabase().query("events", null, "(sync_hidden=0) AND ("+where+")", args, null, null, order)',1)
db.write_text(s,encoding="utf8")

s=act.read_text(encoding="utf8")
def replace_method(s,method,replacement):
    name=re.search(r'\bprivate\s+(?:void|boolean|long|int)\s+'+method+r'\s*\(',s)
    assert name, "Method missing: "+method
    opening=s.index("{",name.end())
    depth=0
    for i in range(opening,len(s)):
        if s[i]=="{":depth+=1
        elif s[i]=="}":
            depth-=1
            if depth==0:
                return s[:name.start()]+replacement+s[i+1:]
    raise ValueError("Unclosed "+method)
s=replace_method(s,"findBestCalendar",
    'private long findBestCalendar(){return SafeCalendarSync.findCalendar(this);}')
s=replace_method(s,"syncCalendar",'''
    private void syncCalendar(boolean showToast){
        if(calendarSyncing)return;
        if(checkSelfPermission(Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED
                ||checkSelfPermission(Manifest.permission.WRITE_CALENDAR)!=PackageManager.PERMISSION_GRANTED){
            requestCalendarIfNeeded();return;
        }
        calendarSyncing=true;
        new Thread(()->{
            try{
                long calendarId=findBestCalendar();
                int updated=SafeCalendarSync.pull(this,db,calendarId,currentYear);
                runOnUiThread(()->{
                    renderCurrent();
                    if(showToast)Toast.makeText(this,
                        "Google Calendar recibido: "+updated+" actividades. APUNTES conservados.",
                        Toast.LENGTH_LONG).show();
                });
            }catch(Exception ex){
                final String message=ex.getMessage()==null?"No se pudo sincronizar":ex.getMessage();
                runOnUiThread(()->Toast.makeText(this,"Calendar: "+message,Toast.LENGTH_LONG).show());
            }finally{calendarSyncing=false;}
        },"calendar-safe-pull").start();
    }
''')
s=replace_method(s,"syncOneToCalendar",'''
    private void syncOneToCalendar(AgendaDb3.Event e){
        if(e==null||!e.syncEnabled||e.startDate.isEmpty())return;
        if(checkSelfPermission(Manifest.permission.WRITE_CALENDAR)!=PackageManager.PERMISSION_GRANTED)return;
        new Thread(()->{
            try{SafeCalendarSync.pushEdited(this,db,e,findBestCalendar());}
            catch(Exception ex){runOnUiThread(()->Toast.makeText(this,
                "Edición local conservada; Calendar: "+ex.getMessage(),Toast.LENGTH_LONG).show());}
        },"calendar-manual-edit").start();
    }
''')
act.write_text(s,encoding="utf8")
print("365 Agenda v7: pull-first, protected APUNTES/Drive, no bulk push, soft-hide old dates; branding untouched.")
