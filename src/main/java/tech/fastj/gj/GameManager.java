package tech.fastj.gj;

import tech.fastj.engine.FastJEngine;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.RenderSettings;

import tech.fastj.systems.control.SceneManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import tech.fastj.gj.scenes.editor.SongEditor;
import tech.fastj.gj.scenes.game.MainGame;
import tech.fastj.gj.scenes.information.InformationMenu;
import tech.fastj.gj.scenes.mainmenu.MainMenu;
import tech.fastj.gj.scenes.settings.Settings;

public class GameManager extends SceneManager {

    private final MainMenu mainMenu = new MainMenu();
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

        canvas.modifyRenderSettings(RenderSettings.Antialiasing.Enable);
        addScene(mainMenu);
        addScene(settings);
        addScene(informationMenu);
        addScene(mainGame);
        addScene(songEditor);
        setCurrentScene(mainMenu);
        loadCurrentScene();
    }
}
