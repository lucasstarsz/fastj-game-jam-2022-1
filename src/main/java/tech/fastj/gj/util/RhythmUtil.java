package tech.fastj.gj.util;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gj.gameobjects.KeyCircle;
import tech.fastj.gj.gameobjects.MusicNote;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.rhythm.GeneralSongInfo;
import tech.fastj.gj.scripts.MusicNoteMovement;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.util.DrawUtil;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.math.Maths;
import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.systems.control.GameHandler;

import java.awt.Color;
import java.util.List;

public class RhythmUtil {

    public static void createLaneKeys(GameHandler gameHandler, Conductor conductor, List<KeyCircle> keyCircles) {
        List<Keys> laneKeys = conductor.musicInfo.getLaneKeys();
        FastJCanvas canvas = FastJEngine.getCanvas();
        Pointf canvasCenter = canvas.getCanvasCenter();
        Point canvasResolution = canvas.getResolution();

        for (int i = 0; i < laneKeys.size(); i++) {
            Keys laneKey = laneKeys.get(i);
            Pointf laneStartingLocation = new Pointf(
                canvasCenter.x + ((i + 1) * Shapes.NoteSize * 2.5f),
                canvasResolution.y - (Shapes.NoteSize * 4f)
            );

            KeyCircle keyCircle = (KeyCircle) new KeyCircle(laneKey, Shapes.NoteSize, "Tahoma", gameHandler)
                .setFill(Color.gray)
                .setOutline(KeyCircle.DefaultOutlineStroke, KeyCircle.DefaultOutlineColor)
                .setTranslation(laneStartingLocation);

            keyCircles.add(keyCircle);
            gameHandler.drawableManager().addGameObject(keyCircle);
        }
    }

    public static Conductor createConductor(GameHandler gameHandler, GeneralSongInfo songInfo, FastJCanvas canvas) {
        Conductor conductor = new Conductor(songInfo, gameHandler, true);

        conductor.setSpawnMusicNote((note, noteLane) -> {
            Pointf noteStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (noteLane * Shapes.NoteSize * 2.5f), -Shapes.NoteSize / 2f);
            Color musicNoteColor = DrawUtil.randomColor();
            MusicNote musicNote = new MusicNote(noteStartingLocation, Shapes.NoteSize)
                .setFill(musicNoteColor)
                .setOutline(MusicNote.DefaultOutlineStroke, musicNoteColor.darker());

            double noteTravelDistance = canvas.getResolution().y - (Shapes.NoteSize * 4f);
            MusicNoteMovement musicNoteMovement = new MusicNoteMovement(conductor, note, noteTravelDistance);
            musicNote.addLateBehavior(musicNoteMovement, gameHandler);
            gameHandler.drawableManager().addGameObject(musicNote);
        });
        gameHandler.drawableManager().addGameObject(conductor);

        return conductor;
    }

    public static double adjustBeatPosition(double inputBeatPosition) {
        int cutBeatPosition = (int) inputBeatPosition;

        double adjustedBeatPosition;
        if (inputBeatPosition <= cutBeatPosition + 0.25d) {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, cutBeatPosition, (float) (cutBeatPosition + 0.25d));
        } else if (inputBeatPosition <= cutBeatPosition + 0.5d) {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.25d), (float) (cutBeatPosition + 0.5d));
        } else if (inputBeatPosition <= cutBeatPosition + 0.75d) {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.5d), (float) (cutBeatPosition + 0.75d));
        } else {
            adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.75d), cutBeatPosition + 1);
        }

        return adjustedBeatPosition;
    }
}
