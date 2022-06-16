package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.systems.audio.state.PlaybackState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public record GameInputMatcher(Set<Double> consumedNotes, Conductor conductor, SongInfo songInfo,
                               Consumer<String> onSpawnNotice) implements KeyboardActionListener {

    private static final double MaxNoteDistance = 0.25d;
    private static final double PerfectNoteDistance = 0.125d;

    public GameInputMatcher(Conductor conductor, SongInfo songInfo, Consumer<String> onSpawnNotice) {
        this(new HashSet<>(), conductor, songInfo, onSpawnNotice);
    }

    @Override
    public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
        if (conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
            return;
        }

        if (songInfo.getLaneKeys().contains(keyboardStateEvent.getKey())) {
            double inputBeatPosition = (((keyboardStateEvent.getTimestamp() / 1_000_000_000d) - conductor.dspSongTime) / conductor.secPerBeat) - conductor.firstBeatOffset;
            FastJEngine.trace("{} arrow key pressed at {}", keyboardStateEvent.getKey(), inputBeatPosition);
            checkNotes(inputBeatPosition, keyboardStateEvent.getKey());
        }
    }

    private void checkNotes(double inputBeatPosition, Keys inputKey) {
        if (inputBeatPosition > songInfo.getNote(songInfo.getNotesLength() - 1) + MaxNoteDistance) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
            return;
        }

        int nextIndex = songInfo.findIndex(inputBeatPosition, MaxNoteDistance);

        boolean hasNext = nextIndex != -1 && inputKey == songInfo.getLaneKey(songInfo.getNoteLane(nextIndex));
        double nextNote = 0;
        if (hasNext) {
            nextNote = songInfo.getNote(nextIndex);
            FastJEngine.trace("has next on {}", nextNote);
        }
        double nextNoteDistance = 0;
        if (hasNext) {
            nextNoteDistance = Math.abs(nextNote - inputBeatPosition);
        }

        boolean hasPrevious = nextIndex - 1 >= 0 && inputKey == songInfo.getLaneKey(songInfo.getNoteLane(nextIndex - 1));
        double previousNote = 0;
        if (hasPrevious) {
            previousNote = songInfo.getNote(nextIndex - 1);
            FastJEngine.trace("has previous on {}", previousNote);
        }
        double lastNoteDistance = 0;
        if (hasPrevious) {
            lastNoteDistance = Math.abs(inputBeatPosition - previousNote);
        }

        if (!hasPrevious && hasNext || hasPrevious && hasNext && Double.compare(lastNoteDistance, nextNoteDistance) >= 0) {
            if (!consumedNotes.contains(nextNote) && checkNote(nextNoteDistance, inputBeatPosition, "next", "Early.")) {
                consumedNotes.add(nextNote);
                FastJEngine.trace("consumed {}", nextNote);
            }
        } else {
            if (!consumedNotes.contains(previousNote) && checkNote(lastNoteDistance, inputBeatPosition, "previous", "Late.")) {
                consumedNotes.add(previousNote);
                FastJEngine.trace("consumed {}", previousNote);
            }
        }
    }

    private boolean checkNote(double nextNoteDistance, double inputBeatPosition, String nextOrPrevious, String helpfulTip) {
        double adjustedMaxDistance = MaxNoteDistance * (conductor.songBpm / 120d);
        if (nextNoteDistance > adjustedMaxDistance) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
            return false;
        } else {
            double adjustedPerfectDistance = PerfectNoteDistance * (conductor.songBpm / 120d);
            String resultMessage = nextNoteDistance < adjustedPerfectDistance ? "Perfect!" : helpfulTip;
            FastJEngine.log("Input was {} beats away from {} note. {}", nextNoteDistance, nextOrPrevious, resultMessage);
            FastJEngine.runAfterRender(() -> onSpawnNotice.accept(resultMessage));
            return true;
        }
    }
}
