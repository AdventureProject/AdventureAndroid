package com.darkrockstudios.apps.adventure;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

import org.joda.time.DateTimeConstants;

public class WallpaperService extends JobService
{
	private static final String TAG = WallpaperService.class.getSimpleName();

	@Override
	public boolean onStartJob( JobParameters params )
	{
		Log.d( TAG, "Starting Wallpaper Job..." );
		WallpaperTask task = new WallpaperServiceTask( this, params );
		new Thread( task ).start();

		return true;
	}

	@Override
	public boolean onStopJob( JobParameters params )
	{
		Log.d( TAG, "Stopping Wallpaper Job!" );
		return true;
	}

	private static class WallpaperServiceTask extends WallpaperTask
	{
		private final WallpaperService m_service;
		private final JobParameters m_params;

		private WallpaperServiceTask( WallpaperService context, JobParameters params )
		{
			super( context, URL_TODAY );
			m_params = params;
			m_service = context;
		}

		@Override
		public void run()
		{
			super.run();

			final JobScheduler jobScheduler = (JobScheduler) m_service.getSystemService( Context.JOB_SCHEDULER_SERVICE );

			Log.d( TAG, "WallpaperServiceTask::run()" );
			// If this was the setup job, now create our real job
			if( m_params.getExtras().getBoolean( WallpaperUtils.JOB_EXTRA_IS_SETUP, false ) )
			{
				Log.d( TAG, "Is setup job, schedule the real one" );
				JobInfo.Builder builder = new JobInfo.Builder( 1,
				                                               new ComponentName( m_service.getPackageName(),
				                                                                  WallpaperService.class.getName() ) );
				builder.setPeriodic( DateTimeConstants.MILLIS_PER_DAY );
				builder.setRequiresDeviceIdle( true );
				builder.setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY );
				builder.setPersisted( true );

				PersistableBundle extras = new PersistableBundle();
				extras.putBoolean( WallpaperUtils.JOB_EXTRA_IS_SETUP, false );
				builder.setExtras( extras );

				jobScheduler.schedule( builder.build() );
			}

			final boolean success = (getBitmap() != null);
			Log.d( TAG, "Wallpaper success: " + success );
			m_service.jobFinished( m_params, !success );
		}
	}
}
