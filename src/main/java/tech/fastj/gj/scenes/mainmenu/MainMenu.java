package tech.fastj.gj.scenes.mainmenu;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.gj.FastJGameJam2022;
import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.FilePaths;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.systems.audio.AudioEvent;
import tech.fastj.systems.audio.MemoryAudio;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;

import java.awt.Color;
import java.io.IOException;

public class MainMenu extends Scene {

    private MemoryAudio mainMenuMusic;

    public MainMenu() {
        super(SceneNames.MainMenu);
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(MainMenu.class, "loading {}", getSceneName());
        Pointf center = canvas.getCanvasCenter();

        Text2D titleText = Text2D.create(FastJGameJam2022.GameName)
            .withFill(Colors.Snowy)
            .withFont(Fonts.TitleTextFont)
            .build();
        titleText.setTranslation(Pointf.subtract(center, titleText.width() / 2f, 200f));
        drawableManager().addGameObject(titleText);

        BetterButton playButton = new BetterButton(this, Pointf.subtract(center, 225f, 50f), Shapes.ButtonSize);
        playButton.setText("Play Game");
        playButton.setFill(Color.darkGray);
        playButton.setFont(Fonts.ButtonTextFont);
        playButton.setOutlineColor(Colors.Snowy);
        playButton.setTextColor(Colors.Snowy);
        playButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.SongPicker, false), CoreLoopState.Update);
        });

        BetterButton infoButton = new BetterButton(this, Pointf.subtract(center, -25f, 50f), Shapes.ButtonSize);
        infoButton.setText("Information");
        infoButton.setFill(Color.darkGray);
        infoButton.setFont(Fonts.ButtonTextFont);
        infoButton.setOutlineColor(Colors.Snowy);
        infoButton.setTextColor(Colors.Snowy);
        infoButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.Information, false), CoreLoopState.Update);
        });

        BetterButton songEditorButton = new BetterButton(this, Pointf.subtract(center, 225f, -50f), Shapes.ButtonSize);
        songEditorButton.setText("Song Editor");
        songEditorButton.setFill(Color.darkGray);
        songEditorButton.setFont(Fonts.ButtonTextFont);
        songEditorButton.setOutlineColor(Colors.Snowy);
        songEditorButton.setTextColor(Colors.Snowy);
        songEditorButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.SongEditor), CoreLoopState.Update);
        });

        BetterButton settingsButton = new BetterButton(this, Pointf.subtract(center, -25f, -50f), Shapes.ButtonSize);
        settingsButton.setText("Settings");
        settingsButton.setFill(Color.darkGray);
        settingsButton.setFont(Fonts.ButtonTextFont);
        settingsButton.setOutlineColor(Colors.Snowy);
        settingsButton.setTextColor(Colors.Snowy);
        settingsButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.Settings, false), CoreLoopState.Update);
        });

        BetterButton exitButton = new BetterButton(this, Pointf.subtract(center, 100f, -150f), Shapes.ButtonSize);
        exitButton.setText("Quit Game");
        exitButton.setFill(Color.darkGray);
        exitButton.setFont(Fonts.ButtonTextFont);
        exitButton.setOutlineColor(Colors.Snowy);
        exitButton.setTextColor(Colors.Snowy);
        exitButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(FastJEngine.getDisplay()::close, CoreLoopState.Update);
        });

        mainMenuMusic = FastJEngine.getAudioManager().loadMemoryAudio(FilePaths.MainMenuMusic);
        mainMenuMusic.setLoopPoints(0.0096f, 0.951f);
        mainMenuMusic.setShouldLoop(true);
        mainMenuMusic.setLoopCount(MemoryAudio.ContinuousLoop);
        mainMenuMusic.play();

        Log.debug(MainMenu.class, "loaded {}", getSceneName());
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(MainMenu.class, "unloading {}", getSceneName());

        if (mainMenuMusic != null) {
            FastJEngine.getGameLoop().removeEventObserver(mainMenuMusic.getAudioEventListener(), AudioEvent.class);
            mainMenuMusic.stop();

            try {
                mainMenuMusic.getAudioInputStream().close();
            } catch (IOException exception) {
                Log.warn(MainMenu.class, "Error occurred while closing main menu music", exception);
            }

            mainMenuMusic = null;
        }

        Log.debug(MainMenu.class, "unloaded {}", getSceneName());
    }
}
