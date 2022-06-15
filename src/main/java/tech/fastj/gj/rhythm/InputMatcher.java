package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.systems.audio.state.PlaybackState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public record InputMatcher(Set<Double> consumedNotes, Conductor conductor,
                           Consumer<String> onSpawnNotice) implements KeyboardActionListener {

    private static final double MaxNoteDistance = 0.25d;
    private static final double PerfectNoteDistance = 0.125d;

    public InputMatcher(Conductor conductor, Consumer<String> onSpawnNotice) {
        this(new HashSet<>(), conductor, onSpawnNotice);
    }

    @Override
    public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
        if (conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
            return;
        }

        if (conductor.musicInfo.getLaneKeys().contains(keyboardStateEvent.getKey())) {
            double inputBeatPosition = (((keyboardStateEvent.getTimestamp() / 1_000_000_000d) - conductor.dspSongTime) / conductor.secPerBeat) - conductor.firstBeatOffset;
            FastJEngine.trace("arrow key pressed at {}", inputBeatPosition);
            checkNotes(inputBeatPosition, keyboardStateEvent.getKey());
        }
    }

    private void checkNotes(double inputBeatPosition, Keys inputKey) {
        if (inputBeatPosition > conductor.musicInfo.getNote(conductor.musicInfo.getNotesLength() - 1) + MaxNoteDistance) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
            return;
        }

        int nextIndex = conductor.musicInfo.findIndex(inputBeatPosition, MaxNoteDistance);

        boolean hasNext = nextIndex != -1 && inputKey == conductor.musicInfo.getLaneKey(conductor.musicInfo.getNoteLane(nextIndex));
        double nextNote = 0;
        if (hasNext) {
            nextNote = conductor.musicInfo.getNote(nextIndex);
            FastJEngine.trace("checking {} next", nextNote);
        }
        double nextNoteDistance = 0;
        if (hasNext) {
            nextNoteDistance = Math.abs(nextNote - inputBeatPosition);
        }

        boolean hasPrevious = nextIndex - 1 >= 0 && inputKey == conductor.musicInfo.getLaneKey(conductor.musicInfo.getNoteLane(nextIndex - 1));
        double previousNote = 0;
        if (hasPrevious) {
            previousNote = conductor.musicInfo.getNote(nextIndex - 1);
            FastJEngine.trace("checking {} previous", previousNote);
        }
        double lastNoteDistance = 0;
        if (hasPrevious) {
            lastNoteDistance = Math.abs(inputBeatPosition - previousNote);
        }

        if (!hasPrevious && hasNext || Double.compare(lastNoteDistance, nextNoteDistance) >= 0) {
            if (!consumedNotes.contains(nextNote) && checkNote(nextNoteDistance, inputBeatPosition, "Early.")) {
                consumedNotes.add(nextNote);
            }
        } else {
            if (!consumedNotes.contains(previousNote) && checkNote(lastNoteDistance, inputBeatPosition, "Late.")) {
                consumedNotes.add(previousNote);
            }
        }
    }

    private boolean checkNote(double nextNoteDistance, double inputBeatPosition, String helpfulTip) {
        double adjustedMaxDistance = MaxNoteDistance * (conductor.songBpm / 120d);
        if (nextNoteDistance > adjustedMaxDistance) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
            return false;
        } else {
            double adjustedPerfectDistance = PerfectNoteDistance * (conductor.songBpm / 120d);
            String resultMessage = nextNoteDistance < adjustedPerfectDistance ? "Perfect!" : helpfulTip;
            FastJEngine.log("Input was {} beats away from next note. {}", nextNoteDistance, resultMessage);
            FastJEngine.runAfterRender(() -> onSpawnNotice.accept(resultMessage));
            return true;
        }
    }
}
