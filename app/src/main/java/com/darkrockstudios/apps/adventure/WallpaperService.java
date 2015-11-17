package com.darkrockstudios.apps.adventure;

import android.app.job.JobParameters;
import android.app.job.JobService;

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
		return false;
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
