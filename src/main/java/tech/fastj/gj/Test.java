package tech.fastj.gj;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.LogLevel;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import com.google.gson.Gson;
import tech.fastj.gameloop.event.GameEventObserver;
import tech.fastj.gj.gameobjects.KeyCircle;
import tech.fastj.gj.gameobjects.MusicNote;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.rhythm.ConductorFinishedEvent;
import tech.fastj.gj.rhythm.InputMatcher;
import tech.fastj.gj.rhythm.SongInfo;
import tech.fastj.gj.scenes.game.ResultMenu;
import tech.fastj.gj.scripts.MusicNoteMovement;
import tech.fastj.gj.ui.Notice;
import tech.fastj.gj.util.FilePaths;
import tech.fastj.gj.util.Shapes;

public class Test extends SimpleManager implements GameEventObserver<ConductorFinishedEvent> {

    @Override
    public void init(FastJCanvas canvas) {
        canvas.modifyRenderSettings(RenderSettings.Antialiasing.Enable);

        Gson gson = new Gson();
        String stackAttackJson;
        try {
            stackAttackJson = Files.readString(FilePaths.StackAttackJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SongInfo stackAttackInfo = gson.fromJson(stackAttackJson, SongInfo.class);
        Conductor conductor = new Conductor(stackAttackInfo, this, false);

        conductor.setSpawnMusicNote((note, noteLane) -> {
            Pointf noteStartingLocation = new Pointf((canvas.getCanvasCenter().x * 1.5f) + (noteLane * Shapes.NoteSize * 2.5f), -Shapes.NoteSize / 2f);
            MusicNote musicNote = new MusicNote(noteStartingLocation, Shapes.NoteSize)
                    .setFill(DrawUtil.randomColor())
                    .setOutline(MusicNote.DefaultOutlineStroke, DrawUtil.randomColor());

            double noteTravelDistance = canvas.getResolution().y - (Shapes.NoteSize * 4f);
            MusicNoteMovement musicNoteMovement = new MusicNoteMovement(conductor, note, noteTravelDistance);
            musicNote.addLateBehavior(musicNoteMovement, this);
            drawableManager.addGameObject(musicNote);
        });

        Collection<Keys> laneKeys = conductor.musicInfo.getLaneKeys();
        int laneKeyIncrement = 1;
        for (Keys laneKey : laneKeys) {
            Pointf laneStartingLocation = new Pointf((canvas.getCanvasCenter().x * 1.5f) + (laneKeyIncrement * Shapes.NoteSize * 2.5f), canvas.getResolution().y - (Shapes.NoteSize * 4f));
            KeyCircle keyCircle = (KeyCircle) new KeyCircle(laneKey, Shapes.NoteSize, "Tahoma")
                    .setFill(Color.yellow)
                    .setOutline(KeyCircle.DefaultOutlineStroke, KeyCircle.DefaultOutlineColor)
                    .setTranslation(laneStartingLocation);
            drawableManager.addGameObject(keyCircle);
            laneKeyIncrement++;
        }

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

        FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
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

    @Override
    public void eventReceived(ConductorFinishedEvent event) {
        new ResultMenu(this, event);
    }
}
