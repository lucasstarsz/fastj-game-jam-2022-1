package tech.fastj.gj.rhythm;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.systems.audio.Audio;
import tech.fastj.systems.audio.AudioEvent;
import tech.fastj.systems.behaviors.Behavior;
import tech.fastj.systems.behaviors.BehaviorHandler;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Graphics2D;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

public class Conductor extends GameObject implements Behavior {

    public double songBpm;
    public double secPerBeat;
    public double songPosition;
    public double songPositionInBeats;
    public double firstBeatOffset;
    public double dspSongTime;
    public Audio musicSource;
    public SongInfo musicInfo;
    private DoubleConsumer spawnMusicNote;
    private final ScheduledExecutorService musicPlayer = Executors.newSingleThreadScheduledExecutor();

    public Conductor(Audio musicSource, SongInfo musicInfo, double songBpm, BehaviorHandler behaviorHandler) {
        this(musicSource, musicInfo, songBpm, 0, behaviorHandler);
    }

    public Conductor(Audio musicSource, SongInfo musicInfo, double songBpm, double firstBeatOffset, BehaviorHandler behaviorHandler) {
        this.musicSource = musicSource;
        this.musicInfo = musicInfo;
        this.songBpm = songBpm;
        this.secPerBeat = 60d / songBpm;
        this.firstBeatOffset = firstBeatOffset;
        setCollisionPath(DrawUtil.createPath(DrawUtil.createBox(Pointf.origin(), 0f)));
        addBehavior(this, behaviorHandler);
    }

    public void setSpawnMusicNote(DoubleConsumer spawnMusicNote) {
        this.spawnMusicNote = spawnMusicNote;
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
        songPosition = (System.nanoTime() / 1_000_000_000d) - dspSongTime - (firstBeatOffset * secPerBeat);
        songPositionInBeats = songPosition / secPerBeat;

        if (musicInfo.nextIndex < musicInfo.getNotesLength() && musicInfo.getNote(musicInfo.nextIndex) < songPositionInBeats + musicInfo.getBeatPeekCount()) {
            double note = musicInfo.getNote(musicInfo.nextIndex);
            spawnMusicNote.accept(note);

            FastJEngine.log("added new music note at beat {}", note);
            musicInfo.nextIndex++;
        }
    }
}
