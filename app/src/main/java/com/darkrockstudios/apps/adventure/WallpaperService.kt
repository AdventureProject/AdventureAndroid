package com.darkrockstudios.apps.adventure

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

class WallpaperService : JobService()
{

	override fun onStartJob(params: JobParameters): Boolean
	{
		Log.d(TAG, "Starting Wallpaper Job...")
		val task = WallpaperServiceTask(this, params)
		Thread(task).start()

		return true
	}

	override fun onStopJob(params: JobParameters): Boolean
	{
		Log.d(TAG, "Stopping Wallpaper Job!")
		return true
	}

	private class WallpaperServiceTask constructor(private val m_service: WallpaperService,
	                                               private val m_params: JobParameters) : WallpaperTask(m_service,
	                                                                                                    WallpaperTask.Companion.URL_TODAY)
	{
		override fun run()
		{
			super.run()

			Log.d(TAG, "WallpaperServiceTask::run()")

			val photo = photo
			val bitmap = bitmap

			val success = bitmap != null && photo != null

			if (bitmap != null && photo != null)
			{
				Log.d(TAG, "Wallpaper success!")

				// Reschedule us at the correct time
				WallpaperUtils.setupWallpaperJob(m_service)

				postSuccessNotification(photo, bitmap)
			}
			else
			{
				postFailureNotification()
			}

			m_service.jobFinished(m_params, !success)
		}

		private fun postSuccessNotification(photo: Photo, bitmap: Bitmap)
		{
			val intent = Intent(m_service, MainActivity::class.java)
			val pendingIntent = PendingIntent.getActivity(
					m_service,
					0,
					intent,
					PendingIntent.FLAG_UPDATE_CURRENT
			)

			val largeIcon = BitmapFactory.decodeResource(m_service.resources,
			                                             R.drawable.ic_notification_large)

			val notification = Notification.Builder(m_service)
					.setContentTitle(
							m_service.getString(R.string.notification_success_wallpaper_title))
					.setContentText(photo.title)
					.setSmallIcon(R.drawable.ic_notification)
					.setLargeIcon(largeIcon)
					.setContentIntent(pendingIntent)
					.setStyle(Notification.BigPictureStyle()
							          .bigPicture(bitmap))
					.setAutoCancel(true)
					.build()

			val notificationManager = m_service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

			notificationManager.notify(0, notification)
		}

		private fun postFailureNotification()
		{
			val largeIcon = BitmapFactory.decodeResource(m_service.resources,
			                                             R.drawable.ic_notification_large)

			val notification = Notification.Builder(m_service)
					.setContentTitle(
							m_service.getString(R.string.notification_failure_wallpaper_title))
					.setContentText(
							m_service.getString(R.string.notification_failure_wallpaper_message))
					.setSmallIcon(R.drawable.ic_notification)
					.setLargeIcon(largeIcon)
					.setAutoCancel(true).build()


			val notificationManager = m_service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

			notificationManager.notify(0, notification)
		}
	}

	companion object
	{
		private val TAG = WallpaperService::class.java.simpleName
	}
}
