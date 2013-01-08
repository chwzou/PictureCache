package com.levelup.picturecache;

import java.io.File;

import com.levelup.HandlerUIThread;

import android.graphics.Bitmap;

import com.levelup.log.AbstractLogger;

public abstract class PictureLoaderHandler {

	abstract protected void drawDefaultPicture(String url, HandlerUIThread postHandler, AbstractLogger logger);
	abstract protected void drawBitmap(Bitmap bmp, String url, HandlerUIThread postHandler, AbstractLogger logger);
	
	protected PictureLoaderHandler(StorageTransform bitmapStorageTransform, BitmapTransform bitmapTransform) {
		this.mStorageTransform = bitmapStorageTransform;
		this.mBitmapTransform = bitmapTransform;
	}
	
	protected StorageTransform getStorageTransform() {
		return mStorageTransform;
	}
	
	protected BitmapTransform getDisplayTransform() {
		return mBitmapTransform;
	}
	
	/**
	 * 
	 * @param downloadManager
	 * @param newURL
	 * @param logger TODO
	 * @return true if the URL is new for this target
	 */
	abstract protected boolean setLoadingNewURL(DownloadManager downloadManager, String newURL, AbstractLogger logger);
	/**
	 * 
	 * @return
	 */
	abstract protected String getLoadingURL();
	abstract protected boolean canDirectLoad(File file);
	
	protected boolean isDownloadAllowed() {
		return true;
	}

	protected final BitmapTransform mBitmapTransform;
	protected final StorageTransform mStorageTransform;

	@Override
	public boolean equals(Object o) {
		if (this==o) return true;
		if (!(o instanceof PictureLoaderHandler)) return false;
		PictureLoaderHandler loader = (PictureLoaderHandler) o;
		return (mBitmapTransform==null && loader.mBitmapTransform==null) || (mBitmapTransform!=null && mBitmapTransform.equals(loader))
				&& (mStorageTransform==null && loader.mStorageTransform==null) || (mStorageTransform!=null && mStorageTransform.equals(loader));
	}
	
	@Override
	public int hashCode() {
		return (mBitmapTransform==null ? 0 : mBitmapTransform.hashCode()) * 31 + (mStorageTransform==null ? 0 : mStorageTransform.hashCode());
	}
}