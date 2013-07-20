package uk.co.ormand.teaseme;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

@SuppressLint("DefaultLocale")
public class AndroidTeaseMeActivity extends Activity {
	private File objSDRoot;
	private File objPresFolder;
	private String strPresentationPath;
	private String strMediaDirectory;
	private PageTimer tmrPageTimer;
	private Timer tmrTetronome;
	private WebView objWebView1;
	private TextView objCountText;
	private String strDelStyle;
	private String strDelTarget;
	private String strVidTarget;
	private Boolean blnDebug;
	private int intHeightTop;
	private int intWidthTop;
	private LinearLayout objLayoutImage;
	private LinearLayout objLayoutTop;
	private SoundPool soundPool;
	private int sound;
	private int MintFontSize;
	private SecureRandom rndGen = new SecureRandom();
	private String strFilePatern;
	private String strPasswordEntered;
	private String strPrefPassword;
	private ArrayList<String> Flags = new ArrayList<String>();
	private Boolean blnAutoSetPage;
	private Boolean blnImageBackground = true;
	private String strDelaySet;
	private String strDelayUnSet;
	private int intBtnLetters;
	private MediaPlayer mMediaPlayer;
	private static final String TAG = "ATM";
	private String strTitle;
	private Boolean blnImageZoomed;
	private SharedPreferences objLocalVarianbles;
	private SecureRandom mRandom = new SecureRandom();
	private int intAudioLoops;
	private String strAudio;
	private String strAudioTarget;
	private String strLoadSortOrder;
	private Boolean blnTitle = true;
	private Boolean blnClock = true;
	private int intTransparency = 255;
	private comonFunctions comFun = new comonFunctions(TAG);
	private HashMap<String, page> Ipages = new HashMap<String, page>(); 
	private VideoView objVideoView;
	private GifDecoder mGifDecoder;    
	private Bitmap mTmpBitmap;
	final Handler mHandler = new Handler(); 
	ImageView objImageView;
	File flImage = null;
	private Boolean blnShowGif = false;
	private String strCurrentGif = "";
	private String imgPath = "";

	// TODO about
	// TODO vote
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Runs this code first
		super.onCreate(savedInstanceState);
		try {
			// Set what the volume buttons do (so it will change the audio
			// volume not the phone ringer volume)
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			// Turn off title bar
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Force it to full screen
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			// Set what screen layout to use
			setContentView(R.layout.main);

			// Get the size of the screen in pixels
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);

			intHeightTop = dm.heightPixels;
			intWidthTop = dm.widthPixels;

			// Reference to the bit of the screen that displays the image
			objLayoutImage = (LinearLayout) findViewById(R.id.LayoutImage);
			objLayoutTop = (LinearLayout) findViewById(R.id.LayoutTop);

			// Get the current preferences
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			// debug flag
			blnDebug = sharedPrefs.getBoolean("Debug", false);

			// Title flag
			blnTitle = sharedPrefs.getBoolean("Title", true);

			// Clock flag
			blnClock = sharedPrefs.getBoolean("Clock", true);

			// transparency
			intTransparency = Integer.parseInt(sharedPrefs.getString("BtnTran", "0"));

			// font size
			MintFontSize = Integer.parseInt(sharedPrefs.getString("FontSize", "10"));
			// number of letters for buttons per row
			intBtnLetters = Integer.parseInt(sharedPrefs.getString("BtnLetters", "35"));
			// SD card location
			objSDRoot = Environment.getExternalStorageDirectory();
			// path to the xml files
			strPresentationPath = sharedPrefs.getString("PrefDir", "");
			if (strPresentationPath == "") {
				SharedPreferences.Editor objPrefEdit = sharedPrefs.edit();
				objPrefEdit.putString("PrefDir", objSDRoot.getAbsolutePath() + "/Android/data/uk.co.ormand.teaseme/files/");
				objPrefEdit.commit();
				strPresentationPath = sharedPrefs.getString("PrefDir", "");
			}
			//Image Full Screen default false
			blnImageBackground = sharedPrefs.getBoolean("FullScreen", false);
			if (!blnImageBackground) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			
			//Load Sort Order
			strLoadSortOrder = sharedPrefs.getString("SortOrder", "NAME");

			// Reference to the browser control that contains the text
			objWebView1 = (WebView) findViewById(R.id.webView1);
			// Set background colour to black
			objWebView1.setBackgroundColor(0);

			// Clock control that displays the current time
			DigitalClock objClock = (DigitalClock) findViewById(R.id.digitalClock1);
			if (blnClock) {
				objClock.setTextSize(MintFontSize);
				objClock.setTextColor(Color.WHITE);
				if (blnImageBackground) {
					objClock.setBackgroundColor(Color.parseColor("#00000000"));
				} else {
					objClock.setBackgroundColor(color.black);
				}
			} else {
				objClock.setBackgroundColor(Color.parseColor("#00000000"));
				objClock.setTextColor(Color.parseColor("#00000000"));
			}

			// Text control that contains page name in debug mode or description
			// if not
			TextView objDebug = (TextView) findViewById(R.id.textViewDebug);
			if (blnTitle) {
				objDebug.setTextSize(MintFontSize);
				objDebug.setTextColor(Color.WHITE);
				if (blnImageBackground) {
					objDebug.setBackgroundColor(Color.parseColor("#00000000"));
				} else {
					objDebug.setBackgroundColor(color.black);
				}
			} else {
				objDebug.setBackgroundColor(Color.parseColor("#00000000"));
				objDebug.setTextColor(Color.parseColor("#00000000"));

			}

			// Text control that contains the count down timer
			objCountText = (TextView) findViewById(R.id.textViewTimer);
			objCountText.setTextSize(MintFontSize);
			if (blnImageBackground) {
				objCountText.setBackgroundColor(Color.parseColor("#00000000"));
			} else {
				objCountText.setBackgroundColor(color.black);
			}

			// Refernce to the folder that contains the xml files
			objPresFolder = new File(strPresentationPath);

			// write .nomedia file so any pictures or videos do not appear in
			// the device gallery
			@SuppressWarnings("unused")
			boolean blnTry;
			// if the folder does not exist create it
			if (!objPresFolder.exists()) {
				blnTry = objPresFolder.mkdirs();
			}
			// write the .nomedia file
			File objNoMedia = new File(strPresentationPath + ".nomedia");
			if (!objNoMedia.exists()) {
				blnTry = objNoMedia.createNewFile();
			}

			// If a password is set then show the password dialogue
			strPrefPassword = sharedPrefs.getString("Password", "");
			if (!strPrefPassword.equals("")) {
				this.showDialog(DIALOG_PASSWORD_ENTER);
				// See onCreateDialog further down for what happens when they
				// exit this
			}

