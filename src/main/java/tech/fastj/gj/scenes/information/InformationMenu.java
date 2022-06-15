package tech.fastj.gj.scenes.information;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.elements.Button;

import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;

import java.awt.Color;

import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;

public class InformationMenu extends Scene {

    private Text2D howToPlayHeader;
    private Text2D controlsText;
    private Text2D gameAimText;

    private Text2D creditsHeader;
    private Text2D creditsText;
    private Button mainMenuButton;

    public InformationMenu() {
        super(SceneNames.Information);
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(InformationMenu.class, "loading {}", getSceneName());
        Pointf center = canvas.getCanvasCenter();

        howToPlayHeader = Text2D.create("How to Play")
                .withFont(Fonts.SubtitleTextFont)
                .withTransform(Pointf.subtract(center, 400f, 150f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        drawableManager.addGameObject(howToPlayHeader);

        controlsText = Text2D.create("Notes will fall down from the top of the screen.")
                .withFont(Fonts.SmallStatTextFont)
                .withTransform(Pointf.subtract(center, 425f, 75f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        drawableManager.addGameObject(controlsText);

        gameAimText = Text2D.create("Time your key presses correctly to match when the note falls on the block.")
                .withFont(Fonts.SmallStatTextFont)
                .withTransform(Pointf.subtract(center, 500f, 50f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        drawableManager.addGameObject(gameAimText);

        creditsHeader = Text2D.create("Credits")
                .withFont(Fonts.SubtitleTextFont)
                .withTransform(Pointf.subtract(center, -200f, 150f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        drawableManager.addGameObject(creditsHeader);

        creditsText = Text2D.create("All content was made by lucasstarsz -- even the game engine!")
                .withFont(Fonts.SmallStatTextFont)
                .withTransform(Pointf.subtract(center, -75f, 75f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();
        drawableManager.addGameObject(creditsText);

        mainMenuButton = new Button(this, Pointf.subtract(center, 100f, -150f), Shapes.ButtonSize);
        mainMenuButton.setText("Back");
        mainMenuButton.setFill(Color.white);
        mainMenuButton.setFont(Fonts.ButtonTextFont);
        mainMenuButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runAfterRender(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu));
        });

        Log.debug(InformationMenu.class, "loaded {}", getSceneName());
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(InformationMenu.class, "unloading {}", getSceneName());

        if (howToPlayHeader != null) {
            howToPlayHeader.destroy(this);
            howToPlayHeader = null;
        }

        if (controlsText != null) {
            controlsText.destroy(this);
            controlsText = null;
        }

        if (gameAimText != null) {
            gameAimText.destroy(this);
            gameAimText = null;
        }

        if (creditsHeader != null) {
            creditsHeader.destroy(this);
            creditsHeader = null;
        }

        if (creditsText != null) {
            creditsText.destroy(this);
            creditsText = null;
        }

        if (mainMenuButton != null) {
            mainMenuButton.destroy(this);
            mainMenuButton = null;
        }

        setInitialized(false);

        Log.debug(InformationMenu.class, "unloaded {}", getSceneName());
    }

    @Override
    public void fixedUpdate(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
    }
}
