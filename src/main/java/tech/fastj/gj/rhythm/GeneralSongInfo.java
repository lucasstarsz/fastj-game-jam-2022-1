package tech.fastj.gj.rhythm;

import tech.fastj.input.keyboard.Keys;

import java.util.Collection;

public interface GeneralSongInfo {

    String getSongName();

    String getMusicPath();

    double getBpm();

    double getFirstBeatOffset();

    double getNote(int index);

    int getNoteLane(int index);

    Collection<Keys> getLaneKeys();

    Keys getLaneKey(int lane);

    int getNotesLength();

    int getBeatPeekCount();

    int getNextIndex();

    void incrementNextIndex();

    void resetNextIndex();
}
