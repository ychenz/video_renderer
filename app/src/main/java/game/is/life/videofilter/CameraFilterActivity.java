package game.is.life.videofilter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidexperiments.shadercam.fragments.CameraFragment;
import com.androidexperiments.shadercam.fragments.PermissionsHelper;
import com.androidexperiments.shadercam.gl.CameraRenderer;
import com.androidexperiments.shadercam.utils.ShaderUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import game.is.life.videofilter.renderer.BlackWhiteRenderer;
import game.is.life.videofilter.renderer.ExampleRenderer;
import game.is.life.videofilter.renderer.DystopiaRenderer;
import game.is.life.videofilter.renderer.InvertedRenderer;
import game.is.life.videofilter.renderer.SuperAwesomeRenderer;

// Current in use
public class CameraFilterActivity extends AppCompatActivity implements CameraRenderer.OnRendererReadyListener, PermissionsHelper.PermissionsListener{

    private CameraFragment mCameraFragment;
    private CameraRenderer mRenderer = null;
    private PermissionsHelper mPermissionsHelper;
    private boolean mPermissionsSatisfied = false;
    private static final String TAG = CameraFilterActivity.class.getSimpleName();
    private FileIO fileIO;
    private String storeFileName = "";

    private static final String TAG_CAMERA_FRAGMENT = "tag_camera_frag";

    /**
     * boolean for triggering restart of camera after completed rendering
     */
    private boolean mRestartCamera = false;

    /**
     * We inject our views from our layout xml here using {@link ButterKnife}
     */
    @InjectView(R.id.cameraSurface) TextureView mTextureView;
    @InjectView(R.id.btn_record) FloatingActionButton mRecordBtn;
    @InjectView(R.id.btn_swap_camera) ImageButton mSwapCameraButton;
    @InjectView(R.id.btn_close) ImageButton mCloseButton;
    @InjectView(R.id.btn_choose_filter) ImageButton mChooseFilterButton;

