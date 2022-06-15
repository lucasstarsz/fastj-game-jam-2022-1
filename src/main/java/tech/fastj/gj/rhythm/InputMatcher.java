package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.systems.audio.state.PlaybackState;

import java.util.function.Consumer;

public record InputMatcher(Conductor conductor, Consumer<String> onSpawnNotice) implements KeyboardActionListener {

    private static final double MaxNoteDistance = 0.25d;
    private static final double PerfectNoteDistance = 0.125d;

    @Override
    public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
        if (conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
            return;
        }

        if (keyboardStateEvent.getKey() == Keys.Left) {
            double inputBeatPosition = (((keyboardStateEvent.getTimestamp() / 1_000_000_000d) - conductor.dspSongTime) / conductor.secPerBeat) - conductor.firstBeatOffset;
            FastJEngine.trace("arrow key pressed at {}", inputBeatPosition);
            checkNotes(inputBeatPosition);
        }
    }

    private void checkNotes(double inputBeatPosition) {
        int nextIndex = conductor.musicInfo.findIndex(inputBeatPosition);
        if (nextIndex >= conductor.musicInfo.getNotesLength() || nextIndex == -1) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
            return;
        }
        boolean hasPrevious = nextIndex - 1 >= 0;

        FastJEngine.trace("checking {} next", conductor.musicInfo.getNote(nextIndex));
        if (hasPrevious) {
            FastJEngine.trace("checking {} previous", conductor.musicInfo.getNote(nextIndex - 1));
        }

        double nextNoteDistance = Math.abs(conductor.musicInfo.getNote(nextIndex) - inputBeatPosition);
        double lastNoteDistance = 0;
        if (hasPrevious) {
            lastNoteDistance = Math.abs(inputBeatPosition - conductor.musicInfo.getNote(nextIndex - 1));
        }

        if (!hasPrevious || Double.compare(lastNoteDistance, nextNoteDistance) >= 0) {
            checkNote(nextNoteDistance, inputBeatPosition, "Early.");
        } else {
            checkNote(lastNoteDistance, inputBeatPosition, "Late.");
        }
    }

    private void checkNote(double nextNoteDistance, double inputBeatPosition, String helpfulTip) {
        if (nextNoteDistance > MaxNoteDistance * (conductor.songBpm / 120d)) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
        } else {
            String resultMessage = nextNoteDistance < PerfectNoteDistance * (conductor.songBpm / 120d) ? "Perfect!" : helpfulTip;
            FastJEngine.log("Input was {} beats away from next note. {}", nextNoteDistance, resultMessage);
            FastJEngine.runAfterRender(() -> onSpawnNotice.accept(resultMessage));
        }
    }
}
