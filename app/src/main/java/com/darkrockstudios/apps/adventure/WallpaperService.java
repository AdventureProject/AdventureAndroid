package com.darkrockstudios.apps.adventure;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class WallpaperService extends JobService
{
	private static final String TAG = WallpaperService.class.getSimpleName();

	@Override
	public boolean onStartJob( JobParameters params )
	{
		WallpaperTask task = new WallpaperTask( this, params );
		new Thread( task ).start();

		return true;
	}

	@Override
	public boolean onStopJob( JobParameters params )
	{
		return false;
	}

	private class WallpaperTask implements Runnable
	{
		private static final String URL = "http://wethinkadventure.rocks/todayswallpaper";

		private final Context       m_context;
		private final JobParameters m_params;

		private WallpaperTask( Context context, JobParameters params )
		{
			m_context = context;
			m_params = params;
		}

		@Override
		public void run()
		{
			Bitmap bitmap = null;
			try
			{
				Gson gson = new Gson();

				InputStream jsonInput = new java.net.URL( URL ).openStream();
				String json = IOUtils.toString( jsonInput, "UTF-8" );
				Photo photo = gson.fromJson( json, Photo.class );

				Log.d( TAG, "Image URL: " + photo.image );
				InputStream input = new java.net.URL( photo.image ).openStream();
				bitmap = BitmapFactory.decodeStream( input );

				setHomeScreenWallpaper( bitmap );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}

			final boolean success = (bitmap != null);

			jobFinished( m_params, !success );
		}

		private void setHomeScreenWallpaper( Bitmap bitmap )
		{
			DisplayMetrics displayMetrics = m_context.getResources().getDisplayMetrics();

			final int maxWidth = displayMetrics.heightPixels * 2;

			final Bounds wallpaperSize = new Bounds( maxWidth, displayMetrics.heightPixels );

			final Bounds imageSize = new Bounds( bitmap.getWidth(), bitmap.getHeight() );
			final Bounds scaledSize = getScaledBounds( imageSize, wallpaperSize );

			Log.d( TAG, "Image Size: " + imageSize.m_width + " x " + imageSize.m_height );
			Log.d( TAG, "Wallpaper Size: " + wallpaperSize.m_width + " x " + wallpaperSize.m_height );
			Log.d( TAG, "Scaled Size: " + scaledSize.m_width + " x " + scaledSize.m_height );

			Bitmap scaledBitmap = Bitmap.createScaledBitmap( bitmap, (int) scaledSize.m_width, (int) scaledSize.m_height, false );

			WallpaperManager wallpaperManager = WallpaperManager.getInstance( m_context );
			try
			{
				wallpaperManager.setBitmap( scaledBitmap );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		private Bounds getScaledBounds( @NonNull final Bounds imageSize, @NonNull final Bounds boundary )
		{
			double widthRatio = boundary.m_width / imageSize.m_width;
			double heightRatio = boundary.m_height / imageSize.m_height;
			double ratio = Math.min( widthRatio, heightRatio );
			Log.d( TAG, "ratio: " + ratio );
			return new Bounds( (int) (imageSize.m_width * ratio), (int) (imageSize.m_height * ratio) );
		}
	}
}
