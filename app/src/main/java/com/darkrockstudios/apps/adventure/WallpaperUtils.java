package com.darkrockstudios.apps.adventure;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.File;

/**
 * Created by Adam on 11/16/2015.
 */
public final class WallpaperUtils
{
	private static final String TAG = WallpaperUtils.class.getSimpleName();

	public static final String WALLPAPER_FILE_NAME = "current_wallpaper";

	public static File getCurrentWallpaperFile( @NonNull final Context context )
	{
		final File filesDir = context.getFilesDir();
		return new File( filesDir, WallpaperUtils.WALLPAPER_FILE_NAME );
	}

	public static void setupWallpaperJob( @NonNull final Context context )
	{
		Log.d( TAG, "setupWallpaperJob" );
		final JobScheduler jobScheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
		if( jobScheduler.getAllPendingJobs().size() <= 1 )
		{
			Log.d( TAG, "Scheduling job" );
			JobInfo.Builder builder = new JobInfo.Builder( 1,
														   new ComponentName( context.getPackageName(),
																			  WallpaperService.class.getName() ) );

			final DateTime now = DateTime.now();
			final DateTime startOfTomorrow = now.withTimeAtStartOfDay().plusDays( 1 );
			final DateTime oneAmTomorrow = startOfTomorrow.plusHours( 1 );
			final Interval timeTillOneAm = new Interval( now, oneAmTomorrow );
			final long timeTillOneAmMills = timeTillOneAm.toDurationMillis();

			builder.setMinimumLatency( timeTillOneAmMills );
			builder.setRequiredNetworkType( JobInfo.NETWORK_TYPE_ANY );
			builder.setPersisted( true );

			jobScheduler.schedule( builder.build() );

			Toast.makeText( context, "Scheduling Wallpaper Service", Toast.LENGTH_SHORT ).show();
		}
	}

	public static void stopWallpaperJob( @NonNull final Context context )
	{
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
		jobScheduler.cancelAll();
	}
}
