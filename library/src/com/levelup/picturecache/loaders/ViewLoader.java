package com.levelup.picturecache.loaders;

import java.io.File;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.levelup.picturecache.LogManager;
import com.levelup.picturecache.PictureCache;
import com.levelup.picturecache.PictureLoaderHandler;
import com.levelup.picturecache.UIHandler;
import com.levelup.picturecache.transforms.bitmap.BitmapTransform;
import com.levelup.picturecache.transforms.storage.StorageTransform;

/**
 * the base class used to display the loaded/default bitmap in an View
 * <p>
 * @see {@link ViewLoaderDefaultResource} and {@link ViewLoaderDefaultDrawable} 
 */
public abstract class ViewLoader<T extends View> extends PictureLoaderHandler {
	private final T view;

	private static final long MAX_SIZE_IN_UI_THREAD = 19000;
	static final boolean DEBUG_VIEW_LOADING = BuildConfig.DEBUG && false;

	public ViewLoader(T view, StorageTransform storageTransform, BitmapTransform loadTransform) {
		super(storageTransform, loadTransform);
		if (view==null) throw new NullPointerException("empty view to load to, use PrecacheImageLoader");
		this.view = view;
	}

	public T getImageView() {
		return view;
	}

	@Override
	public boolean equals(Object o) {
		if (o==this) return true;
		if (!(o instanceof ViewLoader)) return false;
		ViewLoader<?> loader = (ViewLoader<?>) o;
		//if (DEBUG_VIEW_LOADING && toString().equals(loader.toString())) Log.e("PlumeCache",this+" same equals "+loader+" = "+(loader.view==view && Float.compare(loader.mRotation,mRotation)==0 && loader.mRoundedCorner==mRoundedCorner));
		return loader.view==view && super.equals(loader);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + view.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+"@"+hashCode()+(getStorageTransform()!=null ? getStorageTransform().getVariantPostfix() : "");
	}

	@Override
	public final void drawDefaultPicture(String url, UIHandler postHandler) {
		if (DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" drawDefaultPicture");
		showDrawable(postHandler, null, url);
	}

	@Override
	public final void drawBitmap(Bitmap bmp, String url, UIHandler postHandler) {
		if (DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" drawBitmap "+view+" with "+bmp);
		showDrawable(postHandler, bmp, url);
	}

	/**
	 * display the default view, called in the UI thread
	 * called under a lock on {@link view}
	 */
	protected abstract void displayDefaultView();

	/**
	 * display this Bitmap in the view, called in the UI thread
	 * @param bmp the Bitmap to display in {@link view}
	 * called under a lock on {@link view}
	 */
	protected void displayCustomBitmap(Bitmap bmp) {
		if (view instanceof ImageView)
			((ImageView) view).setImageBitmap(bmp);
	}

	private void showDrawable(UIHandler postHandler, Bitmap customBitmap, String url) {
		synchronized (view) {
			ViewLoadingTag tag = (ViewLoadingTag) view.getTag();
			if (tag==null) {
				tag = new ViewLoadingTag(url, getStorageTransform(), getDisplayTransform());
				view.setTag(tag);
			}
			tag.setPendingDraw(customBitmap, url);
			tag.drawInView(postHandler, this);
		}
	}

	@Override
	public String setLoadingURL(String newURL) {
		ViewLoadingTag newTag = new ViewLoadingTag(newURL, getStorageTransform(), getDisplayTransform());

		ViewLoadingTag oldTag = null;
		synchronized (view) {
			oldTag = (ViewLoadingTag) view.getTag();
			if (newTag.equals(oldTag)) {
				if (oldTag.isUrlLoaded() || oldTag.isBitmapPending()) {
					if (DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" setting the same picture in "+view+" isLoaded:"+oldTag.isUrlLoaded()+" drawPending:"+oldTag.isBitmapPending());
					return newURL; // no need to do anything
				}
				return null; // hack for now as the PictureCache will consider it's the same URL and do nothing, but it's the same URL loading as before
			}

			if (oldTag!=null) {
				// the previous URL loading is not good for this view anymore
				if (DEBUG_VIEW_LOADING) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" the old picture in "+view+" doesn't match "+newURL+" was "+oldTag+" isLoaded:"+oldTag.isUrlLoaded()+" drawPending:"+oldTag.isBitmapPending());
				// keep the previous state of the tag
				newTag.recoverStateFrom(oldTag);
			}

			view.setTag(newTag);
		}
		if (DEBUG_VIEW_LOADING) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" set loading "+view+" with "+newURL+" tag:"+newTag);


		if (oldTag==null || oldTag.url==null)
			return null;
		if (oldTag.url.equals(newURL))
			return null; // hack for now as the PictureCache will consider it's the same URL and do nothing, but the transforms have changed
		return oldTag.url;
	}

	@Override
	public String getLoadingURL() {
		ViewLoadingTag tag = (ViewLoadingTag) view.getTag();
		if (tag==null)
			return null;
		return tag.url;
	}

	@Override
	protected boolean canDirectLoad(File file, UIHandler uiHandler) {
		return !uiHandler.isUIThread() || file.length() < MAX_SIZE_IN_UI_THREAD;
	}
}