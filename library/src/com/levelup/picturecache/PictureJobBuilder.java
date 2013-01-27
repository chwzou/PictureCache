package com.levelup.picturecache;

import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

public class PictureJobBuilder {
	private String mURL;
	private String mUUID;
	private long mFreshDate;
	private LifeSpan mLifeSpan;
	private int mDimension;
	private boolean mWidthBased;
	private StorageType mExtensionMode = StorageType.AUTO;
	protected final PictureLoaderHandler mHandler;

	protected PictureJobBuilder(PictureLoaderHandler handler) {
		this.mHandler = handler;
	}

	public PictureJobBuilder setURL(String URL) {
		mURL = URL;
		return this;
	}

	public PictureJobBuilder setUUID(String UUID) {
		mUUID = UUID;
		return this;
	}

	/**
	 * set for how long the item should remain in the cache
	 * @return the {@link PictureJobBuilder} being created
	 */
	public PictureJobBuilder setLifeType(LifeSpan lifeSpan) {
		mLifeSpan = lifeSpan;
		return this;
	}

	public PictureJobBuilder setFreshDate(long date) {
		mFreshDate = date;
		return this;
	}

	public PictureJobBuilder setDimension(int dimension, boolean widthBased) {
		mDimension = dimension;
		mWidthBased = widthBased;
		return this;
	}

	public PictureJobBuilder setExtensionMode(StorageType extensionMode) {
		mExtensionMode = extensionMode;
		return this;
	}

	CacheKey buildKey() throws NoSuchAlgorithmException {
		if (!TextUtils.isEmpty(mUUID))
			return CacheKey.newUUIDBasedKey(mUUID, mDimension, mWidthBased, mExtensionMode, mHandler.getStorageTransform()!=null ? mHandler.getStorageTransform().getVariantPostfix() : null);

		if (!TextUtils.isEmpty(mURL))
			return CacheKey.newUrlBasedKey(mURL, mDimension, mWidthBased, mExtensionMode, mHandler.getStorageTransform()!=null ? mHandler.getStorageTransform().getVariantPostfix() : null);

		return null;
	}

	void startLoading(PictureCache cache) throws NoSuchAlgorithmException {
		CacheKey key = buildKey();
		if (key==null) {
			LogManager.logger.w(PictureCache.TAG, "could not generate a CacheKey for "+mUUID+" / "+mURL);
			return;
		}

		cache.getPicture(mURL, key, mFreshDate, mHandler, mLifeSpan);
	}
}