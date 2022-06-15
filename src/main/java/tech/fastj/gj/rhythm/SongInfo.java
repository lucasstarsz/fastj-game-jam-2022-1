package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.Keys;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class SongInfo {

    private final double bpm;
    private final double[] notes;
    private final int[] noteLanes;
    private final Map<Integer, Keys> laneKeys;
    int nextIndex;
    private final int beatPeekCount;

    public SongInfo(double bpm, int beatPeekCount, double[] notes, int[] noteLanes, Map<Integer, Keys> laneKeys) {
        this.bpm = bpm;
        this.notes = notes;
        this.noteLanes = noteLanes;
        this.laneKeys = laneKeys;
        this.nextIndex = 0;
        this.beatPeekCount = beatPeekCount;
    }

    public double getBpm() {
        return bpm;
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
