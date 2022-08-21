package tech.fastj.gj.scenes.game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.event.EventObserver;
import tech.fastj.gj.gameobjects.KeyCircle;
import tech.fastj.gj.gameobjects.MusicNote;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.rhythm.ConductorFinishedEvent;
import tech.fastj.gj.rhythm.GameInputMatcher;
import tech.fastj.gj.rhythm.SongInfo;
import tech.fastj.gj.scripts.MusicNoteMovement;
import tech.fastj.gj.ui.ContentBox;
import tech.fastj.gj.ui.Notice;
import tech.fastj.gj.ui.PauseButton;
import tech.fastj.gj.user.User;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.util.DrawUtil;
import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.logging.Log;
import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.systems.audio.state.PlaybackState;
import tech.fastj.systems.control.Scene;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class MainGame extends Scene implements EventObserver<ConductorFinishedEvent> {

    private GameState gameState;
    private final User user;
    private Conductor conductor;
    private SongInfo songInfo;

    private ContentBox songNameBox;

    private PauseButton pauseButton;
    private PauseMenu pauseMenu;
    private KeyboardActionListener pauseListener;
    private boolean allowClicks;

    GameInputMatcher inputMatcher;

    private ResultMenu resultMenu;
    private final List<KeyCircle> keyCircles;

    public MainGame() {
        super(SceneNames.Game);
        user = User.getInstance();
        keyCircles = new ArrayList<>();
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isRhythmInputAllowed() {
        return allowClicks;
    }

    public void setSongInfo(SongInfo songInfo) {
        if (isInitialized()) {
            throw new IllegalStateException("bad");
        }

        this.songInfo = songInfo;
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(MainGame.class, "loading {}", getSceneName());

        resetConductor(canvas);
        createUI();
        createListeners();
        changeState(GameState.Intro);

        Log.debug(MainGame.class, "loaded {}", getSceneName());
    }

    private void createUI() {
        songNameBox = new ContentBox(this, "Now Playing");
        songNameBox.setTranslation(new Pointf(80f, 30f));
        songNameBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
        songNameBox.getStatDisplay().setFill(Colors.Snowy);
        songNameBox.setShouldRender(false);

        pauseButton = new PauseButton(this, new Pointf(20f, 20f), new Pointf(40f));
        pauseButton.setFill(Color.gray);
        pauseButton.setOutlineColor(Color.black);
        pauseButton.addOnAction(event -> {
            if (event.isConsumed() || gameState != GameState.Playing || conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
                return;
            }

            event.consume();
            FastJEngine.runLater(() -> changeState(GameState.Paused));
        });
        pauseButton.setShouldRender(false);
    }

    private void createListeners() {
        pauseListener = new KeyboardActionListener() {
            @Override
            public void onKeyReleased(KeyboardStateEvent event) {
                if (event.isConsumed() || gameState != GameState.Playing || conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
                    return;
                }

                if (event.getKey() == Keys.P || event.getKey() == Keys.Escape) {
                    event.consume();
                    FastJEngine.runLater(() -> changeState(GameState.Paused));
                }
            }
        };

        inputMatcher.setOnLaneKeyPressed(event -> {
            for (KeyCircle keyCircle : keyCircles) {
                if (keyCircle.getKey() == event.getKey()) {
                    keyCircle.setFill(Color.white, false);
                    return;
                }
            }
        });
    }

    private void resetConductor(FastJCanvas canvas) {
        conductor = new Conductor(songInfo, this, true);
        conductor.setSpawnMusicNote((note, noteLane) -> {
            Pointf noteStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (noteLane * Shapes.NoteSize * 2.5f), -Shapes.NoteSize / 2f);
            Color musicNoteColor = DrawUtil.randomColor();
            MusicNote musicNote = new MusicNote(noteStartingLocation, Shapes.NoteSize)
                .setFill(musicNoteColor)
                .setOutline(MusicNote.DefaultOutlineStroke, musicNoteColor.darker());

            double noteTravelDistance = canvas.getResolution().y - (Shapes.NoteSize * 4f);
            MusicNoteMovement musicNoteMovement = new MusicNoteMovement(conductor, note, noteTravelDistance);
            musicNote.addLateBehavior(musicNoteMovement, this);
            drawableManager().addGameObject(musicNote);
        });
        drawableManager().addGameObject(conductor);

        inputMatcher = new GameInputMatcher(
            conductor,
            songInfo,
            message -> {
                Notice notice = new Notice(message, new Pointf(20f, 40f), this);
                notice.setFill("Perfect!".equalsIgnoreCase(message) ? Color.green : Color.red.brighter());
                notice.setFont(Fonts.StatTextFont);
                drawableManager().addGameObject(notice);
            }
        );
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(MainGame.class, "unloading {}", getSceneName());

        FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class);
        gameState = null;
        allowClicks = false;

        user.resetScore();
        keyCircles.clear();

        if (pauseListener != null) {
            inputManager().removeKeyboardActionListener(pauseListener);
            pauseListener = null;
        }

        Log.info(MainGame.class, "unloaded {}", getSceneName());
    }

    @Override
    public void fixedUpdate(FastJCanvas canvas) {
        Log.info("listeners: {}", getBehaviorListeners().size());
    }

    public void changeState(GameState next) {
        Log.debug(MainGame.class, "changing state from {} to {}", gameState, next);

        switch (next) {
            case Intro -> {
                if (gameState == GameState.Results) {
                    songNameBox.setShouldRender(false);

                    resultMenu.destroy(this);
                    resultMenu = null;
                    resetConductor(FastJEngine.getCanvas());

                    user.resetScore();
                    FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class);
                }

                FastJEngine.runLater(() -> changeState(GameState.Playing));
            }
            case Playing -> {
                if (gameState == GameState.Intro) {
                    songNameBox.setContent(conductor.musicInfo.getSongName());
                    songNameBox.setShouldRender(true);

                    createLaneKeys(keyCircles);

                    FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
                    pauseButton.setShouldRender(true);
                } else if (gameState == GameState.Paused) {
                    pauseMenu.setShouldRender(false);
                    conductor.setPaused(false);
                }

                inputManager().addKeyboardActionListener(pauseListener);
                inputManager().addMouseActionListener(pauseButton);
                inputManager().addKeyboardActionListener(inputMatcher);
            }
            case Paused -> {
                if (pauseMenu == null) {
                    pauseMenu = new PauseMenu(this);
                    drawableManager().addUIElement(pauseMenu);
                }

                conductor.setPaused(true);
                pauseMenu.setShouldRender(true);

                inputManager().removeKeyboardActionListener(pauseListener);
                inputManager().removeMouseActionListener(pauseButton);
                inputManager().removeKeyboardActionListener(inputMatcher);
            }
            case Results -> {
                for (KeyCircle keyCircle : keyCircles) {
                    keyCircle.destroy(this);
                }
                keyCircles.clear();

                conductor.setPaused(true);
                conductor.destroy(this);
                pauseButton.setShouldRender(false);

                inputManager().removeKeyboardActionListener(pauseListener);
                inputManager().removeMouseActionListener(pauseButton);
                inputManager().removeKeyboardActionListener(inputMatcher);

                SwingUtilities.invokeLater(() -> FastJEngine.runLater(
                    () -> FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class)
                ));
            }
        }
        gameState = next;
    }

    public void createLaneKeys(List<KeyCircle> keyCircles) {
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

            KeyCircle keyCircle = (KeyCircle) new KeyCircle(laneKey, Shapes.NoteSize, "Tahoma", this)
                .setFill(Color.gray)
                .setOutline(KeyCircle.DefaultOutlineStroke, KeyCircle.DefaultOutlineColor)
                .setTranslation(laneStartingLocation);

            keyCircles.add(keyCircle);
            drawableManager().addGameObject(keyCircle);
        }
    }

    @Override
    public void eventReceived(ConductorFinishedEvent event) {
        FastJEngine.log("Conductor finished. Processing results...");
        changeState(GameState.Results);
        resultMenu = new ResultMenu(this, event);
    }
}
