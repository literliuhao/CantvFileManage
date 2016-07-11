package com.cantv.media.center.data;

import java.util.LinkedList;
import java.util.List;

public class LyricInfo {

	private String title;
	private String singer;
	private String album;
	private long duration;
	private List<LyricInfo.Lyric> lyrics;

	public LyricInfo() {
		lyrics = new LinkedList<LyricInfo.Lyric>();
	}
	
	public String getLyricStrAt(int index){
		if(lyrics == null || index >= lyrics.size() || index < 0){
			return "";
		}
		return lyrics.get(index).getLyric();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public List<LyricInfo.Lyric> getLyrics() {
		return lyrics;
	}

	public void setLyrics(List<LyricInfo.Lyric> lyrics) {
		this.lyrics = lyrics;
	}

	public void addLyric(LyricInfo.Lyric lyric) {
		lyrics.add(lyric);
	}

	@Override
	public String toString() {
		return "LyricInfo [title=" + title + ", singer=" + singer + ", album=" + album + ", duration=" + duration
				+ ", lyrics=" + lyrics + "]";
	}

	/**
	 * 单句歌词
	 * 
	 * @author zhangbingyuan
	 */
	public static class Lyric implements Comparable<Lyric> {
		private long startTime;
		private long endTime;
		private long duration;
		private String lyric;

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public long getEndTime() {
			return endTime;
		}

		public void setEndTime(long endTime) {
			this.endTime = endTime;
			this.duration = endTime - startTime;
		}

		public long getDuration() {
			return duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}

		public String getLyric() {
			return lyric;
		}

		public void setLyric(String lyric) {
			this.lyric = lyric;
		}

		@Override
		public String toString() {
			return "Lyric [startTime=" + startTime + ", endTime=" + endTime + ", duration=" + duration + ", lyric="
					+ lyric + "]";
		}

		@Override
		public int compareTo(Lyric another) {
			if (another == null) {
				return -1;
			}
			long anotherStartTime = another.getStartTime();
			return this.startTime > anotherStartTime ? 1 : (this.startTime < anotherStartTime ? -1 : 0);
		}
	}

	public boolean isLegal() {
		return lyrics != null && lyrics.size() > 0;
	}

}
