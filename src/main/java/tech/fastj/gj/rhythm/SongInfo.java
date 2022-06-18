package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.Keys;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.TreeMap;

public class SongInfo implements GeneralSongInfo {

    private String songName;
    private double bpm;
    private double[] notes;
    private int[] noteLanes;
    private TreeMap<Integer, Keys> laneKeys;
    int nextIndex;
    private int beatPeekCount;
    private double firstBeatOffset;
    private String musicPath;

    public SongInfo() {
    }

    public SongInfo(String songName, double bpm, int beatPeekCount, double firstBeatOffset, double[] notes, int[] noteLanes, TreeMap<Integer, Keys> laneKeys, String musicPath) {
        this.songName = songName;
        this.bpm = bpm;
        this.notes = notes;
        this.noteLanes = noteLanes;
        this.laneKeys = laneKeys;
        this.musicPath = musicPath;
        this.nextIndex = 0;
        this.beatPeekCount = beatPeekCount;
        this.firstBeatOffset = firstBeatOffset;
    }

    @Override
    public String getSongName() {
        return songName;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public double getBpm() {
        return bpm;
    }

    public double getFirstBeatOffset() {
        return firstBeatOffset;
    }

    public double getNote(int index) {
        return notes[index];
    }

    public int getNoteLane(int index) {
        return noteLanes[index];
    }

    public Collection<Keys> getLaneKeys() {
        return Collections.unmodifiableCollection(laneKeys.values());
    }

    public Keys getLaneKey(int lane) {
        return laneKeys.get(lane);
    }

    public int getNotesLength() {
        return notes.length;
    }

    public int getBeatPeekCount() {
        return beatPeekCount;
    }

    @Override
    public int getNextIndex() {
        return nextIndex;
    }

    @Override
    public void incrementNextIndex() {
        nextIndex++;
    }

    @Override
    public void resetNextIndex() {
        nextIndex = 0;
    }

    public int findIndex(double beat, double maxBeatDistance) {
        if (beat + maxBeatDistance >= notes[notes.length - 1]) {
            return -1;
        }

        for (int i = 0; i < notes.length; i++) {
            if (beat <= notes[i]) {
                FastJEngine.log("{} <= {}", beat, notes[i]);
                return i;
            }
        }

        return notes.length - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SongInfo songInfo = (SongInfo) o;
        return Double.compare(songInfo.bpm, bpm) == 0
                && beatPeekCount == songInfo.beatPeekCount
                && Double.compare(songInfo.firstBeatOffset, firstBeatOffset) == 0
                && Arrays.equals(notes, songInfo.notes)
                && Arrays.equals(noteLanes, songInfo.noteLanes)
                && Objects.equals(laneKeys, songInfo.laneKeys)
                && Objects.equals(musicPath, songInfo.musicPath);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(bpm, laneKeys, nextIndex, beatPeekCount, firstBeatOffset, musicPath);
        result = 31 * result + Arrays.hashCode(notes);
        result = 31 * result + Arrays.hashCode(noteLanes);
        return result;
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "bpm=" + bpm +
                ", beatPeekCount=" + beatPeekCount +
                ", firstBeatOffset=" + firstBeatOffset +
                ", nextIndex=" + nextIndex +
                ", musicPath='" + musicPath + '\'' +
                ", notes=" + Arrays.toString(notes) +
                ", noteLanes=" + Arrays.toString(noteLanes) +
                ", laneKeys=" + laneKeys +
                '}';
    }
}
