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

import javax.sound.sampled.LineEvent;
import java.awt.Graphics2D;

public class Conductor extends GameObject implements Behavior {

    public double songBpm;
    public double secPerBeat;
    public double songPosition;
    public double songPositionInBeats;
    public double firstBeatOffset;
    public double dspSongTime;
    public Audio musicSource;
    public SongInfo musicInfo;

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

        musicSource.getAudioSource().addLineListener(event -> {
            if (event.getType() == LineEvent.Type.START && dspSongTime == 0) {
                dspSongTime = System.nanoTime() / 1_000_000_000d;
            }
        });

        musicSource.play();
    }

    @Override
    public void fixedUpdate(GameObject gameObject) {
    }

    @Override
    public void update(GameObject gameObject) {
        songPosition = (System.nanoTime() / 1_000_000_000d) - dspSongTime - (firstBeatOffset * secPerBeat);
        songPositionInBeats = songPosition / secPerBeat;

        if (musicInfo.nextIndex < musicInfo.getNotesLength() && musicInfo.getNote(musicInfo.nextIndex) < songPositionInBeats + musicInfo.getBeatPeekCount()) {
            FastJEngine.log("spawned new music note at: {}", musicInfo.getNote(musicInfo.nextIndex));

            musicInfo.nextIndex++;
        }
    }
}
