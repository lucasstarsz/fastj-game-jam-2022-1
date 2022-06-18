package tech.fastj.gj.scenes.songpicker;

import com.google.gson.Gson;
import tech.fastj.engine.FastJEngine;
import tech.fastj.gj.GameManager;
import tech.fastj.gj.rhythm.SongInfo;
import tech.fastj.gj.scenes.game.MainGame;
import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.FilePaths;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.SimpleDisplay;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;

import javax.swing.*;
import java.awt.*;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SongPicker extends Scene {

    private Text2D titleText;
    private BetterButton playStackAttackButton;
    private BetterButton playLadybirdButton;
    private BetterButton playCustomButton;
    private BetterButton browseCustomButton;
    private BetterButton mainMenuButton;
    private Text2D customSongInfo;
    private SongInfo customSong;

    public SongPicker() {
        super(SceneNames.SongPicker);
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(SongPicker.class, "loading {}", getSceneName());
        Pointf center = canvas.getCanvasCenter();

        titleText = Text2D.create("Select a Song")
                .withFill(Colors.Snowy)
                .withFont(Fonts.TitleTextFont)
                .withTransform(Pointf.subtract(center, 155f, 200f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        drawableManager.addGameObject(titleText);

        playStackAttackButton = new BetterButton(this, Pointf.subtract(center, 225f, 50f), Shapes.ButtonSize);
        playStackAttackButton.setText("Play Stack Attack");
        playStackAttackButton.setFill(Color.darkGray);
        playStackAttackButton.setFont(Fonts.ButtonTextFont);
        playStackAttackButton.setOutlineColor(Colors.Snowy);
        playStackAttackButton.setTextColor(Colors.Snowy);
        playStackAttackButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runAfterRender(() -> {
                try {
                    String json = Files.readString(FilePaths.StackAttackJson);
                    Gson gson = new Gson();
                    SongInfo stackAttackInfo = gson.fromJson(json, SongInfo.class);
                    GameManager sceneManager = FastJEngine.getLogicManager();

                    Scene mainMenuScene = sceneManager.getScene(SceneNames.MainMenu);
                    mainMenuScene.inputManager.unload();
                    mainMenuScene.unload(canvas);

                    sceneManager.<MainGame>getScene(SceneNames.Game).setSongInfo(stackAttackInfo);
                    sceneManager.switchScenes(SceneNames.Game);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        });

        playLadybirdButton = new BetterButton(this, Pointf.subtract(center, -25f, 50f), Shapes.ButtonSize);
        playLadybirdButton.setText("Play Ladybird");
        playLadybirdButton.setFill(Color.darkGray);
        playLadybirdButton.setFont(Fonts.ButtonTextFont);
        playLadybirdButton.setOutlineColor(Colors.Snowy);
        playLadybirdButton.setTextColor(Colors.Snowy);
        playLadybirdButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runAfterRender(() -> {
                try {
                    String json = Files.readString(FilePaths.LadybirdJson);
                    Gson gson = new Gson();
                    SongInfo ladybirdInfo;
                    ladybirdInfo = gson.fromJson(json, SongInfo.class);
                    GameManager sceneManager = FastJEngine.getLogicManager();

                    Scene mainMenuScene = sceneManager.getScene(SceneNames.MainMenu);
                    mainMenuScene.inputManager.unload();
                    mainMenuScene.unload(canvas);

                    sceneManager.<MainGame>getScene(SceneNames.Game).setSongInfo(ladybirdInfo);
                    sceneManager.switchScenes(SceneNames.Game);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        });

        playCustomButton = new BetterButton(this, Pointf.subtract(center, 225f, -50f), Shapes.ButtonSize);
        playCustomButton.setText("Play Custom");
        playCustomButton.setFill(Color.darkGray);
        playCustomButton.setFont(Fonts.ButtonTextFont);
        playCustomButton.setOutlineColor(Colors.Snowy);
        playCustomButton.setTextColor(Colors.Snowy);
        playCustomButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();

            if (customSongInfo == null) {
                SwingUtilities.invokeLater(() -> {
                    DialogUtil.showMessageDialog(
                            DialogConfig.create()
                                    .withTitle("No Custom Song Loaded")
                                    .withPrompt("You haven't loaded a custom song yet! Please click \"Browse...\" in order to select a custom song.")
                                    .build()
                    );
                });
                return;
            }

            FastJEngine.runAfterRender(() -> {
                GameManager sceneManager = FastJEngine.getLogicManager();

                Scene mainMenuScene = sceneManager.getScene(SceneNames.MainMenu);
                mainMenuScene.inputManager.unload();
                mainMenuScene.unload(canvas);

                sceneManager.<MainGame>getScene(SceneNames.Game).setSongInfo(customSong);
                sceneManager.switchScenes(SceneNames.Game);
            });
        });

        browseCustomButton = new BetterButton(this, Pointf.subtract(center, -25f, -50f), Shapes.ButtonSize);
        browseCustomButton.setText("Browse...");
        browseCustomButton.setFill(Color.darkGray);
        browseCustomButton.setFont(Fonts.ButtonTextFont);
        browseCustomButton.setOutlineColor(Colors.Snowy);
        browseCustomButton.setTextColor(Colors.Snowy);
        browseCustomButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            SwingUtilities.invokeLater(() -> {
                String path = browseForPath(
                        "Choose a custom song file to load.",
                        FileDialog.LOAD,
                        (dir, name) -> name.endsWith(".json"),
                        "Song must be of JSON (.json) format.",
                        ".json"
                );

                if (path == null) {
                    return;
                }

                try {
                    String json = Files.readString(Path.of(path));
                    Gson gson = new Gson();
                    setCustomSong(gson.fromJson(json, SongInfo.class));
                } catch (Exception exception) {
                    DialogUtil.showMessageDialog(
                            DialogConfig.create()
                                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                    .withTitle("Couldn't load song data")
                                    .withPrompt("There was an error while loading the song data: " + exception.getMessage() + ". No data was loaded.")
                                    .build()
                    );
                }
            });
        });

        customSongInfo = Text2D.fromText("No custom song loaded.");
        customSongInfo.setFont(Fonts.StatTextFont);
        customSongInfo.setFill(Colors.Snowy);
        customSongInfo.setTranslation(Pointf.subtract(center, 225f, -125f));
        drawableManager.addGameObject(customSongInfo);

        mainMenuButton = new BetterButton(this, Pointf.subtract(center, 100f, -175f), Shapes.ButtonSize);
        mainMenuButton.setText("Back");
        mainMenuButton.setFill(Color.darkGray);
        mainMenuButton.setFont(Fonts.ButtonTextFont);
        mainMenuButton.setOutlineColor(Colors.Snowy);
        mainMenuButton.setTextColor(Colors.Snowy);
        mainMenuButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runAfterRender(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu));
        });

        Log.debug(SongPicker.class, "loaded {}", getSceneName());
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(SongPicker.class, "unloading {}", getSceneName());
        if (titleText != null) {
            titleText.destroy(this);
            titleText = null;
        }

        if (playStackAttackButton != null) {
            playStackAttackButton.destroy(this);
            playStackAttackButton = null;
        }

        if (playLadybirdButton != null) {
            playLadybirdButton.destroy(this);
            playLadybirdButton = null;
        }

        if (browseCustomButton != null) {
            browseCustomButton.destroy(this);
            browseCustomButton = null;
        }

        if (playCustomButton != null) {
            playCustomButton.destroy(this);
            playCustomButton = null;
        }

        if (customSongInfo != null) {
            customSongInfo.destroy(this);
            customSongInfo = null;
        }

        if (mainMenuButton != null) {
            mainMenuButton.destroy(this);
            mainMenuButton = null;
        }

        setInitialized(false);
        Log.debug(SongPicker.class, "unloaded {}", getSceneName());
    }

    @Override
    public void fixedUpdate(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }

    private void setCustomSong(SongInfo songInfo) {
        if (customSong != null && customSong.equals(songInfo)) {
            return;
        }
        customSong = songInfo;
        customSongInfo.setText("Custom Song: " + customSong.getSongName());
    }

    private String browseForPath(String title, int fileDialogType, FilenameFilter filter, String invalidFormatMessage, String firstFileType) {
        String file = null;
        String directory = null;

        while (file == null) {
            FileDialog songLoaderDialog = new FileDialog(FastJEngine.<SimpleDisplay>getDisplay().getWindow(), title, fileDialogType);
            songLoaderDialog.setDirectory(System.getProperty("user.home"));
            songLoaderDialog.setFilenameFilter(filter);
            songLoaderDialog.setMultipleMode(false);
            songLoaderDialog.setVisible(true);
            file = songLoaderDialog.getFile();
            directory = songLoaderDialog.getDirectory();

            if (file == null) {
                return null;
            }

            if (!file.endsWith(firstFileType)) {
                boolean matchOtherFileType = false;

                if (!matchOtherFileType) {
                    DialogUtil.showMessageDialog(
                            DialogConfig.create()
                                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                    .withTitle("Invalid file format")
                                    .withPrompt(invalidFormatMessage)
                                    .build()
                    );
                    file = null;
                }
            }
        }

        return directory + file;
    }
}
