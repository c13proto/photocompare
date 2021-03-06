package ch.want.imagecompare.domain;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public abstract class ImageMediaQuery extends ImageMediaStore {

    private final String selectionExpression;
    private final String[] selectionArgs;

    protected ImageMediaQuery(final ContentResolver contentResolver) {
        super(contentResolver);
        selectionExpression = null;
        selectionArgs = null;
    }

    protected ImageMediaQuery(final ContentResolver contentResolver, final String selectionExpression, final String[] selectionArgs) {
        super(contentResolver);
        this.selectionExpression = selectionExpression;
        this.selectionArgs = selectionArgs;
    }

    public void execute() {
        final Cursor cursor = contentResolver.query(MEDIA_CONTENT_URI, PROJECTION, selectionExpression, selectionArgs, ORDER_BY);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                final String bucketPath = cursor.getString(0);
                final String imagePath = cursor.getString(1);
                doWithCursor(bucketPath, imagePath, buildContentUri(cursor.getInt(2)));
            }
            cursor.close();
        }
    }

    private static Uri buildContentUri(final int mediaId) {
        return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(mediaId));
    }

    /**
     * @param bucketPath      Column MediaStore.Images.Media.BUCKET_DISPLAY_NAME
     * @param imageFilePath   Column MediaStore.Images.Media.DATA
     * @param imageContentUri
     */
    public abstract void doWithCursor(String bucketPath, String imageFilePath, Uri imageContentUri);
}
