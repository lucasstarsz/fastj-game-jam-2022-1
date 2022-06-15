package tech.fastj.gj.scenes.game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.control.Scene;

import java.awt.Color;
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
import tech.fastj.gj.scripts.MusicNoteMovement;
import tech.fastj.gj.ui.ContentBox;
import tech.fastj.gj.ui.Notice;
import tech.fastj.gj.user.User;
import tech.fastj.gj.util.FilePaths;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;

public class MainGame extends Scene implements GameEventObserver<ConductorFinishedEvent> {

    private GameState gameState;
    private User user;
    private Conductor conductor;

    private ContentBox scoreBox;
    private ContentBox highScoreBox;
    private ContentBox blocksStackedBox;
    private ContentBox highestBlocksStackedBox;

    private PauseMenu pauseMenu;
    private KeyboardActionListener pauseListener;
    private boolean allowClicks;

    private ResultMenu resultMenu;

    public MainGame() {
        super(SceneNames.Game);
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isRhythmInputAllowed() {
        return allowClicks;
    }

    @Override
    public void load(FastJCanvas canvas) {
        System.out.println("begin");
        Log.debug(MainGame.class, "loading {}", getSceneName());
        changeState(GameState.Intro);
        Log.debug(MainGame.class, "loaded {}", getSceneName());
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(MainGame.class, "unloading {}", getSceneName());
        gameState = null;
        allowClicks = false;

        if (user != null) {
            user.resetScore();
            user = null;
        }

        if (conductor != null) {
            conductor.setPaused(true);
            conductor.musicSource.stop();
            conductor = null;
        }

        if (scoreBox != null) {
            scoreBox.destroy(this);
            scoreBox = null;
        }

        if (highScoreBox != null) {
            highScoreBox.destroy(this);
            highScoreBox = null;
        }

        if (blocksStackedBox != null) {
            blocksStackedBox.destroy(this);
            blocksStackedBox = null;
        }

        if (highestBlocksStackedBox != null) {
            highestBlocksStackedBox.destroy(this);
            highestBlocksStackedBox = null;
        }

        if (pauseMenu != null) {
            pauseMenu.destroy(this);
            pauseMenu = null;
        }

        if (pauseListener != null) {
            inputManager.removeKeyboardActionListener(pauseListener);
            pauseListener = null;
        }

        if (resultMenu != null) {
            resultMenu.destroy(this);
            resultMenu = null;
        }

        setInitialized(false);
        Log.debug(MainGame.class, "unloaded {}", getSceneName());
    }

    @Override
    public void fixedUpdate(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }

    public void changeState(GameState next) {
        Log.debug(MainGame.class, "changing state from {} to {}", gameState, next);
        switch (next) {
            case Intro -> {
                if (gameState == GameState.Results) {

                    scoreBox.destroy(this);
                    scoreBox = null;

                    highScoreBox.destroy(this);
                    scoreBox = null;

                    blocksStackedBox.destroy(this);
                    scoreBox = null;

                    highestBlocksStackedBox.destroy(this);
                    scoreBox = null;

                    user.resetScore();
                    user = null;

                    resultMenu.destroy(this);
                    resultMenu = null;
                    FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class);
                }

                FastJEngine.runAfterUpdate(() -> changeState(GameState.Playing));
            }
            case Playing -> {
                if (gameState == GameState.Intro) {
                    user = User.getInstance();

                    scoreBox = new ContentBox(this, "Score", "" + user.getScore());
                    scoreBox.setTranslation(new Pointf(30f));
                    scoreBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                    drawableManager.addUIElement(scoreBox);

                    highScoreBox = new ContentBox(this, "High Score", "" + user.getHighScore());
                    highScoreBox.setTranslation(new Pointf(30f, 50f));
                    highScoreBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                    drawableManager.addUIElement(highScoreBox);

                    blocksStackedBox = new ContentBox(this, "Blocks Stacked", "" + user.getNumberStacked());
                    blocksStackedBox.setTranslation(new Pointf(30f, 70f));
                    blocksStackedBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                    drawableManager.addUIElement(blocksStackedBox);

                    highestBlocksStackedBox = new ContentBox(this, "Highest Number Stacked", "" + user.getHighestNumberStacked());
                    highestBlocksStackedBox.setTranslation(new Pointf(30f, 90f));
                    highestBlocksStackedBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                    drawableManager.addUIElement(highestBlocksStackedBox);

                    Gson gson = new Gson();
                    String stackAttackJson;
                    try {
                        stackAttackJson = Files.readString(FilePaths.StackAttackJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    SongInfo stackAttackInfo = gson.fromJson(stackAttackJson, SongInfo.class);
                    conductor = new Conductor(stackAttackInfo, this, true);
                    FastJCanvas canvas = FastJEngine.getCanvas();

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
                    FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
                } else if (gameState == GameState.Paused) {
                    pauseMenu.setShouldRender(false);
                    inputManager.addKeyboardActionListener(pauseListener);
                    conductor.setPaused(false);
                }
            }
            case Paused -> {
                if (pauseMenu == null) {
                    pauseMenu = new PauseMenu(this);
                    drawableManager.addUIElement(pauseMenu);
                }

                pauseMenu.setShouldRender(true);
                inputManager.removeKeyboardActionListener(pauseListener);
                conductor.setPaused(true);
            }
            case Results -> conductor.setPaused(true);
        }
        gameState = next;
    }

    @Override
    public void eventReceived(ConductorFinishedEvent event) {
        FastJEngine.log("Conductor finished. Processing results...");
        changeState(GameState.Results);
        resultMenu = new ResultMenu(this, event);
    }
}
