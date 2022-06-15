package tech.fastj.gj;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.LogLevel;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.audio.StreamedAudio;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.Map;

import tech.fastj.gj.gameobjects.MusicNote;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.rhythm.InputMatcher;
import tech.fastj.gj.rhythm.SongInfo;
import tech.fastj.gj.scripts.MusicNoteMovement;
import tech.fastj.gj.ui.Notice;

public class Test extends SimpleManager {

    private static final double[] StackAttackNotes = {
            1f, 2.5f, 4f,
            5.5f, 7f, 8f,
            9f, 10.5f, 12f,
            13.5f, 15f, 16f,
            17f, 17.5f, 18.5f, 19.5f, 20f, 20.5f,
            21f, 21.5f, 22.5f, 24f,

            29f, 30.5f, 32f,
            33.5f, 35f, 36f,
            37f, 38.5f,

            45f, 46.5f, 48f,
            49.5f, 51f, 52f,
            53f, 54.5f,
    };
    private static final int[] StackAttackNoteLanes = {
            2, 1, 2,
            1, 3, 2,
            2, 1, 2,
            1, 3, 2,
            1, 3, 3, 3, 2, 1,
            1, 3, 3, 3,

            2, 1, 2,
            1, 3, 2,
            1, 2,

            2, 1, 2,
            1, 3, 2,
            1, 2,
    };
    private static final Map<Integer, Keys> StackAttackLaneKeys = Map.of(
            1, Keys.Left,
            2, Keys.Down,
            3, Keys.Right
    );

    private static final double StackAttackBPM = 184.0d;

    public static final float NoteSize = 25f;

    @Override
    public void init(FastJCanvas canvas) {
        canvas.modifyRenderSettings(RenderSettings.Antialiasing.Enable);
        Path audioPath = Path.of("audio/Stack_Attack_is_Back.wav");
        StreamedAudio music = FastJEngine.getAudioManager().loadStreamedAudio(audioPath);

        SongInfo stackAttackInfo = new SongInfo(
                StackAttackBPM,
                4,
                StackAttackNotes,
                StackAttackNoteLanes,
                StackAttackLaneKeys
        );
        Conductor conductor = new Conductor(music, stackAttackInfo, StackAttackBPM, -1, this);

        conductor.setSpawnMusicNote((note, noteLane) -> {
            Pointf noteStartingLocation = new Pointf(canvas.getCanvasCenter().x + (noteLane * NoteSize * 2), -NoteSize / 2f);
            MusicNote musicNote = new MusicNote(noteStartingLocation, NoteSize)
                    .setFill(DrawUtil.randomColor())
                    .setOutline(MusicNote.DefaultOutlineStroke, DrawUtil.randomColor());

            double noteTravelDistance = canvas.getResolution().y - (NoteSize * 4f);
            MusicNoteMovement musicNoteMovement = new MusicNoteMovement(conductor, note, noteTravelDistance);
            musicNote.addLateBehavior(musicNoteMovement, this);
            drawableManager.addGameObject(musicNote);
        });

        drawableManager.addGameObject(conductor);

        InputMatcher matcher = new InputMatcher(
                conductor,
                message -> {
                    Notice notice = new Notice(message, Color.black, new Pointf(100f, 50f), this);
                    drawableManager.addGameObject(notice);
                }
        );
        inputManager.addKeyboardActionListener(matcher);

        FastJEngine.getDisplay().getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                conductor.musicSource.stop();
                FastJEngine.getDisplay().getWindow().setVisible(false);
                FastJEngine.getDisplay().getWindow().dispose();
                System.exit(0);
            }
        });
    }

    @Override
    public void fixedUpdate(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }

    public static void main(String[] args) {
        FastJEngine.init("rhythm test", new Test());
        FastJEngine.setTargetUPS(1);
        FastJEngine.configureLogging(LogLevel.Debug);
        FastJEngine.run();
    }
}
