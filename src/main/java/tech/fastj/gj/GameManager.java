package tech.fastj.gj;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gj.scenes.editor.SongEditor;
import tech.fastj.gj.scenes.game.MainGame;
import tech.fastj.gj.scenes.information.InformationMenu;
import tech.fastj.gj.scenes.mainmenu.MainMenu;
import tech.fastj.gj.scenes.settings.Settings;
import tech.fastj.gj.scenes.songpicker.SongPicker;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;
import tech.fastj.graphics.display.SimpleDisplay;
import tech.fastj.systems.control.SceneManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameManager extends SceneManager {

    private final MainMenu mainMenu = new MainMenu();
    private final SongPicker songPicker = new SongPicker();
    private final InformationMenu informationMenu = new InformationMenu();
    private final Settings settings = new Settings();
    private final SongEditor songEditor = new SongEditor();
    private final MainGame mainGame = new MainGame();

    @Override
    public void init(FastJCanvas canvas) {
        FastJEngine.getDisplay().getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        FastJEngine.<SimpleDisplay>getDisplay().getWindow().setResizable(false);
        canvas.modifyRenderSettings(RenderSettings.Antialiasing.Enable);
        addScene(mainMenu);
        addScene(songPicker);
        addScene(settings);
        addScene(informationMenu);
        addScene(mainGame);
        addScene(songEditor);
        setCurrentScene(mainMenu);
        loadCurrentScene();
    }
}
