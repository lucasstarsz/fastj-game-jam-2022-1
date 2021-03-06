package tech.fastj.gj.scenes.game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.event.GameEventObserver;
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
import tech.fastj.math.Pointf;
import tech.fastj.systems.audio.state.PlaybackState;
import tech.fastj.systems.control.Scene;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainGame extends Scene implements GameEventObserver<ConductorFinishedEvent> {

    private GameState gameState;
    private User user;
    private Conductor conductor;
    private SongInfo songInfo;

    private ContentBox songNameBox;

    private PauseButton pauseButton;
    private PauseMenu pauseMenu;
    private KeyboardActionListener pauseListener;
    private boolean allowClicks;

    private ResultMenu resultMenu;
    private List<KeyCircle> keyCircles;
    private List<MusicNote> musicNotes;

    public MainGame() {
        super(SceneNames.Game);
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
            conductor.destroy(this);
            conductor.musicSource.stop();
            conductor = null;
        }

        if (songNameBox != null) {
            songNameBox.destroy(this);
            songNameBox = null;
        }

        if (keyCircles != null) {
            for (KeyCircle keyCircle : keyCircles) {
                keyCircle.destroy(this);
            }
            keyCircles.clear();
            keyCircles = null;
        }

        if (musicNotes != null) {
            for (MusicNote musicNote : musicNotes) {
                musicNote.destroy(this);
            }
            musicNotes.clear();
            musicNotes = null;
        }

        if (pauseMenu != null) {
            pauseMenu.destroy(this);
            pauseMenu = null;
        }

        if (pauseButton != null) {
            pauseButton.destroy(this);
            pauseButton = null;
        }

        if (pauseListener != null) {
            inputManager.removeKeyboardActionListener(pauseListener);
            pauseListener = null;
        }

        if (resultMenu != null) {
            resultMenu.destroy(this);
            resultMenu = null;
        }

        FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class);

        setInitialized(false);
        Log.info(MainGame.class, "unloaded {}", getSceneName());
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
                    songNameBox.destroy(this);
                    songNameBox = null;

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

                    conductor = new Conductor(songInfo, this, true);
                    FastJCanvas canvas = FastJEngine.getCanvas();

                    musicNotes = new ArrayList<>();
                    conductor.setSpawnMusicNote((note, noteLane) -> {
                        Pointf noteStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (noteLane * Shapes.NoteSize * 2.5f), -Shapes.NoteSize / 2f);
                        Color musicNoteColor = DrawUtil.randomColor();
                        MusicNote musicNote = new MusicNote(noteStartingLocation, Shapes.NoteSize)
                                .setFill(musicNoteColor)
                                .setOutline(MusicNote.DefaultOutlineStroke, musicNoteColor.darker());

                        double noteTravelDistance = canvas.getResolution().y - (Shapes.NoteSize * 4f);
                        MusicNoteMovement musicNoteMovement = new MusicNoteMovement(conductor, note, noteTravelDistance);
                        musicNote.addLateBehavior(musicNoteMovement, this);
                        musicNotes.add(musicNote);
                        drawableManager.addGameObject(musicNote);
                    });

                    songNameBox = new ContentBox(this, "Now Playing", conductor.musicInfo.getSongName());
                    songNameBox.setTranslation(new Pointf(80f, 30f));
                    songNameBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                    songNameBox.getStatDisplay().setFill(Colors.Snowy);
                    drawableManager.addUIElement(songNameBox);

                    Collection<Keys> laneKeys = conductor.musicInfo.getLaneKeys();
                    int laneKeyIncrement = 1;
                    keyCircles = new ArrayList<>();
                    for (Keys laneKey : laneKeys) {
                        Pointf laneStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (laneKeyIncrement * Shapes.NoteSize * 2.5f), canvas.getResolution().y - (Shapes.NoteSize * 4f));
                        KeyCircle keyCircle = (KeyCircle) new KeyCircle(laneKey, Shapes.NoteSize, "Tahoma", this)
                                .setFill(Color.gray)
                                .setOutline(KeyCircle.DefaultOutlineStroke, KeyCircle.DefaultOutlineColor)
                                .setTranslation(laneStartingLocation);
                        keyCircles.add(keyCircle);
                        drawableManager.addGameObject(keyCircle);
                        laneKeyIncrement++;
                    }

                    drawableManager.addGameObject(conductor);

                    GameInputMatcher inputMatcher = new GameInputMatcher(
                            conductor,
                            songInfo,
                            message -> {
                                Notice notice = new Notice(message, new Pointf(20f, 40f), this);
                                notice.setFill("Perfect!".equalsIgnoreCase(message) ? Color.green : Color.red.brighter());
                                notice.setFont(Fonts.StatTextFont);
                                drawableManager.addGameObject(notice);
                            }
                    );
                    inputManager.addKeyboardActionListener(inputMatcher);
                    inputMatcher.setOnLaneKeyPressed(event -> {
                        for (KeyCircle keyCircle : keyCircles) {
                            if (keyCircle.getKey() == event.getKey()) {
                                keyCircle.setFill(Color.white, false);
                                return;
                            }
                        }
                    });
                    FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);

                    pauseButton = new PauseButton(this, new Pointf(20f, 20f), new Pointf(40f));
                    pauseButton.setFill(Color.gray);
                    pauseButton.setOutlineColor(Color.black);
                    pauseButton.addOnAction(event -> {
                        if (event.isConsumed() || gameState != GameState.Playing || conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
                            return;
                        }

                        event.consume();
                        FastJEngine.runAfterUpdate(() -> changeState(GameState.Paused));
                    });

                    pauseListener = new KeyboardActionListener() {
                        @Override
                        public void onKeyReleased(KeyboardStateEvent event) {
                            if (event.isConsumed() || gameState != GameState.Playing || conductor.musicSource.getCurrentPlaybackState() != PlaybackState.Playing) {
                                return;
                            }

                            if (event.getKey() == Keys.P || event.getKey() == Keys.Escape) {
                                event.consume();
                                FastJEngine.runAfterUpdate(() -> changeState(GameState.Paused));
                            }
                        }
                    };
                } else if (gameState == GameState.Paused) {
                    pauseMenu.setShouldRender(false);
                    conductor.setPaused(false);
                    inputManager.addMouseActionListener(pauseButton);
                }
                inputManager.addKeyboardActionListener(pauseListener);
            }
            case Paused -> {
                if (pauseMenu == null) {
                    pauseMenu = new PauseMenu(this);
                    drawableManager.addUIElement(pauseMenu);
                }

                conductor.setPaused(true);
                pauseMenu.setShouldRender(true);
                inputManager.removeKeyboardActionListener(pauseListener);
                inputManager.removeMouseActionListener(pauseButton);
            }
            case Results -> {
                if (keyCircles != null) {
                    for (KeyCircle keyCircle : keyCircles) {
                        keyCircle.destroy(this);
                    }
                    keyCircles.clear();
                    keyCircles = null;
                }

                if (musicNotes != null) {
                    for (MusicNote musicNote : musicNotes) {
                        musicNote.destroy(this);
                    }
                    musicNotes.clear();
                    musicNotes = null;
                }

                if (pauseButton != null) {
                    pauseButton.destroy(this);
                    pauseButton = null;
                }

                conductor.setPaused(true);
                inputManager.removeKeyboardActionListener(pauseListener);
                SwingUtilities.invokeLater(() -> FastJEngine.runAfterUpdate(() -> FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class)));
            }
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
