package com.levelup.picturecache.loaders;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.levelup.picturecache.PictureLoaderHandler;
import com.levelup.picturecache.UIHandler;
import com.levelup.picturecache.transforms.bitmap.BitmapTransform;
import com.levelup.picturecache.transforms.storage.StorageTransform;


public class RemoteViewLoader extends PictureLoaderHandler {
	private final RemoteViews remoteViews;
	private final int viewId;
	private final int defaultResourceId;
	private String mLoadingUrl;

	public RemoteViewLoader(RemoteViews remoteViews, int viewId, int defaultDrawableResId, StorageTransform storageTransform, BitmapTransform loadTransform) {
		super(storageTransform, loadTransform);
		this.remoteViews = remoteViews;
		this.viewId = viewId;
		this.defaultResourceId = defaultDrawableResId;
	}

	@Override
	public void drawDefaultPicture(String url, UIHandler postHandler) {
		remoteViews.setImageViewResource(viewId, defaultResourceId);
	}

	@Override
	public void drawBitmap(Drawable bmp, String url, UIHandler postHandler) {
		remoteViews.setImageViewBitmap(viewId, ViewLoader.drawableToBitmap(bmp));
	}

	@Override
	public boolean equals(Object o) {
		if (o==this) return true;
		if (!(o instanceof RemoteViewLoader)) return false;
		RemoteViewLoader loader = (RemoteViewLoader) o;
		return viewId==loader.viewId && remoteViews==loader.remoteViews && super.equals(loader);
	}

	@Override
	public int hashCode() {
		return (super.hashCode()*31 + viewId) * 31 + remoteViews.hashCode();
	}

	@Override
	public String toString() {
		return remoteViews.toString()+":"+viewId;
	}

	@Override
	public String setLoadingURL(String newURL) {
		String oldLoadingUrl = mLoadingUrl;
		mLoadingUrl = newURL;
		return oldLoadingUrl;
	}

	@Override
	public String getLoadingURL() {
		return mLoadingUrl;
	}

	@Override
	protected boolean canDirectLoad(File file, UIHandler uiHandler) {
		return true;
	}
}
