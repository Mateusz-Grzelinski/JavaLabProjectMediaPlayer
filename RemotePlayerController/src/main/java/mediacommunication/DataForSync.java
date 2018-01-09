/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediacommunication;

import java.io.Serializable;

/**
 *
 * @author mat
 */
public class DataForSync implements Serializable{

	private double seekSliderMaxVal = 100;
	private double currentTime = 0;	// in seconds
	private double currentRate = 1;
	private boolean playing = false;
	private boolean fullscreen = false;
	private boolean openrequest = false;
	private String currentTitle = "";
	private double currentVolume = 100;
	private double skip = 0; //time to skip

	/**
	 * @return the seekSliderMaxVal
	 */
	public double getSeekSliderMaxVal() {
		return seekSliderMaxVal;
	}

	/**
	 * @param seekSliderMaxVal the seekSliderMaxVal to set
	 */
	public void setSeekSliderMaxVal(double seekSliderMaxVal) {
		this.seekSliderMaxVal = seekSliderMaxVal;
	}

	/**
	 * @return the currentTime
	 */
	public double getCurrentTime() {
		return currentTime;
	}

	/**
	 * @param currentTime the currentTime to set
	 */
	public void setCurrentTime(double currentTime) {
		this.currentTime = currentTime;
	}

	/**
	 * @return the currentRate
	 */
	public double getCurrentRate() {
		return currentRate;
	}

	/**
	 * @param currentRate the currentRate to set
	 */
	public void setCurrentRate(double currentRate) {
		if (currentRate<2.5 && currentRate > 0) {
			this.currentRate = currentRate;
		}
	}

	/**
	 * @return the isPlaying
	 */
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * @param playing the isPlaying to set
	 */
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	/**
	 * @return the currentTitle
	 */
	public String getCurrentTitle() {
		return currentTitle;
	}

	/**
	 * @param currentTitle the currentTitle to set
	 */
	public void setCurrentTitle(String currentTitle) {
		this.currentTitle = currentTitle;
	}

	/**
	 * @param currentVolume the currentVolume to set
	 */
	public void setCurrentVolume(double currentVolume) {
		this.currentVolume = currentVolume;
	}

	/**
	 * @return the currentVolume
	 */
	public double getCurrentVolume() {
		return currentVolume;
	}

	/**
	 * @return the skip
	 */
	public double getSkip() {
		return skip;
	}

	/**
	 * @param skip the skip to set
	 */
	public void setSkip(double skip) {
		this.skip = skip;
	}

	/**
	 * @return the isFullscreen
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}

	/**
	 * @param fullscreen the isFullscreen to set
	 */
	public void setFullscreen(boolean fullscreen) {
		this.fullscreen = fullscreen;
	}

	@Override
	public String toString() {
		return "Sending: " 
			+ getCurrentTitle() + " rate: " +
			+ getCurrentRate() + "current time" +
			+ getCurrentTime() + "volume: " +
			+ getCurrentVolume()  + "seek slider max: " +
			+ getSeekSliderMaxVal() 
			+ getSkip()
			+ isFullscreen()
			+ isPlaying();
	}

	public void reset() {
		setCurrentTime(-1);
		setSkip(0);
		setOpenrequest(false);
	}

	/**
	 * @return the openrequest
	 */
	public boolean isOpenrequest() {
		return openrequest;
	}

	/**
	 * @param openrequest the openrequest to set
	 */
	public void setOpenrequest(boolean openrequest) {
		this.openrequest = openrequest;
	}


	
	
}
