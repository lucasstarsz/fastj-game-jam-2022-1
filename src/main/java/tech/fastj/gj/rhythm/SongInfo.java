package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.Keys;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SongInfo {

    private double bpm;
    private double[] notes;
    private int[] noteLanes;
    private Map<Integer, Keys> laneKeys;
    int nextIndex;
    private int beatPeekCount;
    private double firstBeatOffset;
    private String musicPath;

    public SongInfo() {
    }

    public SongInfo(double bpm, int beatPeekCount, double firstBeatOffset, double[] notes, int[] noteLanes, Map<Integer, Keys> laneKeys, String musicPath) {
        this.bpm = bpm;
        this.notes = notes;
        this.noteLanes = noteLanes;
        this.laneKeys = laneKeys;
        this.musicPath = musicPath;
        this.nextIndex = 0;
        this.beatPeekCount = beatPeekCount;
        this.firstBeatOffset = firstBeatOffset;
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
}
