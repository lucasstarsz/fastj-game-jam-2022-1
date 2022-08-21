package tech.fastj.gj.scenes.settings;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.gj.ui.ArrowButton;
import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.user.User;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;

import java.awt.Color;

public class Settings extends Scene {

    private final User user = User.getInstance();

    private Text2D difficultyText;
    private ArrowButton gameDifficulties;

    public Settings() {
        super(SceneNames.Settings);
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(Settings.class, "loading {}", getSceneName());
        Pointf center = canvas.getCanvasCenter();

        Text2D titleText = Text2D.create("Settings")
            .withFont(Fonts.TitleTextFont)
            .withTransform(Pointf.subtract(center, 100f, 200f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(titleText);

//        difficultyText = Text2D.create("Game Difficulty")
//                .withFont(Fonts.StatTextFont)
//                .withTransform(Pointf.subtract(center, 65f, 30f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
//                .build();
//        drawableManager.addGameObject(difficultyText);

//        gameDifficulties = new ArrowButton(this, Pointf.subtract(center, 200f, 0f), Shapes.ButtonSize.copy().multiply(2f, 1.25f), GameStartDifficulty.DifficultiesList, 1);
//        gameDifficulties.setFill(Color.white);
//        gameDifficulties.setFont(Fonts.ButtonTextFont);
//        gameDifficulties.getArrowLeft().setFill(Colors.LightSnowy);
//        gameDifficulties.getArrowRight().setFill(Colors.LightSnowy);
//        gameDifficulties.setOnAction(mouseButtonEvent -> {
//            mouseButtonEvent.consume();
//            user.getSettings().setGameStartDifficulty(GameStartDifficulty.values()[gameDifficulties.getSelectedOption()]);
//        });

        BetterButton mainMenuButton = new BetterButton(this, canvas.getCanvasCenter().add(-100f, 150f), Shapes.ButtonSize);
        mainMenuButton.setText("Back");
        mainMenuButton.setFill(Color.darkGray);
        mainMenuButton.setFont(Fonts.ButtonTextFont);
        mainMenuButton.setOutlineColor(Colors.Snowy);
        mainMenuButton.setTextColor(Colors.Snowy);
        mainMenuButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu), CoreLoopState.Update);
        });

        Log.debug(Settings.class, "loaded {}", getSceneName());
    }
}
