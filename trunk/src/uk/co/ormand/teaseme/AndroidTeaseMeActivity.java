package uk.co.ormand.teaseme;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	private DocumentBuilderFactory objDocumentBuilderFactory;
	private DocumentBuilder objDocumentBuilder;
	private String strMediaDirectory;
	private Document objDocPresXML;
	private Element objPagesElement;
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
	private List<String> Flags;
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
			objClock.setTextSize(MintFontSize);
			if (blnImageBackground) {
				objClock.setBackgroundColor(Color.parseColor("#00000000"));
			} else {
				objClock.setBackgroundColor(color.black);
			}

			// Text control that contains page name in debug mode or description
			// if not
			TextView objDebug = (TextView) findViewById(R.id.textViewDebug);
			objDebug.setTextSize(MintFontSize);
			if (blnImageBackground) {
				objDebug.setBackgroundColor(Color.parseColor("#00000000"));
			} else {
				objDebug.setBackgroundColor(color.black);
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
			Flags = new ArrayList<String>();
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
			mMediaPlayer.stop();
			mMediaPlayer.reset();
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
			Flags = new ArrayList<String>();
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			blnDebug = sharedPrefs.getBoolean("Debug", false);
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
			objClock.setTextSize(MintFontSize);
			if (blnImageBackground) {
				objClock.setBackgroundColor(Color.parseColor("#00000000"));
			} else {
				objClock.setBackgroundColor(color.black);
			}

			// Text control that contains page name in debug mode or description
			// if not
			TextView objDebug = (TextView) findViewById(R.id.textViewDebug);
			objDebug.setTextSize(MintFontSize);
			if (blnImageBackground) {
				objDebug.setBackgroundColor(Color.parseColor("#00000000"));
			} else {
				objDebug.setBackgroundColor(color.black);
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
					SetFlags(strFlags);
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
						SetFlags(strTag);
					}
					strTag = (String) button.getTag(R.string.TagUnSetFlags);
					if (!strTag.equals("")) {
						UnsetFlags(strTag);
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
				Flags = new ArrayList<String>();
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
		String strId;
		Element elPage;
		Element elImage;
		Element elDelay = null;
		Element elButton;
		Element elMetronome;
		Element elAudio;
		Node elText;
		String strImage;
		String strHTML;
		String strDelSeconds;
		String strBtnTarget;
		String strBtnText;
		int intDelSeconds;
		int intPos1;
		int intPos2;
		int intPos3;
		int intMin;
		int intMax;
		int intDpLeft;
		int intBtnLen;
		// *int intRows;
		String strMin;
		String strMax;
		String strPre;
		String strPost;
		String strPageName;
		String strSet;
		String strTest;
		String strStartAt;
		String strStopAt;
		String strDelStartAt;
		int intDelStartAt;
		int intStartAt;
		int intStopAt;
		boolean blnTestSet;
		boolean blnTestNotSet;
		NodeList pageNodeList;
		NodeList tmpNodeList;
		LinearLayout btnLayoutRow = null;
		LinearLayout.LayoutParams btnLayoutParm;
		ViewGroup.LayoutParams layout;
		ImageView objImageView;
		final VideoView objVideoView;
		String imgPath = null;
		String strFlags;
		String strHour;
		String strMinute;
		String strSecond;
		Log.d(TAG, "displayPage PagePassed " + pageName);
		Log.d(TAG, "displayPage Flags " + GetFlags());

		try {
			if(!reDisplay) {
				if (tmrPageTimer != null) {
					tmrPageTimer.cancel();
				}
				if (tmrTetronome != null) {
					tmrTetronome.cancel();
					tmrTetronome = null;
				}

				soundPool.stop(intSoundStream);
				if (mMediaPlayer != null) {
					mMediaPlayer.stop();
					mMediaPlayer.reset();
				}
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
						// Check if we are allowed to display the pages
						for (int i = intMin; i <= intMax; i++) {
							strPageName = strPre + i + strPost;
							if (AllowedToShowPage(strPageName)) {
								Log.d(TAG, "displayPage PageAllowed " + strPageName + " Yes");
								intPageArrayCount++;
								strPageArray[intPageArrayCount] = strPageName;
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

			// set page
			if (blnAutoSetPage) {
				Flags.add(strPageName);
			}

			// display page
			pageNodeList = objPagesElement.getElementsByTagName("Page");
			// loop through till we find the page
			for (int i = 0; i < pageNodeList.getLength(); i++) {
				elPage = (Element) pageNodeList.item(i);
				strId = elPage.getAttribute("id");
				if (strId.equals(strPageName)) {
					// found the page so display it

					// do page set / unset
					try {
						strSet = elPage.getAttribute("set");
						if (!strSet.equals("")) {
							SetFlags(strSet);
							Log.d(TAG, "displayPage PageSet " + strSet);
						}
						strSet = elPage.getAttribute("unset");
						if (!strSet.equals("")) {
							UnsetFlags(strSet);
							Log.d(TAG, "displayPage PageUnSet " + strSet);
						}
					} catch (Exception e1) {
						Log.e(TAG, "displayPage PageFlags Exception " + e1.getLocalizedMessage());
					}

					// can't have a video and an image
					// Video
					tmpNodeList = elPage.getElementsByTagName("Video");
					elImage = (Element) tmpNodeList.item(0);
					if (elImage != null) {
						strImage = elImage.getAttribute("id");
						Log.d(TAG, "displayPage Video " + strImage);
						strStartAt = elImage.getAttribute("start-at");
						Log.d(TAG, "displayPage Video Start At " + strStartAt);
						intStartAt = 0;
						try {
							if (strStartAt != "") {
								intPos1 = strStartAt.indexOf(":");
								if (intPos1 > -1) {
									intPos2 = strStartAt.indexOf(":", intPos1 + 1);
									if (intPos2 > -1) {
										strHour = strStartAt.substring(0, intPos1);
										strMinute = strStartAt.substring(intPos1 + 1, intPos2);
										strSecond = strStartAt.substring(intPos2 + 1, strStartAt.length());
										Log.d(TAG, "displayPage Video Start At Hour " + strHour + " Minute " + strMinute + " Second " + strSecond);
										intStartAt = Integer.parseInt(strSecond) * 1000;
										intStartAt = intStartAt + Integer.parseInt(strMinute) * 1000 * 60;
										intStartAt = intStartAt + Integer.parseInt(strHour) * 1000 * 60 * 60;
									}
								}
							}
						} catch (Exception e1) {
							intStartAt = 0;
							Log.e(TAG, "displayPage startat Exception " + e1.getLocalizedMessage());
						}

						strStopAt = elImage.getAttribute("stop-at");
						Log.d(TAG, "displayPage Video Stop At " + strStopAt);
						intStopAt = 0;
						try {
							if (strStopAt != "") {
								intPos1 = strStopAt.indexOf(":");
								if (intPos1 > -1) {
									intPos2 = strStopAt.indexOf(":", intPos1 + 1);
									if (intPos2 > -1) {
										strHour = strStopAt.substring(0, intPos1);
										strMinute = strStopAt.substring(intPos1 + 1, intPos2);
										strSecond = strStopAt.substring(intPos2 + 1, strStopAt.length());
										Log.d(TAG, "displayPage Video Stop At Hour " + strHour + " Minute " + strMinute + " Second " + strSecond);
										intStopAt = Integer.parseInt(strSecond) * 1000;
										intStopAt = intStopAt + Integer.parseInt(strMinute) * 1000 * 60;
										intStopAt = intStopAt + Integer.parseInt(strHour) * 1000 * 60 * 60;
									}
								}
							}
						} catch (Exception e1) {
							intStartAt = 0;
							Log.e(TAG, "displayPage stopat Exception " + e1.getLocalizedMessage());
						}

						
						imgPath = strPresentationPath + strMediaDirectory + "/" + strImage;
						Log.d(TAG, "displayPage Video full path " + imgPath);
						objVideoView = new VideoView(this);
						objLayoutImage.removeAllViews();
						objLayoutImage.addView(objVideoView);
						layout = objVideoView.getLayoutParams();
						layout.width = (int) (intWidthTop * 0.80);
						layout.height = ViewGroup.LayoutParams.FILL_PARENT;
						try {
							objVideoView.setVideoURI(Uri.parse(imgPath));
							objVideoView.seekTo(intStartAt);
							// if the video has a target create a listener to switch
							// pages after the video
							strVidTarget = elImage.getAttribute("target");
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
					} else {
						// image
						tmpNodeList = elPage.getElementsByTagName("Image");
						elImage = (Element) tmpNodeList.item(0);
						if (elImage != null) {

							strImage = elImage.getAttribute("id");
							strImage = strImage.replace("\\", "/");
							Log.d(TAG, "displayPage Image " + strImage);
							int intSubDir = strImage.lastIndexOf("/");
							String strSubDir;
							if (intSubDir > -1) {
								strSubDir = "/" + strImage.substring(0, intSubDir + 1);
								strImage = strImage.substring(intSubDir + 1);
							} else {
								strSubDir = "";
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
									imgPath = strPresentationPath + strMediaDirectory + strSubDir + "/" + children[intFile].getName();
									Log.d(TAG, "displayPage Random Image Chosen " + imgPath);
								}
							} else {
								// no wildcard so just use the file name
								imgPath = strPresentationPath + strMediaDirectory + strSubDir + "/" + strImage;
								Log.d(TAG, "displayPage Non Random Image " + imgPath);
							}
							File flImage = new File(imgPath);
							if (flImage.exists()){
								try {
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
								} catch (Exception e1) {
									objLayoutImage.removeAllViews();
									Log.e(TAG, "displayPage Image Exception " + e1.getLocalizedMessage());
								}
							} else {
								// No image
								objLayoutImage.removeAllViews();
							}
						} else {
							// No image
							objLayoutImage.removeAllViews();
						}
					}

					// text
					tmpNodeList = elPage.getElementsByTagName("Text");
					try {
						elText = tmpNodeList.item(0);
						String strTemp = getInnerXml(elText, true);
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

					// delay
					//
					strDelaySet = "";
					strDelayUnSet = "";
					tmpNodeList = elPage.getElementsByTagName("Delay");
					elDelay = (Element) tmpNodeList.item(0);

						try {
							if (elDelay != null) {
								Log.d(TAG, "displayPage Delay");
								// test to see if we need this delay
								blnTestSet = true;
								blnTestNotSet = true;
								strTest = elDelay.getAttribute("if-set");
								if (!strTest.equals("")) {
									blnTestSet = MatchesIfSetCondition(strTest);
									Log.d(TAG, "displayPage Delay if-set" + strTest);
								}
								strTest = elDelay.getAttribute("if-not-set");
								if (!strTest.equals("")) {
									blnTestNotSet = MatchesIfNotSetCondition(strTest);
									Log.d(TAG, "displayPage Delay if-not-set" + strTest);
								}

								if (blnTestSet && blnTestNotSet) {
									strDelSeconds = elDelay.getAttribute("seconds");
									strDelStyle = elDelay.getAttribute("style");
									strDelTarget = elDelay.getAttribute("target");
									strDelStartAt = elDelay.getAttribute("start-with");
									try {
										intDelStartAt = Integer.parseInt(strDelStartAt);
									} catch (Exception etemp) {
										intDelStartAt = 0;
									}
									// record any delay set / unset
									strSet = elDelay.getAttribute("set");
									if (!strSet.equals("")) {
										strDelaySet = strSet;
									}
									strSet = elDelay.getAttribute("unset");
									if (!strSet.equals("")) {
										strDelayUnSet = strSet;
									}
									Log.d(TAG, "displayPage Delay Seconds " + strDelSeconds + " Style " + strDelStyle + " Target " + strDelTarget + " Set " + strDelaySet + " UnSet " + strDelayUnSet);

									// handle random Delay
									intPos1 = strDelSeconds.indexOf("(");
									intDelSeconds = 0;
									if (intPos1 > -1) {
										intPos2 = strDelSeconds.indexOf("..", intPos1);
										if (intPos2 > -1) {
											intPos3 = strDelSeconds.indexOf(")", intPos2);
											if (intPos3 > -1) {
												strMin = strDelSeconds.substring(intPos1 + 1, intPos2);
												intMin = Integer.parseInt(strMin);
												strMax = strDelSeconds.substring(intPos2 + 2, intPos3);
												intMax = Integer.parseInt(strMax);
												int i1 = mRandom.nextInt(intMax - intMin) + intMin;
												intDelSeconds = i1;
												Log.d(TAG, "displayPage Random Delay Min " + strMin + " Max " + strMax + " Chosen " + intDelSeconds);
											}
										}
									} else {
										intDelSeconds = Integer.parseInt(strDelSeconds);
									}
									if (!reDisplay) {
										if (intDelSeconds == 0) { 
											tmrPageTimer = new PageTimer(250, 0, 250);
											tmrPageTimer.start();
										} else {
											intDelSeconds = intDelSeconds * 1000;
											tmrPageTimer = new PageTimer(intDelSeconds, intDelStartAt, 1000);
											tmrPageTimer.start();
										}
									}
								}
							} else {
								objCountText.setText("");
							}
						} catch (Exception e1) {
							Log.e(TAG, "displayPage Delay Exception " + e1.getLocalizedMessage());
							objCountText.setText("");
						}
					
					// buttons
					// remove old buttons
					LinearLayout btnLayout = (LinearLayout) findViewById(R.id.btnLayout);
					btnLayout.removeAllViews();

					// add new buttons
					tmpNodeList = elPage.getElementsByTagName("Button");
					intDpLeft = intBtnLetters;
					btnLayoutRow = new LinearLayout(this);
					btnLayout.addView(btnLayoutRow);
					layout = btnLayoutRow.getLayoutParams();
					layout.height = LayoutParams.WRAP_CONTENT;
					layout.width = LayoutParams.WRAP_CONTENT;
					btnLayoutRow.setLayoutParams(layout);
					btnLayoutRow.setWeightSum(intBtnLetters);
					for (int i1 = tmpNodeList.getLength() - 1; i1 >= 0; i1--) {
						try {
							elButton = (Element) tmpNodeList.item(i1);

							if (elButton != null) {

								// test to see if we need this button
								blnTestSet = true;
								blnTestNotSet = true;
								strTest = elButton.getAttribute("if-set");
								if (!strTest.equals("")) {
									blnTestSet = MatchesIfSetCondition(strTest);
									Log.d(TAG, "displayPage Button if-set " + strTest);
								} else {
									strTest = elButton.getAttribute("if-not-set");
									if (!strTest.equals("")) {
										blnTestNotSet = MatchesIfNotSetCondition(strTest);
										Log.d(TAG, "displayPage Button if-not-set " + strTest);
									}
								}

								if (blnTestSet && blnTestNotSet) {

									strBtnTarget = elButton.getAttribute("target");

									StringBuffer buffer = new StringBuffer();
									NodeList childList = elButton.getChildNodes();
									for (int i11 = 0; i11 < childList.getLength(); i11++) {
										Node child = childList.item(i11);
										if (child.getNodeType() == Node.TEXT_NODE) {
											buffer.append(child.getNodeValue());
										}
									}
									strBtnText = buffer.toString();
									intBtnLen = strBtnText.length();
									if (intBtnLen < 5) {
										intBtnLen = 5;
									}
									Button btnDynamic = new Button(this);
									btnDynamic.setText(strBtnText);
									btnDynamic.setTextSize(MintFontSize);
									//btnDynamic.setShadowLayer(2, 1, 1, 0xffffff);
									btnDynamic.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_black_glossy));
									btnDynamic.setTextColor(Color.WHITE);

									if (intDpLeft < intBtnLen) {
										intDpLeft = intBtnLetters;
										btnLayoutRow = new LinearLayout(this);
										layout = btnLayoutRow.getLayoutParams();
										layout.height = LayoutParams.WRAP_CONTENT;
										layout.width = LayoutParams.WRAP_CONTENT;
										btnLayoutRow.setLayoutParams(layout);
										btnLayoutRow.setWeightSum(intBtnLetters);
										btnLayout.addView(btnLayoutRow);
									}
									intDpLeft = intDpLeft - intBtnLen;

									// record any button set / unset
									String strButtonSet;
									String strButtonUnSet;
									strButtonSet = elButton.getAttribute("set");
									if (!strButtonSet.equals("")) {
										btnDynamic.setTag(R.string.TagSetFlags, strButtonSet);
									} else {
										btnDynamic.setTag(R.string.TagSetFlags, "");
									}
									strButtonUnSet = elButton.getAttribute("unset");
									if (!strButtonUnSet.equals("")) {
										btnDynamic.setTag(R.string.TagUnSetFlags, strButtonUnSet);
									} else {
										btnDynamic.setTag(R.string.TagUnSetFlags, "");
									}

									Log.d(TAG, "displayPage Button Text " + strBtnText + " Target " + strBtnTarget + " Set " + strButtonSet + " UnSet " + strButtonUnSet);

									btnDynamic.setTag(R.string.TagPage, strBtnTarget);
									btnDynamic.setOnClickListener(getOnClickDoSomething(btnDynamic));
									btnLayoutRow.addView(btnDynamic);
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
							}
						} catch (Exception e1) {
							Log.e(TAG, "displayPage Buttons Exception " + e1.getLocalizedMessage());
						}
					}
					try {
						if (blnDebug) {
							// add a button to trigger the delay target
							if (elDelay != null) {
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
						tmpNodeList = elPage.getElementsByTagName("Metronome");
						elMetronome = (Element) tmpNodeList.item(0);
						if (elMetronome != null) {
							// Metronome
							String strbpm = elMetronome.getAttribute("bpm");
							int intbpm = Integer.parseInt(strbpm);
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
						} else {
							// Audio
							tmpNodeList = elPage.getElementsByTagName("Audio");
							elAudio = (Element) tmpNodeList.item(0);
							if (elAudio != null) {
								try {
									String strIntAudio = elAudio.getAttribute("repeat");
									if (strIntAudio.equals("")) {
										intAudioLoops = 0;
									} else {
										intAudioLoops = Integer.parseInt(strIntAudio);
									}
									strAudio = elAudio.getAttribute("id");
									Log.d(TAG, "displayPage Audio " + strAudio);
									strAudioTarget = elImage.getAttribute("target");
									Log.d(TAG, "displayPage Audio target " + strAudioTarget);
									// run audio on another thread
									new Thread(new Runnable() {
										public void run() {
											mMediaPlayer = new MediaPlayer();
											try {
												mMediaPlayer.setDataSource(strPresentationPath + strMediaDirectory + "/" + strAudio);
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
					// found the right page and processed it so break out of the loop
					break;
				}
			}

			// Save current page and flags
			SharedPreferences.Editor objPrefEdit;
			objPrefEdit = objLocalVarianbles.edit();
			objPrefEdit.putString("CurrentPage", strPageName);
			strFlags = GetFlags();
			Log.d(TAG, "displayPage End Flags " + strFlags);
			objPrefEdit.putString("Flags", strFlags);
			objPrefEdit.commit();

		} catch (Exception e) {
			Log.e(TAG, "displayPage Exception ", e);
		}
	}

	public String loadXML(String xmlFileName) {
		NodeList objNodeList;
		Element objMediaElement;
		Element objAutoset;
		Element objTitle;
		Element objSettings;
		Node objTemp;
		Element objAuthor;
		String strTmpTitle;
		String strTmpAuthor;
		String strPage = "start";
		String strFlags;
		String strPreXMLPath;

		try {
			objLocalVarianbles = getSharedPreferences(xmlFileName, MODE_PRIVATE);
			Flags = new ArrayList<String>();
			strPreXMLPath = strPresentationPath + xmlFileName;
			File preXMLFile = new File(strPreXMLPath);
			objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
			objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
			objDocPresXML = objDocumentBuilder.parse(preXMLFile);
			objDocPresXML.getDocumentElement().normalize();

			try {
				objNodeList = objDocPresXML.getElementsByTagName("Settings");
				objSettings = (Element) objNodeList.item(0);
				if (objSettings == null) {
					blnAutoSetPage = true;
				} else {
					// AutoSetPageWhenSeen
					objNodeList = objSettings.getElementsByTagName("AutoSetPageWhenSeen");
					objAutoset = (Element) objNodeList.item(0);

					if (objAutoset == null) {
						blnAutoSetPage = true;
					} else {
						blnAutoSetPage = Boolean.parseBoolean(objAutoset.getFirstChild().getNodeValue());
					}
				}
			} catch (Exception e1) {
				blnAutoSetPage = true;
				Log.e(TAG, "loadXML Settings Exception " + e1.getLocalizedMessage());
			}

			// Return to where we left off
			strPage = "start";
			try {
				strPage = objLocalVarianbles.getString("CurrentPage", "start");
				strFlags = objLocalVarianbles.getString("Flags", "");
				if (strFlags != "") {
					SetFlags(strFlags);
				}
			} catch (Exception e1) {
				Log.e(TAG, "loadXML Continue Exception " + e1.getLocalizedMessage());
			}

			// Title
			try {
				objNodeList = objDocPresXML.getElementsByTagName("Title");
				objTitle = (Element) objNodeList.item(0);
				if (objTitle == null) {
					strTmpTitle = "";
				} else {
					objTemp = objTitle.getFirstChild();
					if (objTemp == null) {
						strTmpTitle = "";
					} else {
						strTmpTitle = objTitle.getFirstChild().getNodeValue();
					}
				}
			} catch (Exception e1) {
				strTmpTitle = "";
				Log.e(TAG, "loadXML Title Exception " + e1.getLocalizedMessage());
			}

			// Author
			try {
				objNodeList = objDocPresXML.getElementsByTagName("Author");
				objAuthor = (Element) objNodeList.item(0);
				if (objAuthor == null) {
					strTmpAuthor = "";
				} else {
					objNodeList = objAuthor.getElementsByTagName("Name");
					objAuthor = (Element) objNodeList.item(0);
					if (objAuthor == null) {
						strTmpAuthor = "";
					} else {
						objTemp = objAuthor.getFirstChild();
						if (objTemp == null) {
							strTmpAuthor = "";
						} else {
							strTmpAuthor = objAuthor.getFirstChild().getNodeValue();
						}
					}
				}
			} catch (Exception e1) {
				strTmpAuthor = "";
				Log.e(TAG, "loadXML Author Exception " + e1.getLocalizedMessage());
			}

			strTitle = strTmpTitle + ", " + strTmpAuthor;

			// Media directory
			objNodeList = objDocPresXML.getElementsByTagName("MediaDirectory");
			objMediaElement = (Element) objNodeList.item(0);
			strMediaDirectory = objMediaElement.getFirstChild().getNodeValue();

			// Node holding all pages
			objNodeList = objDocPresXML.getElementsByTagName("Pages");
			objPagesElement = (Element) objNodeList.item(0);

			//Metronome sound
			soundPool = null;
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
			sound = soundPool.load(this, R.raw.tick, 1);
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "loadXML ParserConfigurationException ", e);
		} catch (SAXException e) {
			Log.e(TAG, "loadXML SAXException ", e);
		} catch (IOException e) {
			Log.e(TAG, "loadXML IOException ", e);
		} catch (Exception e) {
			Log.e(TAG, "loadXML Exception ", e);
		}
		return strPage;
	}

	private String getInnerXml(Node objXMLNode, boolean blnTopNode) {
		// Helper function to return the xml below a node as text
		// Used to get the html from the text node as a string
		String strXML;
		String strTemp;
		String strAttributes;
		Node objTmpElement;
		NodeList tmpNodeList;
		NamedNodeMap objAttr;
		Node objAttrNode;
		strXML = "";
		try {
			if (objXMLNode != null) {
				if (objXMLNode.getNodeType() == Node.ELEMENT_NODE) {
					if (!blnTopNode) {
						if (objXMLNode.hasAttributes()){
							strAttributes = "";
							objAttr = objXMLNode.getAttributes();
							for (int i = 0; i < objAttr.getLength(); i++){
								objAttrNode = objAttr.item(i);
								strAttributes = strAttributes + " " + objAttrNode.getNodeName() + "=\"" + objAttrNode.getNodeValue() + "\"";
							}
						} else {
							strAttributes = "";
						}
						strXML = "<" + objXMLNode.getNodeName() + strAttributes + ">";
					}
					tmpNodeList = objXMLNode.getChildNodes();
					for (int i = 0; i < tmpNodeList.getLength(); i++) {
						objTmpElement = tmpNodeList.item(i);
						strXML = strXML + getInnerXml(objTmpElement, false);
					}
					if (!blnTopNode) {
						strXML = strXML + "</" + objXMLNode.getNodeName() + ">";
					}
				}
				if (objXMLNode.getNodeType() == Node.TEXT_NODE) {
					strTemp = objXMLNode.getNodeValue();
					strTemp = strTemp.replace("%", "&#37;");
					strXML = strXML + strTemp;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "getInnerXml Exception ", e);
		}
		return strXML;
	}

	// delay timer
	class PageTimer extends CountDownTimer {
		Long lngStartOffset;
		
		public PageTimer(long millisInFuture, long offset, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			lngStartOffset = offset;
			//Long tmpLong = (Long) millisInFuture / 1000;
			//objCountText.setText(tmpLong.toString());
		}

		// display the target page
		@Override
		public void onFinish() {
			try {
				objCountText.setText("");
				// do set / unset
				if (!strDelaySet.equals("")) {
					Log.d(TAG, "PageTimer  onFinish Set Flags " + strDelaySet);
					SetFlags(strDelaySet);
				}
				if (!strDelayUnSet.equals("")) {
					Log.d(TAG, "PageTimer  onFinish UnSet Flags " + strDelayUnSet);
					UnsetFlags(strDelayUnSet);
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
				/*
				String[] cards = strPattern.split("\\*");
				String text = f.getName().toLowerCase();
				String strFile = text;

				// Iterate over the cards.
				for (String card : cards) {
					int idx = text.indexOf(card);

					// Card not detected in the text.
					if (idx == -1) {
						Log.d(TAG, "WildCardFileFilter accept No Match " + strFile);
						return false;
					}

					// Move ahead, towards the right of the text.
					text = text.substring(idx + card.length());
				}
				*/
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
	public static Bitmap decodeSampledBitmapFromFile(String strFile, int reqWidth, int reqHeight) {
		try {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			Bitmap objBitMap;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(strFile, options);
			float fltRatio;
			float fltScrnRatio;

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
				fltRatio = (float) objBitMap.getHeight() / (float) objBitMap.getWidth();
				fltScrnRatio = (float) reqHeight / (float) reqWidth;
				Log.d(TAG, "decodeSampledBitmapFromFile reqWidth " + reqWidth + " reqHeight " + reqHeight + " height " + height + " width " + width + " inSampleSize " + inSampleSize  + " fltRatio " + fltRatio);
				/*if (fltScrnRatio > fltRatio) {
					// portrait
					objBitMap = Bitmap.createScaledBitmap(objBitMap, (int) (reqHeight / fltRatio), reqHeight, false);
				} else {
				*/
					// landscape
					if (fltScrnRatio > fltRatio) {
						objBitMap = Bitmap.createScaledBitmap(objBitMap, reqWidth, (int) (reqWidth * fltRatio), false);
					} else {
						objBitMap = Bitmap.createScaledBitmap(objBitMap, (int) (reqHeight / fltRatio), reqHeight, false);
					}
				//}
				Log.d(TAG, "decodeSampledBitmapFromFile reqWidth " + reqWidth + " reqHeight " + reqHeight + " height " + height + " width " + width + " inSampleSize " + inSampleSize  + " fltRatio " + fltRatio + " BitMap Height " + objBitMap.getHeight() + " width " + objBitMap.getWidth());
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

	public Bitmap pad(Bitmap Src, int padding_x, int padding_y) {
		Bitmap outputimage = Bitmap.createBitmap(Src.getWidth() + padding_x, Src.getHeight() + padding_y, Bitmap.Config.ARGB_8888);
		Canvas can = new Canvas(outputimage);
		can.drawARGB(0, 0, 0, 0);
		can.drawBitmap(Src, (padding_x / 2), 0, null);

		return outputimage;
	}

	// functions to handle set flags go here
	private void SetFlags(String flagNames) {
		String[] flags;
		try {
			flags = flagNames.split(",", -1);
			Log.d(TAG, "SetFlags before " + Flags + " add " + flagNames);
			for (int i = 0; i < flags.length; i++) {
				if (!Flags.contains(flags[i])) {
					Flags.add(flags[i]);
				}
			}
			Log.d(TAG, "SetFlags after " + Flags);
		} catch (Exception e) {
			Log.e(TAG, "SetFlags Exception ", e);
		}
	}

	private String GetFlags() {
		String strFlags = "";
		try {
			for (int i = 0; i < Flags.size(); i++) {
				strFlags = strFlags + "," + Flags.get(i);
			}

		} catch (Exception e) {
			Log.e(TAG, "SetFlags Exception ", e);
		}
		Log.d(TAG, "GetFlags " + strFlags);
		return strFlags;
	}

	private void UnsetFlags(String flagNames) {
		String[] flags;
		try {
			flags = flagNames.split(",", -1);
			Log.d(TAG, "UnsetFlags remove " + Flags + " add " + flagNames);
			for (int i = 0; i < flags.length; i++) {
				if (Flags.contains(flags[i]) && !flags[i].equals("")) {
					Flags.remove(flags[i]);
				}
			}
			flags = flagNames.split(",", -1);
			Log.d(TAG, "UnsetFlags after " + Flags);
		} catch (Exception e) {
			Log.e(TAG, "UnsetFlags Exception ", e);
		}
	}

	private boolean MatchesIfSetCondition(String condition) {
		boolean blnReturn = false;
		boolean blnAnd = false;
		boolean blnOr = false;
		String[] conditions;

		Log.d(TAG, "MatchesIfSetCondition flags " + Flags + " condition " + condition);
		try {
			if (condition.indexOf("|") > -1) {
				blnOr = true;
				condition = condition.replace("|", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (Flags.contains(conditions[i])) {
						blnReturn = true;
						break;
					}
				}
			}

			if (condition.indexOf("+") > -1) {
				blnAnd = true;
				blnReturn = true;
				condition = condition.replace("+", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (!Flags.contains(conditions[i])) {
						blnReturn = false;
						break;
					}
				}
			}

			if (!blnAnd && !blnOr) {
				blnReturn = Flags.contains(condition);
			}
		} catch (Exception e) {
			Log.e(TAG, "MatchesIfSetCondition Exception ", e);
		}

		Log.d(TAG, "MatchesIfSetCondition returned " + blnReturn);
		return blnReturn;
	}

	private boolean MatchesIfNotSetCondition(String condition) {
		boolean blnReturn = false;
		boolean blnAnd = false;
		boolean blnOr = false;
		String[] conditions;

		Log.d(TAG, "MatchesIfNotSetCondition flags " + Flags + " condition " + condition);
		try {
			if (condition.indexOf("+") > -1) {
				blnAnd = true;
				blnReturn = true;
				condition = condition.replace("+", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (Flags.contains(conditions[i])) {
						blnReturn = false;
						break;
					}
				}
			}

			if (condition.indexOf("|") > -1) {
				blnOr = true;
				condition = condition.replace("|", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (!Flags.contains(conditions[i])) {
						blnReturn = true;
						break;
					}
				}
			}

			if (!blnAnd && !blnOr) {
				blnReturn = !Flags.contains(condition);
			}
		} catch (Exception e) {
			Log.e(TAG, "MatchesIfNotSetCondition Exception ", e);
		}

		Log.d(TAG, "MatchesIfNotSetCondition returned " + blnReturn);
		return blnReturn;
	}

	private boolean AllowedToShowPage(String pageId) {
		NodeList pageNodeList = objPagesElement.getElementsByTagName("Page");
		Element elPage;
		String strTest;
		boolean blnCanShow = false;
		boolean blnSet = true;
		boolean blnNotSet = true;

		Log.d(TAG, "AllowedToShowPage " + pageId);

		try {
			// loop through till we find the page
			for (int i = 0; i < pageNodeList.getLength(); i++) {
				elPage = (Element) pageNodeList.item(i);
				String strId = elPage.getAttribute("id");
				if (strId.equals(pageId)) {
					// found the page so check it
					Log.d(TAG, "AllowedToShowPage found " + pageId);
					strTest = elPage.getAttribute("if-set");
					if (!strTest.equals("")) {
						blnSet = MatchesIfSetCondition(strTest);
					}
					strTest = elPage.getAttribute("if-not-set");
					if (!strTest.equals("")) {
						blnNotSet = MatchesIfNotSetCondition(strTest);
					}
					if (blnSet && blnNotSet) {
						blnCanShow = MatchesIfNotSetCondition(pageId);
					} else {
						blnCanShow = false;
					}
					break;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "AllowedToShowPage Exception ", e);
		}
		Log.d(TAG, "AllowedToShowPage returned " + blnCanShow);
		return blnCanShow;
	}
	
}
