package com.darkrockstudios.apps.adventure;

import android.app.Application;
import android.app.job.JobScheduler;
import android.content.Context;

/**
 * Created by Adam on 11/16/2015.
 */
public class App extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();

		final JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
		if( jobScheduler.getAllPendingJobs().size() == 0 )
		{
			WallpaperUtils.setupWallpaperJob( this );
		}
	}
}
