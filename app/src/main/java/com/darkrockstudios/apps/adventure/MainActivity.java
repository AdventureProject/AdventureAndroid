package com.darkrockstudios.apps.adventure;

import android.app.job.JobScheduler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private ImageView m_imageView;
	private CheckBox m_scheduledCheckbox;
	private FloatingActionButton m_fab;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		m_fab = (FloatingActionButton) findViewById( R.id.fab );
		m_fab.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				new WallpaperActivityTask().execute( WallpaperTask.URL_RANDOM );
				Snackbar.make( view, "Fetching new Wallpaper", Snackbar.LENGTH_LONG ).show();
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

		m_scheduledCheckbox = (CheckBox) findViewById( R.id.scheduledCheckbox );

		new LoadImageForPreview().execute();
	}

	private void populateCheckbox()
	{
		JobScheduler jobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
		boolean jobScheduled = (jobScheduler.getAllPendingJobs().size() > 0);
		m_scheduledCheckbox.setChecked( jobScheduled );
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		m_scheduledCheckbox.setOnCheckedChangeListener( null );
		populateCheckbox();
		m_scheduledCheckbox.setOnCheckedChangeListener( this );
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

	@Override
	public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
	{
		if( isChecked )
		{
			WallpaperUtils.setupWallpaperJob( this );
			Snackbar.make( m_scheduledCheckbox, "Starting Wallpaper Job", Snackbar.LENGTH_LONG ).show();
		}
		else
		{
			WallpaperUtils.stopWallpaperJob( MainActivity.this );
			Snackbar.make( m_scheduledCheckbox, "Stopping Wallpaper Job", Snackbar.LENGTH_LONG ).show();
		}
	}

	private class LoadImageForPreview extends AsyncTask<Void, Void, Bitmap>
	{
		@Override
		protected Bitmap doInBackground( Void... params )
		{
			File image = WallpaperUtils.getCurrentWallpaperFile( MainActivity.this );
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			Bitmap bitmap = BitmapFactory.decodeFile( image.getAbsolutePath(), bmOptions );
			if( bitmap != null )
			{
				bitmap = Bitmap.createScaledBitmap( bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false );
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute( Bitmap result )
		{
			if( result != null )
			{
				m_imageView.setImageBitmap( result );
			}
			else
			{
				new WallpaperActivityTask().execute( WallpaperTask.URL_TODAY );
			}
		}
	}

	private class WallpaperActivityTask extends AsyncTask<String, Void, Bitmap>
	{
		@Override
		protected Bitmap doInBackground( String... params )
		{
			final WallpaperTask task = new WallpaperTask( MainActivity.this, params[ 0 ] );
			task.run();
			final Bitmap bitmap = task.getBitmap();

			return bitmap;
		}

		@Override
		protected void onPostExecute( Bitmap bitmap )
		{
			super.onPostExecute( bitmap );

			if( bitmap != null )
			{
				m_imageView.setImageBitmap( bitmap );
			}
		}
	}
}
