package com.darkrockstudios.apps.adventure;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;

/**
 * Created by Adam on 11/16/2015.
 */
public final class WallpaperUtils
{
	public static final String WALLPAPER_FILE_NAME = "current_wallpaper";

	public static File getCurrentWallpaperFile( @NonNull final Context context )
	{
		final File filesDir = context.getFilesDir();
		return new File( filesDir, WallpaperUtils.WALLPAPER_FILE_NAME );
	}

	public static void setupWallpaperJob( @NonNull final Context context )
	{
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
		if( jobScheduler.getAllPendingJobs().size() == 0 )
		{
			JobInfo.Builder builder = new JobInfo.Builder( 1,
														   new ComponentName( context.getPackageName(),
																			  WallpaperService.class.getName() ) );

			builder.setPeriodic( 12 * 60 * 60 * 1000 );
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

	public static abstract class DownloadImage extends AsyncTask<String, Void, Bitmap>
	{
		private static final String TAG = DownloadImage.class.getSimpleName();

		private final Gson gson = new Gson();

		@Override
		protected Bitmap doInBackground( String... URL )
		{
			String imageURL = URL[ 0 ];
			Log.d( TAG, "URL: " + imageURL );
			Bitmap bitmap = null;
			try
			{
				InputStream jsonInput = new java.net.URL( imageURL ).openStream();
				String json = IOUtils.toString( jsonInput, "UTF-8" );
				Photo photo = gson.fromJson( json, Photo.class );

				Log.d( TAG, "Image URL: " + photo.image );
				InputStream input = new java.net.URL( photo.image ).openStream();
				bitmap = BitmapFactory.decodeStream( input );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected abstract void onPostExecute( Bitmap result );
	}
}
