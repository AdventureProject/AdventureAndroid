package com.darkrockstudios.apps.adventure;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
		private final JobParameters    m_params;

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

			final Photo photo = getPhoto();
			final Bitmap bitmap = getBitmap();

			final boolean success = (bitmap != null);
			Log.d( TAG, "Wallpaper success: " + success );

			if( success )
			{
				// Reschedule us at the correct time
				WallpaperUtils.setupWallpaperJob( m_service );

				postSuccessNotification( photo, bitmap );
			}
			else
			{
				postFailureNotification();
			}

			m_service.jobFinished( m_params, !success );
		}

		private void postSuccessNotification( Photo photo, Bitmap bitmap )
		{
			Intent intent = new Intent( m_service, MainActivity.class );
			PendingIntent pendingIntent =
					PendingIntent.getActivity(
							m_service,
							0,
							intent,
							PendingIntent.FLAG_UPDATE_CURRENT
					);

			Bitmap largeIcon = BitmapFactory.decodeResource( m_service.getResources(),
			                                                 R.drawable.ic_notification_large );

			Notification notification = new Notification.Builder( m_service )
					                            .setContentTitle(
							                            m_service.getString( R.string.notification_success_wallpaper_title ) )
					                            .setContentText( photo.title )
					                            .setSmallIcon( R.drawable.ic_notification )
					                            .setLargeIcon( largeIcon )
					                            .setContentIntent( pendingIntent )
					                            .setStyle( new Notification.BigPictureStyle()
							                                       .bigPicture( bitmap ) )
					                            .setAutoCancel( true )
					                            .build();

			NotificationManager notificationManager =
					(NotificationManager) m_service.getSystemService( NOTIFICATION_SERVICE );

			notificationManager.notify( 0, notification );
		}

		private void postFailureNotification()
		{
			Bitmap largeIcon = BitmapFactory.decodeResource( m_service.getResources(),
			                                                 R.drawable.ic_notification_large );

			Notification notification = new Notification.Builder( m_service )
					                            .setContentTitle(
							                            m_service.getString( R.string.notification_failure_wallpaper_title ) )
					                            .setContentText(
							                            m_service.getString( R.string.notification_failure_wallpaper_message ) )
					                            .setSmallIcon( R.drawable.ic_notification )
					                            .setLargeIcon( largeIcon )
					                            .setAutoCancel( true ).build();


			NotificationManager notificationManager =
					(NotificationManager) m_service.getSystemService( NOTIFICATION_SERVICE );

			notificationManager.notify( 0, notification );
		}
	}
}
