package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.systems.audio.Audio;
import tech.fastj.systems.audio.AudioEvent;
import tech.fastj.systems.audio.state.PlaybackState;
import tech.fastj.systems.behaviors.Behavior;
import tech.fastj.systems.behaviors.BehaviorHandler;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Graphics2D;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class Conductor extends GameObject implements Behavior {

    public double songBpm;
    public double secPerBeat;
    public double songPosition;
    public double songPositionInBeats;
    public double firstBeatOffset;
    public double dspSongTime;
    public Audio musicSource;
    public SongInfo musicInfo;
    private BiConsumer<Double, Integer> spawnMusicNote;
    private boolean isFinished;
    private boolean isPaused;
    private double pauseTimeOffset;
    private final ScheduledExecutorService musicPlayer = Executors.newSingleThreadScheduledExecutor();

    public Conductor(SongInfo musicInfo, BehaviorHandler behaviorHandler, boolean needsLateUpdate) {
        this.musicInfo = musicInfo;
        this.songBpm = musicInfo.getBpm();
        this.secPerBeat = 60d / songBpm;
        this.firstBeatOffset = musicInfo.getFirstBeatOffset();
        setCollisionPath(DrawUtil.createPath(DrawUtil.createBox(Pointf.origin(), 0f)));

        this.musicSource = FastJEngine.getAudioManager().loadStreamedAudio(Path.of(musicInfo.getMusicPath()));

        if (needsLateUpdate) {
            addLateBehavior(this, behaviorHandler);
        } else {
            addBehavior(this, behaviorHandler);
        }

    }

    public void setSpawnMusicNote(BiConsumer<Double, Integer> spawnMusicNote) {
        this.spawnMusicNote = spawnMusicNote;
    }

    public void setPaused(boolean paused) {
        if (isPaused == paused) {
            return;
        }

        isPaused = paused;
        if (!isPaused) {
            pauseTimeOffset += (System.nanoTime() / 1_000_000_000d) - songPosition;
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public void render(Graphics2D g) {
    }

    @Override
    public void destroy(Scene origin) {
        super.destroyTheRest(origin);
    }

    @Override
    public void destroy(SimpleManager origin) {
        super.destroyTheRest(origin);
    }

    @Override
    public void destroy() {
        musicSource.stop();
        musicPlayer.shutdownNow();
    }

    @Override
    public String toString() {
        return "Conductor{" +
                "songBpm=" + songBpm +
                ", secPerBeat=" + secPerBeat +
                ", songPosition=" + songPosition +
                ", songPositionInBeats=" + songPositionInBeats +
                ", dspSongTime=" + dspSongTime +
                ", musicSource=" + musicSource +
                '}';
    }

    @Override
    public void init(GameObject gameObject) {
        musicSource.getAudioEventListener().setAudioStopAction(event -> {
            if (FastJEngine.isRunning() && FastJEngine.getDisplay().getWindow().isShowing()) {
                FastJEngine.runAfterRender(() -> {
                    FastJEngine.getGameLoop().removeEventObserver(musicSource.getAudioEventListener(), AudioEvent.class);
                    FastJEngine.getAudioManager().unloadStreamedAudio(musicSource.getID());
                });
            } else {
                FastJEngine.getGameLoop().removeEventObserver(musicSource.getAudioEventListener(), AudioEvent.class);
                FastJEngine.getAudioManager().unloadStreamedAudio(musicSource.getID());
            }
        });

        double songDelay = 60d / musicInfo.getBpm() * musicInfo.getBeatPeekCount() * 1_000_000_000d;
        dspSongTime = (System.nanoTime() + songDelay) / 1_000_000_000d;
        musicPlayer.schedule(musicSource::play, (long) songDelay, TimeUnit.NANOSECONDS);
    }

    @Override
    public void fixedUpdate(GameObject gameObject) {
    }

    @Override
    public void update(GameObject gameObject) {
        if (!isFinished && musicInfo.nextIndex >= musicInfo.getNotesLength() && musicSource.getCurrentPlaybackState() == PlaybackState.Stopped) {
            FastJEngine.log("end?????????????????????????");
            isFinished = true;
            ConductorFinishedEvent event = new ConductorFinishedEvent(musicInfo.getNotesLength());
            FastJEngine.runAfterRender(() -> FastJEngine.getGameLoop().fireEvent(event));
            return;
        }

        if (isPaused) {
            return;
        }

        songPosition = (System.nanoTime() / 1_000_000_000d) - dspSongTime - (firstBeatOffset * secPerBeat) - pauseTimeOffset;
        songPositionInBeats = songPosition / secPerBeat;

        if (musicInfo.nextIndex < musicInfo.getNotesLength() && musicInfo.getNote(musicInfo.nextIndex) < songPositionInBeats + musicInfo.getBeatPeekCount()) {
            double note = musicInfo.getNote(musicInfo.nextIndex);
            int noteLane = musicInfo.getNoteLane(musicInfo.nextIndex);
            spawnMusicNote.accept(note, noteLane);

            FastJEngine.log("added new music note {} at beat {}", note, songPositionInBeats);
            musicInfo.nextIndex++;
        } else if (!isFinished && musicInfo.nextIndex >= musicInfo.getNotesLength() && musicSource.getCurrentPlaybackState() == PlaybackState.Stopped) {
            isFinished = true;
            ConductorFinishedEvent event = new ConductorFinishedEvent(musicInfo.getNotesLength());
            FastJEngine.runAfterRender(() -> FastJEngine.getGameLoop().fireEvent(event));
        }
    }
}
