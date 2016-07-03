package com.darkrockstudios.apps.adventure;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

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

			Log.d( TAG, "WallpaperServiceTask::run()" );

			// Reschedule us at the correct time
			WallpaperUtils.setupWallpaperJob( m_service );

			final boolean success = (getBitmap() != null);
			Log.d( TAG, "Wallpaper success: " + success );

			if( success )
			{
				postSuccessNotification();
			}
			else
			{
				postFailureNotification();
			}

			m_service.jobFinished( m_params, !success );
		}

		private void postSuccessNotification()
		{
			Notification notification = new Notification.Builder( m_service )
					                 .setContentTitle( "New wallpaper set" )
					                 .setContentText( "So cool!" )
					                 .setSmallIcon( R.mipmap.ic_launcher )
					                 .setAutoCancel( true ).build();


			NotificationManager notificationManager =
					(NotificationManager) m_service.getSystemService( NOTIFICATION_SERVICE );

			notificationManager.notify( 0, notification );
		}

		private void postFailureNotification()
		{
			Notification notification = new Notification.Builder( m_service )
					                 .setContentTitle( "Wallpaper failure" )
					                 .setContentText( "Failed to set the wallpaper for some reason" )
					                 .setSmallIcon( R.mipmap.ic_launcher )
					                 .setAutoCancel( true ).build();


			NotificationManager notificationManager =
					(NotificationManager) m_service.getSystemService( NOTIFICATION_SERVICE );

			notificationManager.notify( 0, notification );
		}
	}
}
