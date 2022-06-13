package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

public class SongInfo {

    private final double bpm;
    private final double[] notes;
    int nextIndex;
    private final int beatPeekCount;

    public SongInfo(double bpm, int beatPeekCount, double[] notes) {
        this.bpm = bpm;
        this.notes = notes;
        this.nextIndex = 0;
        this.beatPeekCount = beatPeekCount;
    }

    public double getBpm() {
        return bpm;
    }

    public double getNote(int index) {
        return notes[index];
    }

    public double getNotesLength() {
        return notes.length;
    }

    public int getBeatPeekCount() {
        return beatPeekCount;
    }

    public int findIndex(double beat) {
        if (beat >= notes[notes.length - 1]) {
            return -1;
        }

        for (int i = 0; i < notes.length; i++) {
            if (beat <= notes[i]) {
                FastJEngine.log("{} <= {}", beat, notes[i]);
                return i;
            }
        }

        return -1;
    }
}
