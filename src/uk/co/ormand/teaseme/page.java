package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class page {
	private String IText;
	private String IpageName;
	private ArrayList<button> IButton;
	private ArrayList<delay> IDelay;
	private ArrayList<video> IVideo;
	private ArrayList<image> IImage;
	private ArrayList<audio> IAudio;
	private ArrayList<metronome> IMetronome;
	private int IButtonCount;
	private int IDelayCount;
	private int IVideoCount;
	private int IImageCount;
	private int IAudioCount;
	private int IMetronomeCount;
	private String Iifset;
	private String IifNotSet;
	private String ISet;
	private String IUnSet;
	private comonFunctions comFun;
	private static final String TAG = "ATM";
	
	public page(String pageName, String ifSet, String ifNotSet, String Set, String UnSet, boolean autoSet) {
		IpageName = pageName;
		IButton = new ArrayList<button>();
		IButtonCount = 0;
		IDelay = new ArrayList<delay>();
		IDelayCount = 0;
		IVideo = new ArrayList<video>();
		IVideoCount = 0;
		IImage = new ArrayList<image>();
		IImageCount = 0;
		IAudio = new ArrayList<audio>();
		IAudioCount = 0;
		IMetronome = new ArrayList<metronome>();
		IMetronomeCount = 0;
		Iifset = ifSet;
		IifNotSet = ifNotSet;
		ISet = Set;
		IUnSet = UnSet;
		
		if (autoSet) {
			if (ISet.length() == 0) {
				ISet = pageName;
			} else {
				ISet = ISet + "," + pageName;
			}
		}
		comFun = new comonFunctions(TAG);
	}

	public button getButton(int butIndex) {
		return IButton.get(butIndex);
	}

	public void addButton(button iButton) {
		IButton.add(iButton);
		IButtonCount++;
	}

	public delay getDelay(int delIndex) {
		return IDelay.get(delIndex);
	}

	public void addDelay(delay iDelay) {
		IDelay.add(iDelay);
		IDelayCount++;
	}

	public video getVideo(int VidIndex) {
		return IVideo.get(VidIndex);
	}
	
	public void addVideo(video iVideo) {
		IVideo.add(iVideo);
		IVideoCount++;
	}

	public image getImage(int ImgIndex) {
		return IImage.get(ImgIndex);
	}
	
	public void addImage(image iImage) {
		IImage.add(iImage);
		IImageCount++;
	}

	public audio getAudio(int AudIndex) {
		return IAudio.get(AudIndex);
	}
	
	public void addAudio(audio iAudio) {
		IAudio.add(iAudio);
		IAudioCount++;
	}

	public metronome getMetronome(int MetIndex) {
		return IMetronome.get(MetIndex);
	}
	
	public void addMetronome(metronome iMetronome) {
		IMetronome.add(iMetronome);
		IMetronomeCount++;
	}

	public String getPageName() {
		return IpageName;
	}

	public int getButtonCount() {
		return IButtonCount;
	}

	public int getDelayCount() {
		return IDelayCount;
	}

	public int getVideoCount() {
		return IVideoCount;
	}

	public int getImageCount() {
		return IImageCount;
	}

	public int getMetronomeCount() {
		return IMetronomeCount;
	}

	public boolean canShow(ArrayList<String> setList) {
		return comFun.canShow(setList, Iifset, IifNotSet, IpageName);
	}

	public void setUnSet(ArrayList<String> setList) {
		comFun.SetFlags(ISet, setList);
		comFun.UnsetFlags(IUnSet, setList);
	}

	@Override
	public String toString() {
		return "page [Page Name=" + IpageName + "]";
	}

	public int getAudioCount() {
		return IAudioCount;
	}

	public String getText() {
		return IText;
	}

	public void setText(String iText) {
		IText = iText;
	}
	
	

}
