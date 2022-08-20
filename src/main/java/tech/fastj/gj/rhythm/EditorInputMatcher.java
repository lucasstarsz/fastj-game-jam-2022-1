package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;
import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.math.Maths;
import tech.fastj.systems.audio.state.PlaybackState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public final class EditorInputMatcher implements KeyboardActionListener {

    private final Set<Double> consumedNotes;
    private final Conductor conductor;
    private final EditableSongInfo songInfo;
    private final List<RecordedNote> recordedNotes;
    private BiConsumer<KeyboardStateEvent, Double> onLaneKeyPressed;

    public EditorInputMatcher(Conductor conductor, EditableSongInfo songInfo) {
        this.consumedNotes = new HashSet<>();
        this.recordedNotes = new ArrayList<>();
        this.conductor = conductor;
        this.songInfo = songInfo;
    }

    public void setOnLaneKeyPressed(BiConsumer<KeyboardStateEvent, Double> onLaneKeyPressed) {
        this.onLaneKeyPressed = onLaneKeyPressed;
    }

    @Override
    public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
        if (conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
            return;
        }

        double outputBeatPosition = -1;
        if (songInfo.getLaneKeys().contains(keyboardStateEvent.getKey())) {
            double inputBeatPosition = (((keyboardStateEvent.getTimestamp() / 1_000_000_000d) - conductor.dspSongTime) / conductor.secPerBeat) - conductor.firstBeatOffset;
            FastJEngine.trace("{} key pressed at {}", keyboardStateEvent.getKey(), inputBeatPosition);
            outputBeatPosition = checkNotes(inputBeatPosition, keyboardStateEvent.getKey());
        }

        if (onLaneKeyPressed != null) {
            onLaneKeyPressed.accept(keyboardStateEvent, outputBeatPosition);
        }
    }

    private double checkNotes(double inputBeatPosition, Keys inputKey) {
        int cutBeatPosition = (int) inputBeatPosition;

        double adjustedBeatPosition;
        if (inputBeatPosition <= cutBeatPosition + 0.25d) {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, cutBeatPosition, (float) (cutBeatPosition + 0.25d));
        } else if (inputBeatPosition <= cutBeatPosition + 0.5d) {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.25d), (float) (cutBeatPosition + 0.5d));
        } else if (inputBeatPosition <= cutBeatPosition + 0.75d) {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.5d), (float) (cutBeatPosition + 0.75d));
        } else {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.75d), cutBeatPosition + 1);
        }

        if (consumedNotes.contains(adjustedBeatPosition)) {
            return -1;
        }

        consumedNotes.add(adjustedBeatPosition);
        recordedNotes.add(new RecordedNote(adjustedBeatPosition, songInfo.getKeyLane(inputKey)));

        return adjustedBeatPosition;
    }

    public List<RecordedNote> getRecordedNotes() {
        return recordedNotes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (EditorInputMatcher) obj;
        return Objects.equals(this.consumedNotes, that.consumedNotes) &&
                Objects.equals(this.conductor, that.conductor) &&
                Objects.equals(this.songInfo, that.songInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumedNotes, conductor, songInfo);
    }

    @Override
    public String toString() {
        return "EditorInputMatcher[" +
                "consumedNotes=" + consumedNotes + ", " +
                "conductor=" + conductor + ", " +
                "songInfo=" + songInfo + ']';
    }
}
