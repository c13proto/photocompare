package ch.want.imagecompare.ui.compareimages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ch.want.imagecompare.BundleKeys;
import ch.want.imagecompare.R;
import ch.want.imagecompare.data.ImageBean;
import ch.want.imagecompare.domain.FileImageMediaQuery;
import ch.want.imagecompare.domain.PhotoViewMediator;
import android.widget.LinearLayout.LayoutParams;

/**
 * @link https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
 * @link https://blog.fossasia.org/how-to-create-a-basic-gallery-application/
 */
public class CompareImagesActivity extends AppCompatActivity {

    private String currentImageFolder;
    private PhotoViewMediator photoViewMediator;
    private Switch syncToggle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_compare_images);

        photoViewMediator = buildPhotoViewMediator();
        initInitialState(savedInstanceState);
        initToolbar();

        setupViewSize();

    }

    private void setupViewSize(){
        //get frame_layout size
        FrameLayout compare_frame=findViewById(R.id.frameLayout_compare);
        ViewTreeObserver vto = compare_frame.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                compare_frame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = compare_frame.getMeasuredWidth();
                int height = compare_frame.getMeasuredHeight();

                View upperLayout=findViewById(R.id.upperImage);
                View bottomLayout=findViewById(R.id.bottomImage);
                android.view.ViewGroup.LayoutParams upper_params=upperLayout.getLayoutParams();
                android.view.ViewGroup.LayoutParams bottom_params=bottomLayout.getLayoutParams();
                if(width>height){//why overlapped?
                    final int view_width=width/2-width/30;
                    upper_params.width=view_width;
                    bottom_params.width=view_width;
                }
                else{
                    final int view_height=height/2;
                    upper_params.height=view_height;
                    bottom_params.height=view_height;
                }
                upperLayout.setLayoutParams(bottom_params);
                bottomLayout.setLayoutParams(upper_params);
            }
        });
    }

    private PhotoViewMediator buildPhotoViewMediator() {

        final PhotoViewMediator mediator = new PhotoViewMediator(new ImageDetailViewImpl(findViewById(R.id.upperImage)), new ImageDetailViewImpl(findViewById(R.id.bottomImage)));
        syncToggle = findViewById(R.id.toggleZoomPanSync);
        syncToggle.setOnCheckedChangeListener((buttonView, isChecked) -> mediator.setSyncZoomAndPan(isChecked));
        syncToggle.setChecked(false);
        //
        final ImageButton resetMatrixButton = findViewById(R.id.resetMatrix);
        resetMatrixButton.setOnClickListener(v -> photoViewMediator.resetState());

        final ImageButton shuffleButton = findViewById(R.id.button_shuffle);
        shuffleButton.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ShuffleView();
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    ShuffleView();
                    break;
                }
            }
            return true;
        });

        return mediator;
    }

    private void ShuffleView(){
        View upperLayout=findViewById(R.id.upperImage);
        View bottomLayout=findViewById(R.id.bottomImage);
        android.view.ViewGroup.LayoutParams upper_params=upperLayout.getLayoutParams();
        android.view.ViewGroup.LayoutParams bottom_params=bottomLayout.getLayoutParams();
        upperLayout.setLayoutParams(bottom_params);
        bottomLayout.setLayoutParams(upper_params);
    }

    private void initInitialState(final Bundle savedInstanceState) {
        final int topImageIndex;
        final int bottomIndex;
        final ArrayList<ImageBean> selectedBeansFromState;
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            currentImageFolder = intent.getStringExtra(BundleKeys.KEY_IMAGE_FOLDER);
            selectedBeansFromState = intent.getParcelableArrayListExtra(BundleKeys.KEY_SELECTION_COLLECTION);
            topImageIndex = intent.getIntExtra(BundleKeys.KEY_TOPIMAGE_INDEX, 0);
            bottomIndex = intent.getIntExtra(BundleKeys.KEY_BOTTOMIMAGE_INDEX, PhotoViewMediator.NO_VALID_IMAGE_INDEX);
        } else {
            currentImageFolder = savedInstanceState.getString(BundleKeys.KEY_IMAGE_FOLDER);
            selectedBeansFromState = savedInstanceState.getParcelableArrayList(BundleKeys.KEY_SELECTION_COLLECTION);
            topImageIndex = savedInstanceState.getInt(BundleKeys.KEY_TOPIMAGE_INDEX);
            bottomIndex = savedInstanceState.getInt(BundleKeys.KEY_BOTTOMIMAGE_INDEX, PhotoViewMediator.NO_VALID_IMAGE_INDEX);
        }
        final List<ImageBean> allImageBeans = new FileImageMediaQuery(getContentResolver(), currentImageFolder).execute();
        if (selectedBeansFromState != null && !selectedBeansFromState.isEmpty()) {
            ImageBean.copySelectedState(selectedBeansFromState, allImageBeans);
        }
        photoViewMediator.initGalleryImageList(allImageBeans, topImageIndex, bottomIndex);
        syncToggle.setChecked(true);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.compare_image_menu, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(BundleKeys.KEY_IMAGE_FOLDER, currentImageFolder);
        savedInstanceState.putInt(BundleKeys.KEY_TOPIMAGE_INDEX, photoViewMediator.getTopIndex());
        savedInstanceState.putInt(BundleKeys.KEY_BOTTOMIMAGE_INDEX, photoViewMediator.getBottomIndex());
        savedInstanceState.putParcelableArrayList(BundleKeys.KEY_SELECTION_COLLECTION, new ArrayList<>(ImageBean.getSelectedImageBeans(photoViewMediator.getGalleryImageList())));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showSelection:
                new ShowSelectedImagesTransition(this, currentImageFolder, photoViewMediator).execute();
                return true;
            case R.id.checkboxStyleDark:
                final boolean isNowDarkMode = photoViewMediator.toggleCheckboxStyleDark();
                item.setChecked(isNowDarkMode);
                return true;
            case R.id.showExifDetails:
                final boolean isNowShowingExif = photoViewMediator.toggleExifDisplay();
                item.setChecked(isNowShowingExif);
                return true;
            case android.R.id.home:
                new BackToImageListTransition(this, currentImageFolder, photoViewMediator.getGalleryImageList()).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