    private Drawable recordBtnIcon;
    private Drawable stopRecordBtnIcon;
    private Drawable swapCameraIcon;
    private Drawable closeIcon;
    private Drawable menuIcon;
    private ArrayList<CameraRenderer> renderers;
    private int render_idx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_camera);
        fileIO = FileIO.getInstance();
        ButterKnife.inject(this);

        // set record button icon
        recordBtnIcon = MaterialDrawableBuilder.with(getApplicationContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.RECORD) // provide an icon
                .setColor(Color.RED) // set the icon color
                .setSizeDp(50) // set the icon size
                .build();
        stopRecordBtnIcon = MaterialDrawableBuilder.with(getApplicationContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.STOP) // provide an icon
                .setColor(Color.RED) // set the icon color
                .setSizeDp(50) // set the icon size
                .build();
        swapCameraIcon = MaterialDrawableBuilder.with(getApplicationContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CAMERA_SWITCH) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setSizeDp(30)
                .build();
        closeIcon = MaterialDrawableBuilder.with(getApplicationContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CLOSE) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setSizeDp(30)
                .build();

        menuIcon = MaterialDrawableBuilder.with(getApplicationContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.MENU) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setSizeDp(30)
                .build();

        mRecordBtn.setImageDrawable(recordBtnIcon);
        mSwapCameraButton.setImageDrawable(swapCameraIcon);
        mCloseButton.setImageDrawable(closeIcon);
        mChooseFilterButton.setImageDrawable(menuIcon);

        renderers = new ArrayList<>();

        Intent intent = getIntent();
        render_idx = intent.getIntExtra("renderer", 0);

        setupCameraFragment();
        setupInteraction();

        if(PermissionsHelper.isMorHigher())
            setupPermissions();

    }

    private void setupPermissions() {
        mPermissionsHelper = PermissionsHelper.attach(this);
        mPermissionsHelper.setRequestedPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        );
    }

    /**
     * create the camera fragment responsible for handling camera state and add it to our activity
     */
    private void setupCameraFragment()
    {
        if(mCameraFragment != null && mCameraFragment.isAdded())
            return;

        mCameraFragment = CameraFragment.getInstance();
        mCameraFragment.setCameraToUse(CameraFragment.CAMERA_PRIMARY); //pick which camera u want to use, we default to forward
        mCameraFragment.setTextureView(mTextureView);

        //add fragment to our setup and let it work its magic
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mCameraFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();
    }

    /**
     * add a listener for touch on our surface view that will pass raw values to our renderer for
     * use in our shader to control color channels.
     */
    private void setupInteraction() {
        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mRenderer instanceof ExampleRenderer) {
                    ((ExampleRenderer) mRenderer).setTouchPoint(event.getRawX(), event.getRawY());
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Things are good to go and we can continue on as normal. If this is called after a user
     * sees a dialog, then onResume will be called next, allowing the app to continue as normal.
     */
    @Override
    public void onPermissionsSatisfied() {
        Log.d(TAG, "onPermissionsSatisfied()");
        mPermissionsSatisfied = true;
    }

    /**
     * User did not grant the permissions needed for out app, so we show a quick toast and kill the
     * activity before it can continue onward.
     * @param failedPermissions string array of which permissions were denied
     */
    @Override
    public void onPermissionsFailed(String[] failedPermissions) {
        Log.e(TAG, "onPermissionsFailed()" + Arrays.toString(failedPermissions));
        mPermissionsSatisfied = false;
        Toast.makeText(this, "shadercam needs all permissions to function, please try again.", Toast.LENGTH_LONG).show();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");

        ShaderUtils.goFullscreen(this.getWindow());

        /**
         * if we're on M and not satisfied, check for permissions needed
         * {@link PermissionsHelper#checkPermissions()} will also instantly return true if we've
         * checked prior and we have all the correct permissions, allowing us to continue, but if its
         * false, we want to {@code return} here so that the popup will trigger without {@link #setReady(SurfaceTexture, int, int)}
         * being called prematurely
         */
        //
        if(PermissionsHelper.isMorHigher() && !mPermissionsSatisfied) {
            if(!mPermissionsHelper.checkPermissions())
                return;
            else
                mPermissionsSatisfied = true; //extra helper as callback sometimes isnt quick enough for future results
        }

        if(!mTextureView.isAvailable())
            mTextureView.setSurfaceTextureListener(mTextureListener); //set listener to handle when its ready
        else
            setReady(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        shutdownCamera(false);
        mTextureView.setSurfaceTextureListener(null);
    }

    /**
     * called whenever surface texture becomes initially available or whenever a camera restarts after
     * completed recording or resuming from onpause
     * @param surface {@link SurfaceTexture} that we'll be drawing into
     * @param width width of the surface texture
     * @param height height of the surface texture
     */
    protected void setReady(SurfaceTexture surface, int width, int height) {
        mRenderer = getRenderer(surface, width, height);
        mRenderer.setCameraFragment(mCameraFragment);
        mRenderer.setOnRendererReadyListener(this);
        mRenderer.start();

        //initial config if needed
        mCameraFragment.configureTransform(width, height);
    }

    protected CameraRenderer getRenderer(SurfaceTexture surface, int width, int height) {
        /**
         * ShaderToy reference
         * iChannel0 = camTexture
         * texture = texture2D
         * fragColor = gl_FragColor
         * fragCoord = v_CamTexCoordinate
         *
         */
        renderers.add(new ExampleRenderer(this, surface, width, height));
        renderers.add(new BlackWhiteRenderer(this, surface, width, height));
        renderers.add(new DystopiaRenderer(this, surface, width, height));
        renderers.add(new InvertedRenderer(this, surface, width, height));
        renderers.add(new SuperAwesomeRenderer(this,surface, width, height));

        Log.d(TAG + " render index", String.valueOf(render_idx));
        return renderers.get(render_idx);
    }

    /**
     * kills the camera in camera fragment and shutsdown render thread
     * @param restart whether or not to restart the camera after shutdown is complete
     */
    private void shutdownCamera(boolean restart)
    {
        //make sure we're here in a working state with proper permissions when we kill the camera
        if(PermissionsHelper.isMorHigher() && !mPermissionsSatisfied) return;

        //check to make sure we've even created the cam and renderer yet
        if(mCameraFragment == null || mRenderer == null) return;

        mCameraFragment.closeCamera();

        mRestartCamera = restart;
        mRenderer.getRenderHandler().sendShutdown();
        mRenderer = null;
    }

    private void startRecording()
    {
        // todo alertDialog cancels full screen mode
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        alert.setTitle("Video name");
        alert.setView(edittext);
        alert.setCancelable(false);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                String videoName = edittext.getText().toString() + ".mp4";
                storeFileName = videoName;
                if (!videoName.equals("")){
                    mRenderer.startRecording(new File(fileIO.getAppFolder(),videoName));
                    mRecordBtn.setImageDrawable(stopRecordBtnIcon);
                }else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter a name!", Toast.LENGTH_SHORT).show();
                }

                ShaderUtils.goFullscreen(getWindow());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                storeFileName = "";
                ShaderUtils.goFullscreen(getWindow());
            }
        });

        alert.show();

    }

    private void stopRecording()
    {
        mRenderer.stopRecording();
        mRecordBtn.setImageDrawable(recordBtnIcon);

        //restart so surface is recreated
        shutdownCamera(true);

        Toast.makeText(this, "File recording complete: " + fileIO.getAppFolder().toString() +
                File.separator + this.storeFileName, Toast.LENGTH_LONG).show();
    }

    /**
     * Interface overrides from our {@link com.androidexperiments.shadercam.gl.CameraRenderer.OnRendererReadyListener}
     * interface. Since these are being called from inside the CameraRenderer thread, we need to make sure
     * that we call our methods from the {@link #runOnUiThread(Runnable)} method, so that we don't
     * throw any exceptions about touching the UI from non-UI threads.
     *
     * Another way to handle this would be to create a Handler/Message system similar to how our
     * {@link com.androidexperiments.shadercam.gl.CameraRenderer.RenderHandler} works.
     */
    @Override
    public void onRendererReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCameraFragment.setPreviewTexture(mRenderer.getPreviewTexture());
                mCameraFragment.openCamera();
            }
        });
    }

    @Override
    public void onRendererFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRestartCamera) {
                    setReady(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
                    mRestartCamera = false;
                }
            }
        });
    }

    /**
     * {@link android.view.TextureView.SurfaceTextureListener} responsible for setting up the rest of the
     * rendering and recording elements once our TextureView is good to go.
     */
    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener()
    {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width, final int height) {
            //convenience method since we're calling it from two places
            setReady(surface, width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            mCameraFragment.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) { }
    };

    @OnClick(R.id.btn_swap_camera)
    public void onClickSwapCamera()
    {
        mCameraFragment.swapCamera();
    }

    @OnClick(R.id.btn_choose_filter)
    public void onClickChooseFilter() {
        if(mRenderer.isRecording()){
            Toast.makeText(getApplicationContext(),
                    "Please stop the current recording first!", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence names[] = new CharSequence[renderers.size()];
        for (int i=0; i<renderers.size();i++){
            names[i] = renderers.get(i).toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a filter");
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]mRenderer = new ExampleRenderer(this, surface, mCameraFragment, width, height);
                Intent intent = getIntent();
                intent.putExtra("renderer",which);
                finish();
                startActivity(intent);
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ShaderUtils.goFullscreen(getWindow());
            }
        });
        builder.show();
    }

    @OnClick(R.id.btn_close)
    public void onClickCLose()
    {
        if(mRenderer.isRecording()){
            Toast.makeText(getApplicationContext(),
                    "Please stop the current recording first!", Toast.LENGTH_SHORT).show();
        }else {
            finish();
        }
    }

    @OnClick(R.id.btn_record)
    public void onClickRecord()
    {
        if(mRenderer.isRecording())
            stopRecording();
        else
            startRecording();
    }

}
