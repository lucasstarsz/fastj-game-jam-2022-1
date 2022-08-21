package tech.fastj.gj.scenes.information;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.ui.LinkText;
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
import java.net.MalformedURLException;
import java.net.URL;

public class InformationMenu extends Scene {

    public InformationMenu() {
        super(SceneNames.Information);
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(InformationMenu.class, "loading {}", getSceneName());
        Pointf center = canvas.getCanvasCenter();

        Text2D howToPlayHeader = Text2D.create("How to Play")
            .withFont(Fonts.SubtitleTextFont)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, 425f, 150f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(howToPlayHeader);

        Text2D controlsText = Text2D.create("When running a song, music notes will fall down from the top of the screen.")
            .withFont(Fonts.SmallStatTextFontPlain)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, 605f, 75f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(controlsText);

        Text2D gameAimText = Text2D.create("Time your key presses correctly to match when the note falls on the block.")
            .withFont(Fonts.SmallStatTextFontPlain)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, 605f, 50f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(gameAimText);

        Text2D themeText = Text2D.create("Just one problem: you're limited by the delayed reaction of your speakers!")
            .withFont(Fonts.SmallStatTextFontPlain)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, 605f, 25f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(themeText);

        Text2D sendoffText = Text2D.create("Try to keep up with the notes as they fall down.")
            .withFont(Fonts.SmallStatTextFontPlain)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, 605f, 0f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(sendoffText);

        Text2D creditsHeader = Text2D.create("Credits")
            .withFont(Fonts.SubtitleTextFont)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, -225f, 150f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(creditsHeader);

        Text2D creditsText = Text2D.create("All content was made by lucasstarsz -- even the game engine!")
            .withFont(Fonts.SmallStatTextFontPlain)
            .withFill(Colors.Snowy)
            .withTransform(Pointf.subtract(center, -80f, 75f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
            .build();
        drawableManager().addGameObject(creditsText);

        try {
            LinkText githubLink = new LinkText(this, "lucasstarsz's GitHub", new URL("https://github.com/lucasstarsz"));
            githubLink.setFont(Fonts.SmallStatTextFontBold);
            githubLink.setFill(Colors.Snowy);
            githubLink.setTranslation(Pointf.subtract(center, -222.5f, 50f));

            LinkText spotifyLink = new LinkText(this, "lucasstarsz's Spotify", new URL("https://soundcloud.com/lucas-z-43717769/"));
            spotifyLink.setFont(Fonts.SmallStatTextFontBold);
            spotifyLink.setFill(Colors.Snowy);
            spotifyLink.setTranslation(Pointf.subtract(center, -222.5f, 25f));
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }

        BetterButton mainMenuButton = new BetterButton(this, Pointf.subtract(center, 100f, -150f), Shapes.ButtonSize);
        mainMenuButton.setText("Back");
        mainMenuButton.setFill(Color.darkGray);
        mainMenuButton.setFont(Fonts.ButtonTextFont);
        mainMenuButton.setOutlineColor(Colors.Snowy);
        mainMenuButton.setTextColor(Colors.Snowy);
        mainMenuButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu), CoreLoopState.Update);
        });

        Log.debug(InformationMenu.class, "loaded {}", getSceneName());
    }
}
