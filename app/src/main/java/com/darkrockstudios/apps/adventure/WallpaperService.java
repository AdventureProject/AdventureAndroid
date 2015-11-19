package com.darkrockstudios.apps.adventure;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

import org.joda.time.DateTimeConstants;

public class WallpaperService extends JobService
{
	@Override
	public boolean onStartJob( JobParameters params )
	{
		WallpaperTask task = new WallpaperServiceTask( this, params );
		new Thread( task ).start();

		return true;
	}

	@Override
	public boolean onStopJob( JobParameters params )
	{
		final JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );

		// If this was the setup job, now create our real job
		if( params.getExtras().getBoolean( WallpaperUtils.JOB_EXTRA_IS_SETUP, false ) )
		{
			JobInfo.Builder builder = new JobInfo.Builder( 1,
														   new ComponentName( getPackageName(),
																			  WallpaperService.class.getName() ) );
			builder.setPeriodic( DateTimeConstants.MILLIS_PER_DAY );
			builder.setPersisted( true );

			PersistableBundle extras = new PersistableBundle();
			extras.putBoolean( WallpaperUtils.JOB_EXTRA_IS_SETUP, false );
			builder.setExtras( extras );

			jobScheduler.schedule( builder.build() );
		}

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

			final boolean success = (getBitmap() != null);
			m_service.jobFinished( m_params, !success );
		}
	}
}
