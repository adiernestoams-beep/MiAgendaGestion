package pe.agenda.gestion;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import java.util.Calendar;

/** Automatic one-way import. Never modifies or deletes events in Google Calendar. */
public final class AutoSyncJobService extends JobService {
    private static final int JOB_ID=365072;
    private volatile boolean stopping=false;

    public static void schedule(Context ctx) {
        try {
            if(ctx.checkSelfPermission(Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED)return;
            JobScheduler scheduler=(JobScheduler)ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if(scheduler==null)return;
            // Android runs periodic background jobs when battery restrictions permit.
            if(scheduler.getPendingJob(JOB_ID)!=null)return;
            JobInfo job=new JobInfo.Builder(JOB_ID,new ComponentName(ctx,AutoSyncJobService.class))
                    .setPeriodic(15*60*1000L).setPersisted(true).build();
            scheduler.schedule(job);
        }catch(Exception ignored){}
    }

    @Override public boolean onStartJob(JobParameters params) {
        stopping=false;
        new Thread(()->{
            try{
                if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_CALENDAR)
                        !=PackageManager.PERMISSION_GRANTED)return;
                long id=SafeCalendarSync.findCalendar(this);
                if(id<0)return;
                AgendaDb3 db=new AgendaDb3(this);
                int year=Calendar.getInstance().get(Calendar.YEAR);
                if(!stopping){
                    SafeCalendarSync.pull(this,db,id,year);
                    if(!stopping)SafeCalendarSync.pull(this,db,id,year+1);
                }
            }catch(Exception ignored){
                // Keep all local notes. A later scheduled/foreground sync retries.
            }finally{jobFinished(params,false);}
        },"365-agenda-periodic-sync").start();
        return true;
    }

    @Override public boolean onStopJob(JobParameters params) {
        stopping=true;
        return true;
    }
}