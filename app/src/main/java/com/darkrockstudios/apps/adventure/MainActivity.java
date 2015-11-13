package com.darkrockstudios.apps.adventure;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private Gson gson = new Gson();

	private ImageView      m_imageView;
	private ProgressDialog m_progressDialog;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		FloatingActionButton fab = (FloatingActionButton) findViewById( R.id.fab );
		fab.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				// Execute DownloadImage AsyncTask
				new DownloadImage().execute( "http://wethinkadventure.rocks/random" );
				Snackbar.make( view, "Fetching new Wallpaper", Snackbar.LENGTH_LONG )
						.setAction( "Action", null ).show();
			}
		} );

		FloatingActionButton fab2 = (FloatingActionButton) findViewById( R.id.fab2 );
		fab2.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				stopWallpaperJob();
				Snackbar.make( view, "Stopping Wallpaper Job", Snackbar.LENGTH_LONG )
						.setAction( "Restart", new View.OnClickListener()
						{
							@Override
							public void onClick( View v )
							{
								setupWallpaperJob();
							}
						} ).show();
			}
		} );

		DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this,
																  drawer,
																  toolbar,
																  R.string.navigation_drawer_open,
																  R.string.navigation_drawer_close );
		drawer.setDrawerListener( toggle );
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
		navigationView.setNavigationItemSelectedListener( this );

		// Locate the ImageView in activity_main.xml
		m_imageView = (ImageView) findViewById( R.id.image );

		setupWallpaperJob();
	}

	private void stopWallpaperJob()
	{
		JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
		jobScheduler.cancelAll();
	}

	private void setupWallpaperJob()
	{
		JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
		if( jobScheduler.getAllPendingJobs().size() == 0 )
		{
			JobInfo.Builder builder = new JobInfo.Builder( 1,
														   new ComponentName( getPackageName(),
																			  WallpaperService.class.getName() ) );

			builder.setPeriodic( 24 * 60 * 60 * 1000 );
			builder.setPersisted( true );

			jobScheduler.schedule( builder.build() );

			Toast.makeText( MainActivity.this, "Scheduling Wallpaper Service", Toast.LENGTH_SHORT ).show();
		}
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
		if( drawer.isDrawerOpen( GravityCompat.START ) )
		{
			drawer.closeDrawer( GravityCompat.START );
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		int id = item.getItemId();

		if( id == R.id.action_settings )
		{
			return true;
		}

		return super.onOptionsItemSelected( item );
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected( MenuItem item )
	{
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
		drawer.closeDrawer( GravityCompat.START );
		return true;
	}

	// DownloadImage AsyncTask
	private class DownloadImage extends AsyncTask<String, Void, Bitmap>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			// Create a progressdialog
			m_progressDialog = new ProgressDialog( MainActivity.this );
			// Set progressdialog title
			m_progressDialog.setTitle( "Download Image" );
			// Set progressdialog message
			m_progressDialog.setMessage( "Loading..." );
			m_progressDialog.setIndeterminate( false );
			// Show progressdialog
			m_progressDialog.show();
		}

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

				setHomeScreenWallpaper( bitmap );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			return bitmap;
		}

		private void setHomeScreenWallpaper( Bitmap bitmap )
		{
			DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

			final int maxWidth = displayMetrics.heightPixels * 2;

			final Bounds wallpaperSize = new Bounds( maxWidth, displayMetrics.heightPixels );

			final Bounds imageSize = new Bounds( bitmap.getWidth(), bitmap.getHeight() );
			final Bounds scaledSize = getScaledBounds( imageSize, wallpaperSize );

			Log.d( TAG, "Image Size: " + imageSize.m_width + " x " + imageSize.m_height );
			Log.d( TAG, "Wallpaper Size: " + wallpaperSize.m_width + " x " + wallpaperSize.m_height );
			Log.d( TAG, "Scaled Size: " + scaledSize.m_width + " x " + scaledSize.m_height );

			Bitmap scaledBitmap = Bitmap.createScaledBitmap( bitmap, (int) scaledSize.m_width, (int) scaledSize.m_height, false );

			WallpaperManager wallpaperManager = WallpaperManager.getInstance( MainActivity.this );
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

		@Override
		protected void onPostExecute( Bitmap result )
		{
			// Set the bitmap into ImageView
			m_imageView.setImageBitmap( result );

			// Close progressdialog
			m_progressDialog.dismiss();
		}
	}
}