			// array to hold the various flags
			Flags.clear();
		} catch (NumberFormatException e) {
			Log.e(TAG, "OnCreate NumberFormatException ", e);
		} catch (Exception e) {
			Log.e(TAG, "OnCreate Exception ", e);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// If we go out of the app then stop any video / sounds that are
		// currently playing
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				mMediaPlayer.reset();
			}
		}

		if (tmrTetronome != null) {
			tmrTetronome.cancel();
			tmrTetronome = null;
		}
		if (soundPool != null) {
			soundPool.stop(intSoundStream);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	protected void onResume() {
		// If we go out and the come back in we need to re-initialise variable
		// that are lost in the process
		super.onResume();
		try {
			Log.d(TAG, "onResume Start ");
			Flags.clear();
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			blnDebug = sharedPrefs.getBoolean("Debug", false);
			blnTitle = sharedPrefs.getBoolean("Title", true);
			blnClock = sharedPrefs.getBoolean("Clock", true);
			intTransparency = Integer.parseInt(sharedPrefs.getString("BtnTran", "0"));
			MintFontSize = Integer.parseInt(sharedPrefs.getString("FontSize", "10"));
			intBtnLetters = Integer.parseInt(sharedPrefs.getString("BtnLetters", "35"));
			objSDRoot = Environment.getExternalStorageDirectory();
			strPresentationPath = sharedPrefs.getString("PrefDir", objSDRoot.getAbsolutePath() + "/Android/data/uk.co.ormand.teaseme/files/");
			//Image Full Screen default false
			blnImageBackground = sharedPrefs.getBoolean("FullScreen", false);
			if (!blnImageBackground) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
			strLoadSortOrder = sharedPrefs.getString("SortOrder", "NAME");



			// Clock control that displays the current time
			DigitalClock objClock = (DigitalClock) findViewById(R.id.digitalClock1);
			if (blnClock) {
				objClock.setTextSize(MintFontSize);
				objClock.setTextColor(Color.WHITE);
				if (blnImageBackground) {
					objClock.setBackgroundColor(Color.parseColor("#00000000"));
				} else {
					objClock.setBackgroundColor(color.black);
				}
			} else {
				objClock.setBackgroundColor(Color.parseColor("#00000000"));
				objClock.setTextColor(Color.parseColor("#00000000"));
			}

			// Text control that contains page name in debug mode or description
			// if not
			TextView objDebug = (TextView) findViewById(R.id.textViewDebug);
			if (blnTitle) {
				objDebug.setTextSize(MintFontSize);
				objDebug.setTextColor(Color.WHITE);
				if (blnImageBackground) {
					objDebug.setBackgroundColor(Color.parseColor("#00000000"));
				} else {
					objDebug.setBackgroundColor(color.black);
				}
			} else {
				objDebug.setBackgroundColor(Color.parseColor("#00000000"));
				objDebug.setTextColor(Color.parseColor("#00000000"));

			}

			// Text control that contains the count down timer
			objCountText.setTextSize(MintFontSize);
			if (blnImageBackground) {
				objCountText.setBackgroundColor(Color.parseColor("#00000000"));
			} else {
				objCountText.setBackgroundColor(color.black);
			}
			
			if (objLocalVarianbles!=null){
				String strPage = objLocalVarianbles.getString("CurrentPage", "start");
				String strFlags = objLocalVarianbles.getString("Flags", "");
				if (strFlags != "") {
					comFun.SetFlags(strFlags, Flags);
				}
				displayPage(strPage, false);
			}
			Log.d(TAG, "onResume End ");

		} catch (NumberFormatException e) {
			Log.e(TAG, "onResume Exception ", e);
		} catch (Exception e) {
			Log.e(TAG, "onResume Exception ", e);
		}
	}
	
	// onclick listener for dynamic buttons
	View.OnClickListener getOnClickDoSomething(final Button button) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				try {
					String strTag;
					strTag = (String) button.getTag(R.string.TagSetFlags);
					if (!strTag.equals("")) {
						comFun.SetFlags(strTag, Flags);
					}
					strTag = (String) button.getTag(R.string.TagUnSetFlags);
					if (!strTag.equals("")) {
						comFun.UnsetFlags(strTag, Flags);
					}
					strTag = (String) button.getTag(R.string.TagPage);
					displayPage(strTag, false);
				} catch (Exception e) {
					Log.e(TAG, "OnClick Exception ", e);
				}
			}
		};
	}

	// onclick listener for imageview
	// maximises or returns to normal size the image if it is tapped
	View.OnClickListener getOnClickDoZoomImage(final ImageView imageview, final String imgPath) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				try {
					ViewGroup.LayoutParams layout;
					ImageView objImageView;
					objImageView = imageview;
					if (blnImageZoomed) {
						objImageView.setImageBitmap(decodeSampledBitmapFromFile(imgPath, intWidthTop / 2, intHeightTop));
					} else {
						objImageView.setImageBitmap(decodeSampledBitmapFromFile(imgPath, intWidthTop, intHeightTop));
					}
					blnImageZoomed = !blnImageZoomed;
					// Clear the parent layout
					objLayoutImage.removeAllViews();
					// add the new image to it and set it to fill the
					// available area
					objLayoutImage.addView(objImageView);
					layout = objImageView.getLayoutParams();
					layout.width = ViewGroup.LayoutParams.WRAP_CONTENT;
					layout.height = ViewGroup.LayoutParams.FILL_PARENT;
					objImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					objImageView.setLayoutParams(layout);
				} catch (Exception e) {
					Log.e(TAG, "OnClick Exception ", e);
				}
			}
		};
	}

	// Prefernces Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 1, "Load");
		menu.add(Menu.NONE, 1, 2, "Preferences");
		menu.add(Menu.NONE, 2, 3, "Restart");
		if (blnDebug) {
			menu.add(Menu.NONE, 4, 4, "Goto Page");
		}
		menu.add(Menu.NONE, 3, 5, "Exit");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, 0, 1, "Load");
		menu.add(Menu.NONE, 1, 2, "Preferences");
		menu.add(Menu.NONE, 2, 3, "Restart");
		if (blnDebug) {
			menu.add(Menu.NONE, 4, 4, "Goto Page");
		}
		menu.add(Menu.NONE, 3, 5, "Exit");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case 0:
				// Load
				loadFileList();
				removeDialog(DIALOG_LOAD_FILE);
				showDialog(DIALOG_LOAD_FILE);
				// See onCreateDialog further down for what happens when they
				// exit this
				return true;
			case 1:
				// Preferences
				startActivity(new Intent(this, QuickPrefsActivity.class));
				Log.d(TAG, "Edit Pref returning ");
				return true;
			case 2:
				//Restart
				Flags.clear();
				SharedPreferences.Editor objPrefEdit;
				objPrefEdit = objLocalVarianbles.edit();
				objPrefEdit.putString("CurrentPage", "start");
				objPrefEdit.putString("Flags", "");
				objPrefEdit.commit();
				displayPage("start", false);
				return true;
			case 3:
				// Exit
				finish();
				return true;
			case 4:
				// goto page
				showDialog(DIALOG_SHOW_PAGE);
				// See onCreateDialog further down for what happens when they
				// exit this
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "onClick Exception ", e);
		}
		return false;

	}

	public void displayPage(String pageName, Boolean reDisplay) {
		// Main code that displays a page
		String strImage;
		String strHTML;
		String strBtnTarget;
		String strBtnText;
		int intDelSeconds = 0;
		int intPos1;
		int intPos2;
		int intPos3;
		int intMin;
		int intMax;
		int intDpLeft;
		int intBtnLen;
		int intRows;
		String strMin;
		String strMax;
		String strPre;
		String strPost;
		String strPageName;
		String strStartAt;
		String strStopAt;
		String strDelStartAt;
		int intDelStartAt;
		int intStartAt;
		// TODO stop at
		int intStopAt;
		boolean blnVideo;
		boolean blnDelay;
		boolean blnMetronome;
		LinearLayout btnLayoutRow = null;
		LinearLayout btnLayoutRow2 = null;
		LinearLayout.LayoutParams btnLayoutParm;
		ViewGroup.LayoutParams layout;
		String strFlags;
		page objCurrPage;
		delay objDelay;
		audio objAudio;
		video objVideo;
		image objImage;
		button objButton;
		metronome objMetronome;
		
		Log.d(TAG, "displayPage PagePassed " + pageName);
		Log.d(TAG, "displayPage Flags " + comFun.GetFlags(Flags));

		try {
			if(!reDisplay) {
				if (tmrPageTimer != null) {
					tmrPageTimer.cancel();
				}
				if (tmrTetronome != null) {
					tmrTetronome.cancel();
					tmrTetronome = null;
				}
				if (soundPool != null) {
					soundPool.stop(intSoundStream);
				}

				if (mMediaPlayer != null) {
					if (mMediaPlayer.isPlaying()) {
						mMediaPlayer.stop();
						mMediaPlayer.reset();
					}
				}
				
				if (objVideoView != null) {
					objVideoView.stopPlayback();
				}
				
				blnShowGif = false;
			}

			// handle random page
			strPageName = pageName;
			strPre = "";
			strPost = "";
			intPos1 = strPageName.indexOf("(");
			if (intPos1 > -1) {
				intPos2 = strPageName.indexOf("..", intPos1);
				if (intPos2 > -1) {
					intPos3 = strPageName.indexOf(")", intPos2);
					if (intPos3 > -1) {
						strMin = strPageName.substring(intPos1 + 1, intPos2);
						intMin = Integer.parseInt(strMin);
						strMax = strPageName.substring(intPos2 + 2, intPos3);
						intMax = Integer.parseInt(strMax);
						if (intPos1 > 0) {
							strPre = strPageName.substring(0, intPos1);
						} else {
							strPre = "";
						}
						strPost = strPageName.substring(intPos3 + 1);
						Log.d(TAG, "displayPage Random Page Min " + strMin + " Max " + strMax + " Pre " + strPre + " strPost " + strPost);
						String[] strPageArray;
						strPageArray = new String[intMax];
						int intPageArrayCount = -1;
						page tmpPage;
						// Check if we are allowed to display the pages
						for (int i = intMin; i <= intMax; i++) {
							strPageName = strPre + i + strPost;
							if (Ipages.containsKey(strPageName)) {
								tmpPage = Ipages.get(strPageName);
								if (tmpPage.canShow(Flags)) {
									Log.d(TAG, "displayPage PageAllowed " + strPageName + " Yes");
									intPageArrayCount++;
									strPageArray[intPageArrayCount] = strPageName;
								} else {
									Log.d(TAG, "displayPage PageAllowed " + strPageName + " No");
								}
							} else {
								Log.d(TAG, "displayPage PageAllowed " + strPageName + " No");
							}
						}
						int i1 = 0;
						if (intPageArrayCount > 0) {
							// show one of the allowed random pages
							i1 = mRandom.nextInt(intPageArrayCount + 1);
							Log.d(TAG, "random number between 0 and " + intPageArrayCount + " generates " + i1);
						}
						strPageName = strPageArray[i1];
						Log.d(TAG, "displayPage PageChosen " + strPageName);
					}
				}
			}

			// display page
			objCurrPage = Ipages.get(strPageName);
			
			// delay
			objCountText.setText("");
			blnDelay = false;
			intDelSeconds = 1;
			if (objCurrPage.getDelayCount() > 0) {
				try {
					for (int i2 = 0; i2 < objCurrPage.getDelayCount(); i2++) {
						objDelay = objCurrPage.getDelay(i2);
						if (objDelay.canShow(Flags)) {
							blnDelay = true;
							Log.d(TAG, "displayPage Delay");
							strDelStyle = objDelay.getstyle();
							strDelTarget = objDelay.getTarget();
							strDelStartAt = objDelay.getStartWith();
							intDelSeconds = objDelay.getDelaySec();
							try {
								intDelStartAt = Integer.parseInt(strDelStartAt);
							} catch (Exception etemp) {
								intDelStartAt = 0;
							}

							// record any delay set / unset
                            strDelaySet = objDelay.getSet();
                            strDelayUnSet =  objDelay.getUnSet();
                            Log.d(TAG, "displayPage Delay Seconds " + intDelSeconds + " Style " + strDelStyle + " Target " + strDelTarget + " Set " + strDelaySet + " UnSet " + strDelayUnSet);

							if (!reDisplay) {
								if (intDelSeconds == 0) { 
									tmrPageTimer = new PageTimer(10, 0, 10);
									tmrPageTimer.start();
								} else {
									intDelSeconds = intDelSeconds * 1000;
									tmrPageTimer = new PageTimer(intDelSeconds, intDelStartAt, 1000);
									tmrPageTimer.start();
								}
							}
							break;
						} else {
							objCountText.setText("");
						}
					}
				} catch (Exception e1) {
					Log.e(TAG, "displayPage Delay Exception " + e1.getLocalizedMessage());
					objCountText.setText("");
				}
			}


			if (!(intDelSeconds == 0)) { 
				// Video
				blnVideo = false;
				if (objCurrPage.getVideoCount() > 0) {
					for (int i2 = 0; i2 < objCurrPage.getVideoCount(); i2++) {
						objVideo = objCurrPage.getVideo(i2);
						if (objVideo.canShow(Flags)) {
							blnVideo = true;
							objLayoutTop.setBackgroundDrawable(null);
							strImage = objVideo.getIid();
							Log.d(TAG, "displayPage Video " + strImage);
							strStartAt = objVideo.getIstartAt();
							Log.d(TAG, "displayPage Video Start At " + strStartAt);
							intStartAt = 0;
							try {
								if (strStartAt != "") {
									intStartAt = comFun.getMilisecFromTime(strStartAt);
								}
							} catch (Exception e1) {
								intStartAt = 0;
								Log.e(TAG, "displayPage startat Exception " + e1.getLocalizedMessage());
							}

							strStopAt = objVideo.getIstopAt();
							Log.d(TAG, "displayPage Video Stop At " + strStopAt);
							intStopAt = 0;
							try {
								if (strStopAt != "") {
									intStopAt = comFun.getMilisecFromTime(strStopAt);
								}
							} catch (Exception e1) {
								intStopAt = 0;
								Log.e(TAG, "displayPage stopat Exception " + e1.getLocalizedMessage());
							}


							strImage = strImage.replace("\\", "/");
							Log.d(TAG, "displayPage Video " + strImage);
							int intSubDir = strImage.lastIndexOf("/");
							String strSubDir;
							if (intSubDir > -1) {
								strSubDir = strImage.substring(0, intSubDir + 1);
								if (!strSubDir.startsWith("/")) {
									strSubDir = "/" + strSubDir;
								}
								strImage = strImage.substring(intSubDir + 1);
							} else {
								strSubDir = "/";
							}
							// String strSubDir
							// Handle wildcard *
							if (strImage.indexOf("*") > -1) {
								strFilePatern = strImage;
								// get the directory
								File f = new File(strPresentationPath + strMediaDirectory + strSubDir);
								// wildcard filter class handles the filtering
								java.io.FileFilter WildCardfilter = new WildCardFileFilter();
								if (f.isDirectory()) {
									// return a list of matching files
									File[] children = f.listFiles(WildCardfilter);
									// return a random image
									int intFile = rndGen.nextInt(children.length);
									Log.d(TAG, "displayPage Random Video Index " + intFile);
									imgPath = strPresentationPath + strMediaDirectory + strSubDir + children[intFile].getName();
									Log.d(TAG, "displayPage Random Video Chosen " + imgPath);
								}
							} else {
								// no wildcard so just use the file name
								imgPath = strPresentationPath + strMediaDirectory + strSubDir + strImage;
								Log.d(TAG, "displayPage Non Random Video " + imgPath);
							}
							objVideoView = new VideoView(this);
							objLayoutImage.removeAllViews();
							objLayoutImage.addView(objVideoView);
							layout = objVideoView.getLayoutParams();
							//layout.width = (int) (intWidthTop * 0.80);
							layout.width = ViewGroup.LayoutParams.FILL_PARENT;
							layout.height = ViewGroup.LayoutParams.FILL_PARENT;
							try {
								objVideoView.setVideoURI(Uri.parse(imgPath));
								objVideoView.seekTo(intStartAt);
								// if the video has a target create a listener to switch
								// pages after the video
								strVidTarget = objVideo.getItarget();
								if (!strVidTarget.equals("")) {
									Log.d(TAG, "displayPage Video target " + strVidTarget);
									objVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
										public void onCompletion(MediaPlayer mp) {
											Log.d(TAG, "displayPage Video.setOnCompletionListener display " + strVidTarget);
											displayPage(strVidTarget, false);
										}
									});
								}
								// Play video
								new Thread(new Runnable() {
									public void run() {
										objVideoView.start();
									}
								}).start();
							} catch (Exception e1) {
								Log.e(TAG, "displayPage Video Exception " + e1.getLocalizedMessage());
							}
							break;
						}
					}
				} 
				if (!blnVideo) {
					// image
					if (objCurrPage.getImageCount() > 0) {
						for (int i2 = 0; i2 < objCurrPage.getImageCount(); i2++) {
							objImage = objCurrPage.getImage(i2);
							if (objImage.canShow(Flags)) {
								strImage = objImage.getIid();
								strImage = strImage.replace("\\", "/");
								Log.d(TAG, "displayPage Image " + strImage);
								int intSubDir = strImage.lastIndexOf("/");
								String strSubDir;
								if (intSubDir > -1) {
									strSubDir = strImage.substring(0, intSubDir + 1);
									if (!strSubDir.startsWith("/")) {
										strSubDir = "/" + strSubDir;
									}
									strImage = strImage.substring(intSubDir + 1);
								} else {
									strSubDir = "/";
								}
								// String strSubDir
								// Handle wildcard *
								if (strImage.indexOf("*") > -1) {
									strFilePatern = strImage;
									// get the directory
									File f = new File(strPresentationPath + strMediaDirectory + strSubDir);
									// wildcard filter class handles the filtering
									java.io.FileFilter WildCardfilter = new WildCardFileFilter();
									if (f.isDirectory()) {
										// return a list of matching files
										File[] children = f.listFiles(WildCardfilter);
										// return a random image
										int intFile = rndGen.nextInt(children.length);
										Log.d(TAG, "displayPage Random Image Index " + intFile);
										imgPath = strPresentationPath + strMediaDirectory + strSubDir + children[intFile].getName();
										Log.d(TAG, "displayPage Random Image Chosen " + imgPath);
									}
								} else {
									// no wildcard so just use the file name
									imgPath = strPresentationPath + strMediaDirectory + strSubDir + strImage;
									Log.d(TAG, "displayPage Non Random Image " + imgPath);
								}
								flImage = new File(imgPath);
								if (flImage.exists()){
									try {
										if (imgPath.toLowerCase().endsWith(".gif")) {
											if (blnImageBackground) {
												objLayoutImage.removeAllViews();
												new Thread(new Runnable() { 
													public void run() { 
														try {
															String MyGif = imgPath;
															strCurrentGif = imgPath;
															if (mGifDecoder != null) {
																mGifDecoder = null;
																System.gc();
															}
															InputStream stream = null; 
															try { 
																stream =  new BufferedInputStream(new FileInputStream(flImage)); 
															} 
															catch (IOException e) { 
																e.printStackTrace(); 
															}
															mGifDecoder = new GifDecoder();
															mGifDecoder.read(stream);
															final int n = mGifDecoder.getFrameCount(); 
															final int ntimes = mGifDecoder.getLoopCount(); 
															int repetitionCounter = 0; 
															blnShowGif = true;
															do { 
																for (int i = 0; i < n; i++) { 
																	if (blnShowGif && MyGif.equals(imgPath)) {
																		mTmpBitmap = mGifDecoder.getFrame(i); 
																		mTmpBitmap = resizeBitmap(mTmpBitmap, intWidthTop, intHeightTop);
																		final int t = mGifDecoder.getDelay(i); 
																		int intHeightPad = 0;
																		int intWidthPad = 0;
																		if (mTmpBitmap.getHeight() < intHeightTop) {
																			intHeightPad = intHeightTop - mTmpBitmap.getHeight();
																		}
																		if (mTmpBitmap.getWidth() < intWidthTop) {
																			intWidthPad = intWidthTop - mTmpBitmap.getWidth();
																		}
																		Log.d(TAG, "displayPage Pad Image screen height " + intHeightTop);
																		Log.d(TAG, "displayPage Pad Image screen width " + intWidthTop);
																		Log.d(TAG, "displayPage Pad Image bitmap height " + mTmpBitmap.getHeight());
																		Log.d(TAG, "displayPage Pad Image bitmap width " + mTmpBitmap.getWidth());
																		Log.d(TAG, "displayPage Pad Image pad height " + intHeightPad);
																		Log.d(TAG, "displayPage Pad Image pad width " + intWidthPad);
																		if (intHeightPad != 0 || intWidthPad != 0) {
																			mTmpBitmap = pad(mTmpBitmap, intWidthPad, intHeightPad);
																		}
																		mHandler.post(mUpdateResults2); 
																		try { 
																			Thread.sleep(t); 
																		} 
																		catch (InterruptedException e) { 
																			e.printStackTrace();
																		} 
																	}
																} 
																if(ntimes != 0) { 
																	repetitionCounter ++; 
																} 
															} while ((repetitionCounter <= ntimes) && blnShowGif && MyGif.equals(imgPath));
															MyGif = null;
															stream = null;
														} catch (Exception e) {
															Log.e(TAG, "Gif Background Exception " + e.getLocalizedMessage());
														}
													}      
												}).start();
											} else {
												// we create a new image view every time (we may
												// have
												// displayed a video last time)
												objImageView = new ImageView(this);
												// Clear the parent layout
												objLayoutImage.removeAllViews();
												objLayoutTop.setBackgroundDrawable(null);
												// add the new image to it and set it to fill
												// the
												// available area
												objLayoutImage.addView(objImageView);
												layout = objImageView.getLayoutParams();
												layout.width = ViewGroup.LayoutParams.WRAP_CONTENT;
												layout.height = ViewGroup.LayoutParams.FILL_PARENT;
												objImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
												objImageView.setLayoutParams(layout);
												blnImageZoomed = false;
												new Thread(new Runnable() { 
													public void run() { 
														try {
															String MyGif = imgPath;
															strCurrentGif = imgPath;
															if (mGifDecoder != null) {
																mGifDecoder = null;
																System.gc();
															}
															InputStream stream = null; 
															try { 
																stream =  new BufferedInputStream(new FileInputStream(flImage)); 
															} 
															catch (IOException e) { 
																e.printStackTrace(); 
															}
															mGifDecoder = new GifDecoder();
															mGifDecoder.read(stream);
															final int n = mGifDecoder.getFrameCount(); 
															final int ntimes = mGifDecoder.getLoopCount(); 
															int repetitionCounter = 0; 
															blnShowGif = true;
															do { 
																for (int i = 0; i < n; i++) { 
																	if (blnShowGif && MyGif.equals(imgPath)) {
																		mTmpBitmap = mGifDecoder.getFrame(i); 
																		mTmpBitmap = resizeBitmap(mTmpBitmap,  intWidthTop / 2, intHeightTop);
																		final int t = mGifDecoder.getDelay(i); 
																		mHandler.post(mUpdateResults); 
																		try { 
																			Thread.sleep(t); 
																		} 
																		catch (InterruptedException e) { 
																			e.printStackTrace();
																		} 
																	}
																} 
																if(ntimes != 0) { 
																	repetitionCounter ++; 
																} 
															} while ((repetitionCounter <= ntimes) && blnShowGif && MyGif.equals(imgPath)); 
															MyGif = null;
															stream = null;
														} catch (Exception e) {
															Log.e(TAG, "Gif ImageView Exception " + e.getLocalizedMessage());
														}
													}      
												}).start();
												
											}
											
										} else {
											if (blnImageBackground) {
												// decodeSampledBitmapFromFile will resize the
												// image
												// before it gets to memory
												// we can load large images using memory
												// efficiently
												// (and not run out of memory and crash the app)

												// Clear the parent layout
												objLayoutImage.removeAllViews();
												Bitmap objRetBitMap;
												objRetBitMap = decodeSampledBitmapFromFile(imgPath, intWidthTop, intHeightTop);
												int intHeightPad = 0;
												int intWidthPad = 0;
												if (objRetBitMap.getHeight() < intHeightTop) {
													intHeightPad = intHeightTop - objRetBitMap.getHeight();
												}
												if (objRetBitMap.getWidth() < intWidthTop) {
													intWidthPad = intWidthTop - objRetBitMap.getWidth();
												}
												Log.d(TAG, "displayPage Pad Image screen height " + intHeightTop);
												Log.d(TAG, "displayPage Pad Image screen width " + intWidthTop);
												Log.d(TAG, "displayPage Pad Image bitmap height " + objRetBitMap.getHeight());
												Log.d(TAG, "displayPage Pad Image bitmap width " + objRetBitMap.getWidth());
												Log.d(TAG, "displayPage Pad Image pad height " + intHeightPad);
												Log.d(TAG, "displayPage Pad Image pad width " + intWidthPad);
												if (intHeightPad != 0 || intWidthPad != 0) {
													objRetBitMap = pad(objRetBitMap, intWidthPad, intHeightPad);
												}
												Drawable objDrawable = new BitmapDrawable(objRetBitMap);
												objLayoutTop.setBackgroundDrawable(objDrawable);
											} else {
												// we create a new image view every time (we may
												// have
												// displayed a video last time)
												objImageView = new ImageView(this);
												// decodeSampledBitmapFromFile will resize the
												// image
												// before it gets to memory
												// we can load large images using memory
												// efficiently
												// (and not run out of memory and crash the app)

												objImageView.setImageBitmap(decodeSampledBitmapFromFile(imgPath, intWidthTop / 2, intHeightTop));
												// Clear the parent layout
												objLayoutImage.removeAllViews();
												objLayoutTop.setBackgroundDrawable(null);
												// add the new image to it and set it to fill
												// the
												// available area
												objLayoutImage.addView(objImageView);
												layout = objImageView.getLayoutParams();
												layout.width = ViewGroup.LayoutParams.WRAP_CONTENT;
												layout.height = ViewGroup.LayoutParams.FILL_PARENT;
												objImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
												objImageView.setLayoutParams(layout);
												objImageView.setOnClickListener(getOnClickDoZoomImage(objImageView, imgPath));
												blnImageZoomed = false;
											}
										}
									} catch (Exception e1) {
										objLayoutImage.removeAllViews();
										Log.e(TAG, "displayPage Image Exception " + e1.getLocalizedMessage());
									}
								} else {
									// No image
									objLayoutImage.removeAllViews();
									objLayoutTop.setBackgroundDrawable(null);
								}
							} else {
								// No image
								objLayoutImage.removeAllViews();
								objLayoutTop.setBackgroundDrawable(null);
							}
						}
					} else {
						// No image
						objLayoutImage.removeAllViews();
						objLayoutTop.setBackgroundDrawable(null);
					}
				}


				// text
				try {
					String strTemp = objCurrPage.getText();
					strTemp = strTemp.replace("<P>", "");
					strTemp = strTemp.replace("<p>", "");
					strTemp = strTemp.replace("</P>", "<br>");
					strTemp = strTemp.replace("</p>", "<br>");
					strTemp = strTemp.replace("<DIV>", "");
					strTemp = strTemp.replace("<div>", "");
					strTemp = strTemp.replace("</DIV>", "<br>");
					strTemp = strTemp.replace("</div>", "<br>");
					if (strTemp.endsWith("<br>")) {
						strTemp = strTemp.substring(0, strTemp.length() - 4);
					}
					if (!blnImageBackground) {
						strHTML = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html  xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"><head><meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" /><title></title><style type=\"text/css\"> body { color: white; background-color: black; font-family: Tahoma; font-size:"
								+ MintFontSize + "px } </style></head><body>" + strTemp + "</body></html>";
						//objWebView1.loadData(strHTML, "text/html", null);
						objWebView1.loadDataWithBaseURL(null, strHTML, "text/html", null, null);
						objWebView1.setBackgroundColor(color.black);
					} else {

						strHTML = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html  xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"><head><meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" /><title></title><style type=\"text/css\"> body { color: white; background-color: rgba(0,0,0,0); font-family: Tahoma; font-size:"
								+ MintFontSize + "px } div {background-color: rgba(0,0,0,0); position: absolute; bottom: 0; text-shadow: 2px 2px #000000} </style></head><body><div>" + strTemp + "</div></body></html>";
						//objWebView1.loadData(strHTML, "text/html", null);
						objWebView1.loadDataWithBaseURL(null, strHTML, "text/html", null, null);
						objWebView1.setBackgroundColor(0);
					}
				} catch (Exception e1) {
					Log.e(TAG, "displayPage Text Exception " + e1.getLocalizedMessage());
					strHTML = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html  xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"><head><meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" /><title></title><style type=\"text/css\"> body { color: white; background-color: rgba(0,0,0,0); font-family: Tahoma; font-size:"
							+ MintFontSize + "px } div {background-color: rgba(0,0,0,0.25); position: absolute; bottom: 0} </style></head><body><div></div></body></html>";
					objWebView1.loadData(strHTML, "text/html", null);
					objWebView1.setBackgroundColor(0);
				}


				// buttons
				// remove old buttons
				LinearLayout btnLayout = (LinearLayout) findViewById(R.id.btnLayout);
				btnLayout.removeAllViews();

				// add new buttons
				intDpLeft = intBtnLetters;
				btnLayoutRow = new LinearLayout(this);
				btnLayoutRow.setOrientation(LinearLayout.HORIZONTAL);
				intRows = 1;
				btnLayout.addView(btnLayoutRow);
				layout = btnLayoutRow.getLayoutParams();
				layout.height = LayoutParams.WRAP_CONTENT;
				layout.width = LayoutParams.WRAP_CONTENT;
				btnLayoutRow.setLayoutParams(layout);
				btnLayoutRow.setWeightSum(intBtnLetters);
				for (int i1 = objCurrPage.getButtonCount() - 1; i1 >= 0; i1--) {
					try {
						objButton = objCurrPage.getButton(i1);

							if (objButton.canShow(Flags)) {

								strBtnTarget = objButton.getTarget();
								strBtnText = objButton.getText();
								intBtnLen = strBtnText.length();
								if (intBtnLen < 5) {
									intBtnLen = 5;
								}
								Button btnDynamic = new Button(this);
								btnDynamic.setText(strBtnText);
								btnDynamic.setTextSize(MintFontSize);
								btnDynamic.setShadowLayer(9, 1, 1, Color.rgb(44,44,44));
								btnDynamic.setTextColor(Color.WHITE);
								if (intTransparency == 255) {
									btnDynamic.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_black_glossy));
								} else {
									btnDynamic.getBackground().setAlpha(intTransparency);
								}


								if (intDpLeft < intBtnLen) {
									intRows = 2;
									intDpLeft = intBtnLetters;
									btnLayoutRow2 = new LinearLayout(this);
									btnLayoutRow2.setOrientation(LinearLayout.HORIZONTAL);
									layout = btnLayoutRow.getLayoutParams();
									layout.height = LayoutParams.WRAP_CONTENT;
									layout.width = LayoutParams.WRAP_CONTENT;
									btnLayoutRow2.setLayoutParams(layout);
									btnLayoutRow2.setWeightSum(intBtnLetters);
									btnLayout.addView(btnLayoutRow2);
								}
								intDpLeft = intDpLeft - intBtnLen;

								// record any button set / unset
								String strButtonSet;
								String strButtonUnSet;
								strButtonSet = objButton.getSet();
								if (!strButtonSet.equals("")) {
									btnDynamic.setTag(R.string.TagSetFlags, strButtonSet);
								} else {
									btnDynamic.setTag(R.string.TagSetFlags, "");
								}
								strButtonUnSet = objButton.getUnSet();
								if (!strButtonUnSet.equals("")) {
									btnDynamic.setTag(R.string.TagUnSetFlags, strButtonUnSet);
								} else {
									btnDynamic.setTag(R.string.TagUnSetFlags, "");
								}

								Log.d(TAG, "displayPage Button Text " + strBtnText + " Target " + strBtnTarget + " Set " + strButtonSet + " UnSet " + strButtonUnSet);

								btnDynamic.setTag(R.string.TagPage, strBtnTarget);
								btnDynamic.setOnClickListener(getOnClickDoSomething(btnDynamic));
								if (intRows == 1) {
									btnLayoutRow.addView(btnDynamic);
								} else {
									btnLayoutRow2.addView(btnDynamic);
								}
								btnLayoutParm = (android.widget.LinearLayout.LayoutParams) btnDynamic.getLayoutParams();
								btnLayoutParm.width = 0;
								btnLayoutParm.height = LayoutParams.WRAP_CONTENT;
								if (blnImageBackground) {
									btnLayoutParm.weight = intBtnLen;
								} else {
									btnLayoutParm.weight = intBtnLen * 2;
								}
								btnDynamic.setLayoutParams(btnLayoutParm);
							}
					} catch (Exception e1) {
						Log.e(TAG, "displayPage Buttons Exception " + e1.getLocalizedMessage());
					}
				}
				try {
					if (blnDebug) {
						// add a button to trigger the delay target
						if (blnDelay) {
							Button btnDynamic = new Button(this);
							btnDynamic.setText("Delay");
							btnDynamic.setTextSize(MintFontSize);
							btnDynamic.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_default_normal));

							if (intDpLeft < 8) {
								intDpLeft = intBtnLetters;
								btnLayoutRow = new LinearLayout(this);
								layout = btnLayoutRow.getLayoutParams();
								layout.height = LayoutParams.WRAP_CONTENT;
								layout.width = LayoutParams.WRAP_CONTENT;
								btnLayoutRow.setLayoutParams(layout);
								btnLayoutRow.setWeightSum(intBtnLetters);
								btnLayout.addView(btnLayoutRow);

							} else {
								intDpLeft = intDpLeft - 8;
							}

							btnDynamic.setTag(R.string.TagSetFlags, strDelaySet);
							btnDynamic.setTag(R.string.TagUnSetFlags, strDelayUnSet);
							btnDynamic.setTag(R.string.TagPage, strDelTarget);
							btnDynamic.setOnClickListener(getOnClickDoSomething(btnDynamic));
							btnLayoutRow.addView(btnDynamic);

							btnLayoutParm = (android.widget.LinearLayout.LayoutParams) btnDynamic.getLayoutParams();
							btnLayoutParm.width = 0;
							btnLayoutParm.height = LayoutParams.WRAP_CONTENT;
							if (blnImageBackground) {
								btnLayoutParm.weight = 8;
							} else {
								btnLayoutParm.weight = 16;
							}
							btnDynamic.setLayoutParams(btnLayoutParm);
						}
						TextView objDebugText = (TextView) findViewById(R.id.textViewDebug);
						objDebugText.setText(" " + strPageName);
					} else {
						TextView objDebugText = (TextView) findViewById(R.id.textViewDebug);
						objDebugText.setText(strTitle);
					}
				} catch (Exception e1) {
					Log.e(TAG, "displayPage Debug Exception " + e1.getLocalizedMessage());
				}

				if (!reDisplay) {
					// Audio / Metronome
					blnMetronome = false;
					if (objCurrPage.getMetronomeCount() > 0) {
						for (int i2 = 0; i2 < objCurrPage.getMetronomeCount(); i2++) {
							objMetronome = objCurrPage.getMetronome(i2);
							if (objMetronome.canShow(Flags)) {
								blnMetronome = true;
								// Metronome
								int intbpm = objMetronome.getbpm();

								Log.d(TAG, "displayPage Metronome " + intbpm + " BPM");
								intbpm = 60000 / intbpm;
								try {
									tmrTetronome = new Timer();
									tmrTetronome.schedule(new MetronomeTask(), intbpm, intbpm);
								} catch (IllegalArgumentException e) {
									Log.e(TAG, "displayPage IllegalArgumentException ", e);
								} catch (IllegalStateException e) {
									Log.e(TAG, "displayPage IllegalStateException ", e);
								} catch (Exception e) {
									Log.e(TAG, "displayPage Exception ", e);
								}
							}
						}
					}
					if (!blnMetronome) {
						// Audio
						if (objCurrPage.getAudioCount() > 0) {
							for (int i2 = 0; i2 < objCurrPage.getAudioCount(); i2++) {
								objAudio = objCurrPage.getAudio(i2);
								if (objAudio.canShow(Flags)) {
									try {
										String strIntAudio = objAudio.getRepeat();
										if (strIntAudio.equals("")) {
											intAudioLoops = 0;
										} else {
											intAudioLoops = Integer.parseInt(strIntAudio);
										}
										strAudio = objAudio.getIid();
										Log.d(TAG, "displayPage Audio " + strAudio);

										strAudio = strAudio.replace("\\", "/");
										Log.d(TAG, "displayPage Video " + strAudio);
										int intSubDir = strAudio.lastIndexOf("/");
										String strSubDir;
										if (intSubDir > -1) {
											strSubDir = strAudio.substring(0, intSubDir + 1);
											if (!strSubDir.startsWith("/")) {
												strSubDir = "/" + strSubDir;
											}
											strAudio = strAudio.substring(intSubDir + 1);
										} else {
											strSubDir = "/";
										}
										// String strSubDir
										// Handle wildcard *
										if (strAudio.indexOf("*") > -1) {
											strFilePatern = strAudio;
											// get the directory
											File f = new File(strPresentationPath + strMediaDirectory + strSubDir);
											// wildcard filter class handles the filtering
											java.io.FileFilter WildCardfilter = new WildCardFileFilter();
											if (f.isDirectory()) {
												// return a list of matching files
												File[] children = f.listFiles(WildCardfilter);
												// return a random image
												int intFile = rndGen.nextInt(children.length);
												Log.d(TAG, "displayPage Random Video Index " + intFile);
												imgPath = strPresentationPath + strMediaDirectory + strSubDir + children[intFile].getName();
												Log.d(TAG, "displayPage Random Video Chosen " + imgPath);
											}
										} else {
											// no wildcard so just use the file name
											imgPath = strPresentationPath + strMediaDirectory + strSubDir + strAudio;
											Log.d(TAG, "displayPage Non Random Video " + imgPath);
										}
										strAudio = imgPath;
										strAudioTarget = objAudio.getTarget();
										Log.d(TAG, "displayPage Audio target " + strAudioTarget);
										// run audio on another thread
										new Thread(new Runnable() {
											public void run() {
												mMediaPlayer = new MediaPlayer();
												try {
													mMediaPlayer.setDataSource(strAudio);
													mMediaPlayer.prepare();
													// if we have a target or a number of loops do some additional processing
													if (!strAudioTarget.equals("") || intAudioLoops > 0) {
														Log.d(TAG, "displayPage Audio.setOnCompletionListener set target " + strAudioTarget + " loops " + intAudioLoops);
														//set a listener for the end of the audio
														mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
															public void onCompletion(MediaPlayer mp) {
																//if we still need to loop play it again
																if (intAudioLoops > 0) {
																	Log.d(TAG, "displayPage Audio.setOnCompletionListener Loop " + intAudioLoops);
																	intAudioLoops = intAudioLoops - 1;
																	//restart the audio
																	mMediaPlayer.stop();
																	mMediaPlayer.start();
																} else {
																	//if we don't need to loop and we have a target display the target page
																	if (!strAudioTarget.equals("")) {
																		Log.d(TAG, "displayPage Audio.setOnCompletionListener display " + strAudioTarget);
																		displayPage(strAudioTarget, false);
																	}
																}
															}
														});
													}
												} catch (IllegalArgumentException e) {
													Log.e(TAG, "displayPage IllegalArgumentException ", e);
												} catch (IllegalStateException e) {
													Log.e(TAG, "displayPage IllegalStateException ", e);
												} catch (IOException e) {
													Log.e(TAG, "displayPage IOException ", e);
												}
												Log.d(TAG, "displayPage Audio Start");
												//start the audio
												mMediaPlayer.start();
											}
										}).start();
									} catch (Exception e1) {
										Log.e(TAG, "displayPage Audio Exception " + e1.getLocalizedMessage());
									}
								}
							}
						}
					}
				}
			}

			// Save current page and flags
			// set page
			if (blnAutoSetPage) {
				Flags.add(strPageName);
			}
			// do page set / unset
			try {
				objCurrPage.setUnSet(Flags);
			} catch (Exception e1) {
				Log.e(TAG, "displayPage PageFlags Exception " + e1.getLocalizedMessage());
			}
			SharedPreferences.Editor objPrefEdit;
			objPrefEdit = objLocalVarianbles.edit();
			objPrefEdit.putString("CurrentPage", strPageName);
			strFlags = comFun.GetFlags(Flags);
			Log.d(TAG, "displayPage End Flags " + strFlags);
			objPrefEdit.putString("Flags", strFlags);
			objPrefEdit.commit();

		} catch (Exception e) {
			Log.e(TAG, "displayPage Exception ", e);
		}
	}

	public enum TagName
	{
		Title, Author, MediaDirectory, Settings, Page, Metronome, Image, Audio, Video, Delay, Button, Text, NOVALUE;

	    public static TagName toTag(String str)
	    {
	        try {
	            return valueOf(str);
	        } 
	        catch (Exception ex) {
	            return NOVALUE;
	        }
	    }   
	}	
	
	public String loadXML(String xmlFileName) {
		String strTmpTitle = "";
		String strTmpAuthor = "";
		String strPage = "start";
		String strFlags;
		String strPreXMLPath;
		String pageName; 
		String ifSet; 
		String ifNotSet; 
		String Set;
		String UnSet;
		String strTag;
		page Ipage = null;
		
		try {
			objLocalVarianbles = getSharedPreferences(xmlFileName, MODE_PRIVATE);
			Flags.clear();
			Ipages.clear();
			blnAutoSetPage = true;
			strPreXMLPath = strPresentationPath + xmlFileName;
			
	         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	         factory.setNamespaceAware(true);
	         XmlPullParser xpp = factory.newPullParser();
	         File preXMLFile = new File(strPreXMLPath);
	         FileInputStream fis = new FileInputStream(preXMLFile);
	         UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
	         ubis.skipBOM();
	         xpp.setInput(new InputStreamReader(ubis));
	         
	         int eventType = xpp.getEventType();

	         while (eventType != XmlPullParser.END_DOCUMENT) {
	        	 switch (eventType) {
	        	 case XmlPullParser.START_DOCUMENT:
	        		 Log.d(TAG, "loadXML Start document " + strPreXMLPath);
	        		 break;
	        	 case XmlPullParser.END_DOCUMENT:
	        		 Log.d(TAG, "loadXML End document");
	        		 break;
	        	 case XmlPullParser.START_TAG:
	        		 Log.d(TAG, "loadXML Start tag " + xpp.getName());
	        		 strTag = xpp.getName();

	        		 switch (TagName.toTag(strTag)) {
	        		 case Title:
	        			 try {
	        				 xpp.next();
	        				 strTmpTitle = xpp.getText();
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Title Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Audio:
	        			 try {
	        				 String strId;
	        				 String strStartAt;
	        				 String strStopAt;
	        				 String strTarget;
	        				 strTarget = xpp.getAttributeValue(null, "target");
	        				 if (strTarget == null) strTarget = "";
	        				 strStartAt = xpp.getAttributeValue(null, "start-at");
	        				 if (strStartAt == null) strStartAt = "";
	        				 strStopAt = xpp.getAttributeValue(null, "stop-at");
	        				 if (strStopAt == null) strStopAt = "";
	        				 strId = xpp.getAttributeValue(null, "id");
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 audio iaudio = new audio(strId, strStartAt, strStopAt, strTarget, strTarget, ifSet, ifNotSet, "", "");
	        				 Ipage.addAudio(iaudio);
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Audio Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Author:
	        			 try {
	        				 int eventType2 = xpp.getEventType();
	        				 while (true) {
	        					 if (eventType2 == XmlPullParser.START_TAG) {
	        						 if (xpp.getName().equals("Name")) {
	        							 xpp.next();
	        							 strTmpAuthor = xpp.getText();
	        						 }
	        					 }
	        					 eventType2 = xpp.next();
	        					 if (eventType2 == XmlPullParser.END_TAG) {
	        						 if (xpp.getName().equals("Author")) break;
	        					 }
	        				 }
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Author Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Button:
	        			 try {
	        				 String strTarget;
	        				 strTarget = xpp.getAttributeValue(null, "target");
	        				 if (strTarget == null) strTarget = "";
	        				 Set = xpp.getAttributeValue(null, "set");
	        				 if (Set == null) Set = "";
	        				 UnSet = xpp.getAttributeValue(null, "unset");
	        				 if (UnSet == null) UnSet = "";
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 xpp.next();
	        				 button ibutton = new button(strTarget, xpp.getText(), ifSet, ifNotSet, Set, UnSet);
	        				 Ipage.addButton(ibutton);
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Button Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Delay:
	        			 try {
	        				 String strSeconds;
	        				 String strStartWith;
	        				 String strStyle;
	        				 String strTarget;
	        				 strTarget = xpp.getAttributeValue(null, "target");
	        				 if (strTarget == null) strTarget = "";
	        				 strStartWith = xpp.getAttributeValue(null, "start-with");
	        				 if (strStartWith == null) strStartWith = "";
	        				 strStyle = xpp.getAttributeValue(null, "style");
	        				 if (strStyle == null) strStyle = "";
	        				 strSeconds = xpp.getAttributeValue(null, "seconds");
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 Set = xpp.getAttributeValue(null, "set");
	        				 if (Set == null) Set = "";
	        				 UnSet = xpp.getAttributeValue(null, "unset");
	        				 if (UnSet == null) UnSet = "";
	        				 delay idelay = new delay(strTarget, strSeconds, ifSet, ifNotSet, strStartWith, strStyle, Set, UnSet);
	        				 Ipage.addDelay(idelay);
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Delay Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Image:
	        			 try {
	        				 String strImage;
	        				 strImage = xpp.getAttributeValue(null, "id");
	        				 if (strImage == null) strImage = "";
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 if (!strImage.equals("")){
	        					 image Iimage = new image(strImage, ifSet, ifNotSet);
	        					 Ipage.addImage(Iimage);
	        				 }
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Image Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case MediaDirectory:
	        			 try {
	        				 xpp.next();
	        				 strMediaDirectory = xpp.getText();
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML MediaDirectory Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Metronome:
	        			 try {
	        				 String strbpm;
	        				 strbpm = xpp.getAttributeValue(null, "bpm");
	        				 if (strbpm == null) strbpm = "";
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 if (!strbpm.equals("")) {
	        					 metronome Imetronome = new metronome(strbpm, ifSet, ifNotSet);
	        					 Ipage.addMetronome(Imetronome);
	        				 }
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Metronome Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case NOVALUE:
	        			 break;
	        		 case Page:
	        			 try {
	        				 pageName = xpp.getAttributeValue(null, "id");
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 Set = xpp.getAttributeValue(null, "set");
	        				 if (Set == null) Set = "";
	        				 UnSet = xpp.getAttributeValue(null, "unset");
	        				 if (UnSet == null) UnSet = "";
	        				 Ipage = new page(pageName, ifSet, ifNotSet, Set, UnSet, blnAutoSetPage); 
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Page Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Settings:
	        			 try {
	        				 int eventType2 = xpp.getEventType();
	        				 while (true) {
	        					 if (eventType2 == XmlPullParser.START_TAG) {
	        						 if (xpp.getName().equals("AutoSetPageWhenSeen")) {
	        							 xpp.next();
	        							 blnAutoSetPage = Boolean.parseBoolean(xpp.getText());
	        						 }	        				 
	        					 }
        						 eventType2 = xpp.next();
        						 if (eventType2 == XmlPullParser.END_TAG) {
        							 if (xpp.getName().equals("Settings")) break;
        						 }
	        				 }
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Settings Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Text:
	        			 try {
	        				 if (xpp.getName().equals("Text")) {
	        					 String text = "";
	        					 String tag = "";
	        					 int eventType2 = xpp.next();
	        					 while (true) {
	        						 switch (eventType2) {
	        						 case XmlPullParser.START_TAG:
	        							 tag = xpp.getName();
	        							 text = text + "<" + tag;
	        							 for (int i=0; i < xpp.getAttributeCount(); i++) {
	        								 text = text + " " + xpp.getAttributeName(i) + "=\"" + xpp.getAttributeValue(i) + "\"";
	        							 }
	        							 text = text + ">";
	        							 break;
	        						 case XmlPullParser.END_TAG:
	        							 text = text + "</"  + tag + ">";
	        							 break;
	        						 case XmlPullParser.TEXT:
	        							 text = text + xpp.getText();
	        							 break;
	        						 }
	        						 eventType2 = xpp.next();
	        						 if (eventType2 == XmlPullParser.END_TAG) {
	        							 if (xpp.getName().equals("Text")) break;
	        						 }
	        					 }
	        					 Ipage.setText(text);
	        				 }
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Text Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 case Video:
	        			 try {
	        				 String strId;
	        				 String strStartAt;
	        				 String strStopAt;
	        				 String strTarget;
	        				 strTarget = xpp.getAttributeValue(null, "target");
	        				 if (strTarget == null) strTarget = "";
	        				 strStartAt = xpp.getAttributeValue(null, "start-at");
	        				 if (strStartAt == null) strStartAt = "";
	        				 strStopAt = xpp.getAttributeValue(null, "stop-at");
	        				 if (strStopAt == null) strStopAt = "";
	        				 strId = xpp.getAttributeValue(null, "id");
	        				 ifSet = xpp.getAttributeValue(null, "if-set");
	        				 if (ifSet == null) ifSet = "";
	        				 ifNotSet = xpp.getAttributeValue(null, "if-not-set"); 
	        				 if (ifNotSet == null) ifNotSet = "";
	        				 video ivideo = new video(strId, strStartAt, strStopAt, strTarget, ifSet, ifNotSet, "", "", "");
	        				 Ipage.addVideo(ivideo);
	        			 } catch (Exception e1) {
	        				 Log.e(TAG, "loadXML Video Exception " + e1.getLocalizedMessage());
	        			 }
	        			 break;
	        		 default:
	        			 break;
	        		 }
	        		 break;
	        	 case XmlPullParser.END_TAG:
	        		 Log.d(TAG, "loadXML End tag " + xpp.getName());
	        		 try {
	        			 if (xpp.getName().equals("Page")) {
	        				 Ipages.put(Ipage.getPageName(), Ipage);
	        			 }
	        		 } catch (Exception e1) {
	        			 Log.e(TAG, "loadXML EndPage Exception " + e1.getLocalizedMessage());
	        		 }
	        		 break;
	        	 case XmlPullParser.TEXT:
	        		 break;
	        	 }

	        	 eventType = xpp.next();
	         }
	         
			strTitle = strTmpTitle + ", " + strTmpAuthor;

			//Metronome sound
			soundPool = null;
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
			sound = soundPool.load(this, R.raw.tick, 1);

			// Return to where we left off
			strPage = "start";
			try {
				strPage = objLocalVarianbles.getString("CurrentPage", "start");
				strFlags = objLocalVarianbles.getString("Flags", "");
				if (strFlags != "") {
					comFun.SetFlags(strFlags, Flags);
				}
			} catch (Exception e1) {
				Log.e(TAG, "loadXML Continue Exception " + e1.getLocalizedMessage());
			}

		} catch (org.xmlpull.v1.XmlPullParserException e) {
			Log.e(TAG, "loadXML XmlPullParserException ", e);
		} catch (IOException e) {
			Log.e(TAG, "loadXML IOException ", e);
		} catch (Exception e) {
			Log.e(TAG, "loadXML Exception ", e);
		}
		return strPage;
	}


	// delay timer
	class PageTimer extends CountDownTimer {
		Long lngStartOffset;
		
		public PageTimer(long millisInFuture, long offset, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			lngStartOffset = offset;
		}

		// display the target page
		@Override
		public void onFinish() {
			try {
				objCountText.setText("");
				// do set / unset
				if (!strDelaySet.equals("")) {
					Log.d(TAG, "PageTimer  onFinish Set Flags " + strDelaySet);
					comFun.SetFlags(strDelaySet, Flags);
				}
				if (!strDelayUnSet.equals("")) {
					Log.d(TAG, "PageTimer  onFinish UnSet Flags " + strDelayUnSet);
					comFun.UnsetFlags(strDelayUnSet, Flags);
				}
				Log.d(TAG, "PageTimer  onFinish Target " + strDelTarget);
				displayPage(strDelTarget, false);
			} catch (Exception e) {
				Log.e(TAG, "onFinish Exception ", e);
			}
		}

		// update the clock
		// normal display minutes and seconds
		// secret display ??:??
		// hidden don't display
		@Override
		public void onTick(long millisUntilFinished) {
			try {
				if (strDelStyle.equals("normal")) {
					Long tmpLong = (Long) millisUntilFinished / 1000;
					tmpLong = tmpLong + lngStartOffset;
					Long tmpMinutes = (Long) tmpLong / 60;
					Long tmpSeconds = (Long) tmpLong - (tmpMinutes * 60);
					String strTime = tmpSeconds.toString();
					if (strTime.length() < 2) {
						strTime = "0" + strTime;
					}
					strTime = tmpMinutes.toString() + ":" + strTime;
					objCountText.setText(strTime);

				} else if (strDelStyle.equals("secret")) {
					objCountText.setText(R.string.timerSecret);
				} else {
					objCountText.setText("");
				}
			} catch (Exception e) {
				Log.e(TAG, "PageTimer.onTick Exception ", e);
			}
		}
	}

	// orientation change or keyboard change
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation/keyboard change
		super.onConfigurationChanged(newConfig);
		// Get the size of the screen in pixels
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		intHeightTop = dm.heightPixels;
		intWidthTop = dm.widthPixels;

		if (objLocalVarianbles!=null){
			String strPage = objLocalVarianbles.getString("CurrentPage", "start");
			displayPage(strPage, true);
		}

	}

	private int intSoundStream;

	// called from the metronome timer plays the tick
	private class MetronomeTask extends TimerTask {
		@Override
		public void run() {
			try {
				new Thread(new Runnable() {
					public void run() {
						intSoundStream = soundPool.play(sound, 1.0f, 1.0f, 0, 0, 1.0f);
					}
				}).start();
			} catch (Exception e) {
				Log.e(TAG, "MetronomeTask.run Exception ", e);
			}
		}
	}

	// Get xml files
	private String[] mFileList;
	private String mChosenFile;
	private static final String FTYPE = ".xml";
	private static final int DIALOG_LOAD_FILE = 1000;
	private static final int DIALOG_PASSWORD_ENTER = 1001;
	private static final int DIALOG_SHOW_PAGE = 1002;

	private void loadFileList() {
		File[] FileList;
		try {
			if (objPresFolder.exists()) {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						@SuppressWarnings("unused")
						File sel = new File(dir, filename);
						return filename.contains(FTYPE);
					}
				};
				FileList = objPresFolder.listFiles(filter);
				Arrays.sort(FileList, new Comparator<File>() {
					public int compare(File f1, File f2) {
						int intRetval;
						if (strLoadSortOrder.equals("DATE")) {
							intRetval = Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
						} else if (strLoadSortOrder.equals("SIZE")){
							intRetval = Long.valueOf(f2.length()).compareTo(f1.length());
						} else {
							intRetval = f1.getName().toUpperCase().compareTo(f2.getName().toUpperCase());
						}
						return intRetval;
					}
				});
				mFileList = new String[FileList.length];
				for (int i = 0; i < FileList.length; i++)
					mFileList[i] = FileList[i].getName();
			} else {
				mFileList = new String[0];
			}
		} catch (Exception e) {
			Log.e(TAG, "loadFileList Exception ", e);
		}
	}

	private Dialog dialog = null;

	// private boolean blnReturn;;

	// File Load and Password dialogs
	protected Dialog onCreateDialog(int id) {
		try {
			AlertDialog.Builder builder = new Builder(this);
			switch (id) {
			case DIALOG_LOAD_FILE:
				builder.setTitle("Choose your file");
				if (mFileList == null) {
					dialog = builder.create();
					return dialog;
				}
				builder.setItems(mFileList, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String strPage;
						mChosenFile = mFileList[which];
						strPage = loadXML(mChosenFile);
						displayPage(strPage, false);
					}
				});
				dialog = builder.show();
				break;
			case DIALOG_PASSWORD_ENTER:
				dialog = new Dialog(this);
				dialog.setCancelable(false);
				dialog.setContentView(R.layout.passwordentry);
				dialog.setTitle("Enter Password");
				Button objButtonPwd = (Button) dialog.findViewById(R.id.btnPasswordOk);
				objButtonPwd.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						EditText edit = (EditText) dialog.findViewById(R.id.editTextPassword);
						String text = edit.getText().toString();
						dialog.dismiss();
						strPasswordEntered = text;
						if (!strPasswordEntered.equals(strPrefPassword)) {
							// If the password is wrong exit the app
							finish();
						}
					}
				});
				break;
			case DIALOG_SHOW_PAGE:
				dialog = new Dialog(this);
				dialog.setCancelable(false);
				dialog.setContentView(R.layout.pageentry);
				dialog.setTitle("Enter Page");
				Button objButtonPage = (Button) dialog.findViewById(R.id.btnPageOk);
				objButtonPage.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						EditText edit = (EditText) dialog.findViewById(R.id.editTextPage);
						String text = edit.getText().toString();
						dialog.dismiss();
						displayPage(text, false);
					}
				});
				break;
			}
		} catch (Exception e) {
			Log.e(TAG, "onCreateDialog Exception ", e);
		}
		return dialog;
	}

	// Wildecard filter
	private class WildCardFileFilter implements java.io.FileFilter {
		public boolean accept(File f) {
			try {
				String strPattern = strFilePatern.toLowerCase();
				String text = f.getName().toLowerCase();
				String strFile = text;
				strPattern = strPattern.replace("*", ".*");
				if (!text.matches(strPattern)) {
					Log.d(TAG, "WildCardFileFilter accept No Match " + strFile);
					return false;
				}
				
				Log.d(TAG, "WildCardFileFilter accept Match " + strFile);
				return true;
			} catch (Exception e) {
				Log.e(TAG, "WildCardFileFilter.accept Exception ", e);
				return false;
			}
		}
	}

	// return the image resized to the correct size to display
	public Bitmap decodeSampledBitmapFromFile(String strFile, int reqWidth, int reqHeight) {
		try {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			Bitmap objBitMap;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(strFile, options);

			int inSampleSize = 1;
			try {
				// Raw height and width of image
				final int height = options.outHeight;
				final int width = options.outWidth;

				if (width > reqWidth) {
					inSampleSize = Math.round((float) width / (float) reqWidth);
				} else if (height > reqHeight) {
					inSampleSize = Math.round((float) height / (float) reqHeight);
				}

				// Calculate inSampleSize
				options.inSampleSize = inSampleSize;

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;

				objBitMap = BitmapFactory.decodeFile(strFile, options);
				objBitMap = resizeBitmap(objBitMap,reqWidth,reqHeight);
				Log.d(TAG, "decodeSampledBitmapFromFile reqWidth " + reqWidth + " reqHeight " + reqHeight + " height " + height + " width " + width + " inSampleSize " + inSampleSize  + " BitMap Height " + objBitMap.getHeight() + " width " + objBitMap.getWidth());
			} catch (Exception e) {
				Log.e(TAG, "calculateInSampleSize Exception ", e);
				return null;
			}

			return objBitMap;
		} catch (Exception e) {
			Log.e(TAG, "decodeSampledBitmapFromFile Exception ", e);
			return null;
		}
	}
	
	public Bitmap resizeBitmap(Bitmap bitmapin, int reqWidth, int reqHeight ){
		try {
			float fltRatio;
			float fltScrnRatio;
			fltRatio = (float) bitmapin.getHeight() / (float) bitmapin.getWidth();
			fltScrnRatio = (float) reqHeight / (float) reqWidth;
			if (fltScrnRatio > fltRatio) {
				bitmapin = Bitmap.createScaledBitmap(bitmapin, reqWidth, (int) (reqWidth * fltRatio), true);
			} else {
				bitmapin = Bitmap.createScaledBitmap(bitmapin, (int) (reqHeight / fltRatio), reqHeight, true);
			}
			return bitmapin;
		} catch (Exception e) {
			Log.e(TAG, "resizeBitmap Exception ", e);
			return bitmapin;
		}
	}

	public Bitmap pad(Bitmap Src, int padding_x, int padding_y) {
		try {
			Bitmap outputimage = Bitmap.createBitmap(Src.getWidth() + padding_x, Src.getHeight() + padding_y, Bitmap.Config.ARGB_8888);
			Canvas can = new Canvas(outputimage);
			can.drawARGB(0, 0, 0, 0);
			can.drawBitmap(Src, (padding_x / 2), 0, null);

			return outputimage;
		} catch (Exception e) {
			Log.e(TAG, "pad Exception ", e);
			return Src;
		}
	}

	
	final Runnable mUpdateResults = new Runnable() { 
		public void run() { 
			if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
				try {
					objImageView.setImageBitmap(mTmpBitmap); 
				} catch (Exception e) {
					Log.e(TAG, "mUpdateResults Exception ", e);
				}
			} 
		} 
	};

	final Runnable mUpdateResults2 = new Runnable() { 
		public void run() { 
			if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
				try {
					Drawable objDrawable = new BitmapDrawable(mTmpBitmap);
					objLayoutTop.setBackgroundDrawable(objDrawable);
				} catch (Exception e) {
					Log.e(TAG, "mUpdateResults Exception ", e);
				}
			}
		}
	};

}
