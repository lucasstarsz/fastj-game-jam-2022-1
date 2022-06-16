package tech.fastj.gj.rhythm;

import tech.fastj.input.keyboard.Keys;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class EditableSongInfo implements GeneralSongInfo {

    public String songName;
    public double bpm;
    public double[] notes;
    public int[] noteLanes;
    public TreeMap<Integer, Keys> laneKeys;
    int nextIndex;
    public int beatPeekCount;
    public double firstBeatOffset;
    public String musicPath;

    public EditableSongInfo() {
    }

    public EditableSongInfo(String songName, double bpm, int beatPeekCount, double firstBeatOffset, TreeMap<Integer, Keys> laneKeys, String musicPath) {
        this.songName = songName;
        this.bpm = bpm;
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

    @Override
    public String getMusicPath() {
        return musicPath;
    }

    @Override
    public double getBpm() {
        return bpm;
    }

    @Override
    public double getFirstBeatOffset() {
        return firstBeatOffset;
    }

    @Override
    public double getNote(int index) {
        return notes[index];
    }

    @Override
    public int getNoteLane(int index) {
        return noteLanes[index];
    }

    @Override
    public Collection<Keys> getLaneKeys() {
        return Collections.unmodifiableCollection(laneKeys.values());
    }

    @Override
    public Keys getLaneKey(int lane) {
        return laneKeys.get(lane);
    }

    public int getKeyLane(Keys key) {
        for (Map.Entry<Integer, Keys> entry : laneKeys.entrySet()) {
            if (entry.getValue() == key) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException("No key lane found for key " + key);
    }

    @Override
    public int getNotesLength() {
        return notes == null ? 0 : notes.length;
    }

    @Override
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EditableSongInfo editableSongInfo = (EditableSongInfo) o;
        return Double.compare(editableSongInfo.bpm, bpm) == 0
                && beatPeekCount == editableSongInfo.beatPeekCount
                && Double.compare(editableSongInfo.firstBeatOffset, firstBeatOffset) == 0
                && Arrays.equals(notes, editableSongInfo.notes)
                && Arrays.equals(noteLanes, editableSongInfo.noteLanes)
                && Objects.equals(laneKeys, editableSongInfo.laneKeys)
                && Objects.equals(musicPath, editableSongInfo.musicPath);
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
        return "EditableSongInfo{" +
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

    public void resetNextIndex() {
        nextIndex = 0;
    }
}
