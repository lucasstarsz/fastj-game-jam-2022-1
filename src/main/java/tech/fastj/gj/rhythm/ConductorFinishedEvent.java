package tech.fastj.gj.rhythm;

import tech.fastj.gameloop.event.Event;

import java.util.Objects;

public class ConductorFinishedEvent extends Event {

    private final int totalNotesOverall;
    private final int totalNotesHit;
    private final int notesPerfect;
    private final int notesMissed;

    public ConductorFinishedEvent(int totalNotesOverall) {
        this.totalNotesOverall = totalNotesOverall;
        this.totalNotesHit = 0;
        this.notesPerfect = 0;
        this.notesMissed = 0;
    }

    public ConductorFinishedEvent(int totalNotesOverall, int totalNotesHit, int notesPerfect, int notesMissed) {
        this.totalNotesOverall = totalNotesOverall;
        this.totalNotesHit = totalNotesHit;
        this.notesPerfect = notesPerfect;
        this.notesMissed = notesMissed;
    }

    public int getTotalNotesOverall() {
        return totalNotesOverall;
    }

    public int getTotalNotesHit() {
        return totalNotesHit;
    }

    public int getNotesPerfect() {
        return notesPerfect;
    }

    public int getNotesMissed() {
        return notesMissed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ConductorFinishedEvent conductorFinishedEvent = (ConductorFinishedEvent) other;
        return totalNotesOverall == conductorFinishedEvent.totalNotesOverall
                && totalNotesHit == conductorFinishedEvent.totalNotesHit
                && notesPerfect == conductorFinishedEvent.notesPerfect
                && notesMissed == conductorFinishedEvent.notesMissed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalNotesOverall, totalNotesHit, notesPerfect, notesMissed);
    }

    @Override
    public String toString() {
        return "ConductorFinishedEvent{" +
                "totalNotesOverall=" + totalNotesOverall +
                ", totalNotesHit=" + totalNotesHit +
                ", notesPerfect=" + notesPerfect +
                ", notesMissed=" + notesMissed +
                '}';
    }
}
