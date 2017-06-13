package com.darkrockstudios.apps.adventure

import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.*
import butterknife.bindView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.FileInputStream
import java.io.IOException
import java.io.ObjectInputStream

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener
{
	private val m_connection = object : CustomTabsServiceConnection()
	{
		override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient)
		{
			m_customTabsClient = client
			client.warmup(0)
		}

		override fun onServiceDisconnected(name: ComponentName)
		{

		}
	}
	private var m_customTabsClient: CustomTabsClient? = null

	val m_toolbar: Toolbar by bindView(R.id.toolbar)

	val m_imageView: ImageView by bindView(R.id.image)

	val m_mapZoomedOutView: ImageView by bindView(R.id.map_zoomed_out)

	val m_mapZoomedInView: ImageView by bindView(R.id.map_zoomed_in)

	val m_scheduledCheckbox: CheckBox by bindView(R.id.scheduledCheckbox)

	val m_fab: FloatingActionButton by bindView(R.id.fab)

	val m_progressBar: ProgressBar by bindView(R.id.progressBar)

	val m_photoTitleView: TextView by bindView(R.id.photo_title)

	val m_photoDescriptionView: TextView by bindView(R.id.photo_description)

	val m_photoInfoButton: TextView by bindView(R.id.photo_info_button)

	val m_photoDateView: TextView by bindView(R.id.photo_date)

	val m_infoButton: View by bindView(R.id.photo_info_button)

	private var m_currentPhoto: Photo? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setSupportActionBar(m_toolbar)

		m_infoButton.setOnClickListener(this::onPhotoInfo)
		m_fab.setOnClickListener(this::onRandomPhoto)

		LoadImageForPreview().execute()

		val ok = CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", m_connection)
	}

	private fun populateCheckbox()
	{
		val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		val jobScheduled = jobScheduler.allPendingJobs.size > 0
		m_scheduledCheckbox.isChecked = jobScheduled
	}

	override fun onResume()
	{
		super.onResume()

		m_scheduledCheckbox.setOnCheckedChangeListener(null)
		populateCheckbox()
		m_scheduledCheckbox.setOnCheckedChangeListener(this)
	}

	override fun onDestroy()
	{
		super.onDestroy()

		if (m_customTabsClient != null)
		{
			m_customTabsClient = null
			unbindService(m_connection)
		}
	}

	override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean)
	{
		if (isChecked)
		{
			WallpaperUtils.setupWallpaperJob(this)
			Snackbar.make(m_scheduledCheckbox, R.string.toast_wallpaper_service_start_scheduling, Snackbar.LENGTH_LONG)
					.show()
		}
		else
		{
			WallpaperUtils.stopWallpaperJob(this@MainActivity)
			Snackbar.make(m_scheduledCheckbox, R.string.toast_wallpaper_service_stop_scheduling, Snackbar.LENGTH_LONG).show()
		}
	}

	fun onRandomPhoto(view: View)
	{
		WallpaperActivityTask().execute(WallpaperTask.URL_RANDOM)
		Snackbar.make(view, R.string.toast_getting_wallpaper, Snackbar.LENGTH_LONG).show()
	}

	fun onPhotoInfo(view: View)
	{
		m_currentPhoto?.let { currentPhoto ->
			//startActivity( new Intent( Intent.ACTION_VIEW ).setData( Uri.parse( m_currentPhoto.url ) ) );
			val builder = CustomTabsIntent.Builder()
			builder.setShowTitle(true)
			builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
			val customTabsIntent = builder.build()
			customTabsIntent.launchUrl(this, Uri.parse(currentPhoto.url))
		}
	}

	private fun showLoading()
	{
		if (m_progressBar != null)
		{
			m_progressBar.visibility = View.VISIBLE
		}

		if (m_imageView != null)
		{
			m_imageView.visibility = View.GONE
			m_imageView.setImageBitmap(null)
		}

		if (m_photoTitleView != null)
		{
			m_photoTitleView.text = ""
		}

		if (m_photoDateView != null)
		{
			m_photoDateView.text = ""
		}

		if (m_photoDescriptionView != null)
		{
			m_photoDescriptionView.text = ""
		}

		if (m_photoInfoButton != null)
		{
			m_photoInfoButton.visibility = View.GONE
		}

		if (m_mapZoomedOutView != null)
		{
			m_mapZoomedOutView.setImageDrawable(null)
			m_mapZoomedOutView.visibility = View.GONE
		}

		if (m_mapZoomedInView != null)
		{
			m_mapZoomedInView.setImageDrawable(null)
			m_mapZoomedInView.visibility = View.GONE
		}
	}

	private fun showImage(data: PhotoData)
	{
		m_currentPhoto = data.m_photo

		if (m_progressBar != null)
		{
			m_progressBar.visibility = View.GONE
		}

		if (m_photoInfoButton != null)
		{
			m_photoInfoButton.visibility = View.VISIBLE
		}

		if (m_imageView != null)
		{
			m_imageView.setImageBitmap(data.m_bitmap)
			m_imageView.visibility = View.VISIBLE
		}

		var title = ""
		var description = ""
		var date = ""
		var location = ""
		if (data.m_photo != null)
		{
			if (!TextUtils.isEmpty(data.m_photo.title))
			{
				title = data.m_photo.title ?: ""
			}

			if (!TextUtils.isEmpty(data.m_photo.description))
			{
				description = data.m_photo.description ?: ""
			}

			if (!TextUtils.isEmpty(data.m_photo.date))
			{
				date = data.m_photo.date ?: ""
			}

			if (!TextUtils.isEmpty(data.m_photo.location))
			{
				location = data.m_photo.location ?: ""
			}
		}

		if (m_mapZoomedInView != null)
		{
			m_mapZoomedInView.visibility = View.VISIBLE

			val url = PhotoMap.getZoomedInMapUrl(location, this)
			Picasso.with(this)
					.load(url)
					.into(m_mapZoomedInView, ImageCallback())
		}

		if (m_mapZoomedOutView != null)
		{
			m_mapZoomedOutView.visibility = View.VISIBLE

			val url = PhotoMap.getZoomedOutMapUrl(location, this)
			Picasso.with(this)
					.load(url)
					.into(m_mapZoomedOutView, ImageCallback())
		}

		if (m_photoTitleView != null)
		{
			m_photoTitleView.text = title
		}

		if (m_photoDateView != null)
		{
			m_photoDateView.text = date
		}

		if (m_photoDescriptionView != null)
		{
			m_photoDescriptionView.text = description
		}
	}

	private inner class ImageCallback : Callback
	{
		override fun onSuccess()
		{

		}

		override fun onError()
		{

		}
	}

	private inner class LoadImageForPreview : AsyncTask<Void, Void, PhotoData>()
	{
		override fun onPreExecute()
		{
			super.onPreExecute()

			showLoading()
		}

		override fun doInBackground(vararg params: Void): PhotoData?
		{
			val imageFile = WallpaperUtils.getCurrentWallpaperFile(this@MainActivity)
			val bmOptions = BitmapFactory.Options()
			var bitmap: Bitmap? = BitmapFactory.decodeFile(imageFile.absolutePath, bmOptions)
			if (bitmap != null)
			{
				bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, false)
			}

			var photo: Photo?
			try
			{
				val photoFile = WallpaperUtils.getCurrentPhotoFile(this@MainActivity)
				val fin = FileInputStream(photoFile)
				val ois = ObjectInputStream(fin)
				photo = ois.readObject() as Photo
			}
			catch (e: IOException)
			{
				photo = null
				e.printStackTrace()
			}
			catch (e: ClassNotFoundException)
			{
				photo = null
				e.printStackTrace()
			}

			if (photo != null)
			{
				return PhotoData(photo, bitmap)
			}
			else
			{
				return null
			}
		}

		override fun onPostExecute(result: PhotoData?)
		{
			if (result == null)
			{
				WallpaperActivityTask().execute(WallpaperTask.URL_TODAY)
			}
			else
			{
				showImage(result)
			}
		}
	}

	private inner class WallpaperActivityTask : AsyncTask<String, Void, PhotoData>()
	{
		override fun onPreExecute()
		{
			super.onPreExecute()

			showLoading()
		}

		override fun doInBackground(vararg params: String): PhotoData
		{
			val task = WallpaperTask(this@MainActivity, params[0])
			task.run()

			return PhotoData(task.photo, task.bitmap)
		}

		override fun onPostExecute(data: PhotoData)
		{
			super.onPostExecute(data)

			showImage(data)
		}
	}

	private class PhotoData constructor(internal val m_photo: Photo?, internal val m_bitmap: Bitmap?)

	companion object
	{

		private val TAG = MainActivity::class.java.simpleName
	}
}
