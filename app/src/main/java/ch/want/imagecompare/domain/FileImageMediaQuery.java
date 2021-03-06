package ch.want.imagecompare.domain;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.want.imagecompare.data.ImageBean;

public class FileImageMediaQuery {

    private final ContentResolver contentResolver;
    private final String bucketPath;
    private final List<ImageBean> galleryImageList = new ArrayList<>();

    public FileImageMediaQuery(ContentResolver contentResolver, final String bucketPath) {
        this.contentResolver = contentResolver;
        this.bucketPath = bucketPath;
    }

    public List<ImageBean> execute() {
        new ImageMediaQuery(contentResolver, MediaStore.Images.Media.DATA + " like ?", new String[]{bucketPath + "%"}) {

            @Override
            public void doWithCursor(final String bucketPath, final String imageFilePath, final Uri imageContentUri) {
                final File file = new File(imageFilePath);
                if (file.exists()) {
                    galleryImageList.add(new ImageBean(file.getName(), Uri.fromFile(file), imageContentUri));
                }
            }
        }.execute();
        return galleryImageList;
    }
}
