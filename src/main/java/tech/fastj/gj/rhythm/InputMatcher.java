package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;

import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;

public record InputMatcher(Conductor conductor) implements KeyboardActionListener {

    private static final double MaxNoteDistance = 0.25d;

    @Override
    public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
        if (keyboardStateEvent.getKey() == Keys.Left) {
            double inputBeatPosition = (((System.nanoTime() / 1_000_000_000d) - conductor.dspSongTime) / conductor.secPerBeat) - conductor.firstBeatOffset;
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

        FastJEngine.trace("checking {} next", conductor.musicInfo.getNote(nextIndex));
        FastJEngine.trace("checking {} previous", conductor.musicInfo.getNote(nextIndex - 1));
        double nextNoteDistance = Math.abs(conductor.musicInfo.getNote(nextIndex) - inputBeatPosition);
        double lastNoteDistance = Math.abs(inputBeatPosition - conductor.musicInfo.getNote(nextIndex - 1));

        if (Double.compare(lastNoteDistance, nextNoteDistance) >= 0) {
            checkNote(nextNoteDistance, inputBeatPosition, "Early.");
        } else {
            checkNote(lastNoteDistance, inputBeatPosition, "Late.");
        }
    }

    private void checkNote(double nextNoteDistance, double inputBeatPosition, String helpfulTip) {
        if (nextNoteDistance > MaxNoteDistance) {
            FastJEngine.log("extra note at {}", inputBeatPosition);
        } else {
            FastJEngine.log("note was {} beats away from next note. {}", nextNoteDistance, nextNoteDistance < 0.125 ? "Perfect!" : helpfulTip);
        }
    }
}
