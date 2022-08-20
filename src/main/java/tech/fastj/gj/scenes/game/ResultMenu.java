package tech.fastj.gj.scenes.game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.gj.rhythm.ConductorFinishedEvent;
import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.ui.ContentBox;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.RenderStyle;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.UIElement;
import tech.fastj.graphics.util.DrawUtil;
import tech.fastj.input.mouse.events.MouseActionEvent;
import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;
import tech.fastj.systems.control.GameHandler;
import tech.fastj.systems.control.SceneManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class ResultMenu extends UIElement<MouseActionEvent> {

    private Polygon2D alphaScreen;
    private Polygon2D backgroundScreen;
    private Text2D gameEndText;
    private ContentBox scoreBox;
    private ContentBox blocksStackedBox;
    private BetterButton playAgainButton;
    private BetterButton mainMenuButton;
    private BetterButton quitGameButton;

    public ResultMenu(MainGame origin, ConductorFinishedEvent event) {
        super(origin);

        Pointf center = FastJEngine.getCanvas().getCanvasCenter();
        Point end = FastJEngine.getCanvas().getResolution().copy();
        Pointf[] backgroundMesh = DrawUtil.createBox(50f, 50f, Point.subtract(end, 120, 140).asPointf());
        Pointf[] alphaMesh = DrawUtil.createBox(0f, 0f, end.asPointf());

        setCollisionPath(DrawUtil.createPath(backgroundMesh));

        alphaScreen = Polygon2D.create(alphaMesh)
                .withFill(Colors.darkGray(25).darker().darker())
                .withRenderStyle(RenderStyle.Fill)
                .build();

        backgroundScreen = Polygon2D.create(backgroundMesh)
                .withFill(new Color(Color.lightGray.getRed(), Color.lightGray.getGreen(), Color.lightGray.getBlue(), 15))
                .withOutline(Shapes.ThickerRoundedStroke, Color.black)
                .withRenderStyle(RenderStyle.FillAndOutline)
                .build();

        gameEndText = Text2D.create("Game Ended.")
                .withFont(Fonts.TitleTextFont)
                .withTransform(Pointf.subtract(center, 160f), Transform2D.DefaultRotation, Transform2D.DefaultScale)
                .build();

        scoreBox = new ContentBox(
                origin,
                "Total Notes",
                "" + event.getTotalNotesOverall()
        );

        playAgainButton = new BetterButton(origin, backgroundScreen.getCenter().subtract(100f, 0f), Shapes.ButtonSize);
        mainMenuButton = new BetterButton(origin, backgroundScreen.getCenter().subtract(100f, -75f), Shapes.ButtonSize);
        quitGameButton = new BetterButton(origin, backgroundScreen.getCenter().subtract(100f, -150f), Shapes.ButtonSize);

        setup(center);

        playAgainButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> origin.changeState(GameState.Intro), CoreLoopState.Update);
        });
        mainMenuButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu), CoreLoopState.Update);
        });

        origin.drawableManager.removeUIElement(playAgainButton);
        origin.drawableManager.removeUIElement(mainMenuButton);
        origin.drawableManager.removeUIElement(quitGameButton);
        origin.drawableManager.removeUIElement(scoreBox);
//        origin.drawableManager.removeUIElement(blocksStackedBox);
    }

    private void setup(Pointf center) {
        if (gameEndText != null) {
            gameEndText.setFill(Colors.Snowy);
        }

        if (scoreBox != null) {
            scoreBox.getStatDisplay().setFont(Fonts.StatTextFont);
            scoreBox.getStatDisplay().setFill(Colors.Snowy);
            scoreBox.translate(Pointf.subtract(center, scoreBox.width(), 90f));
        }

//        blocksStackedBox = new ContentBox(
//                origin,
//                "Blocks Stacked",
//                user.getHasHighBlocksStacked() ? user.getNumberStacked() + " (New Record!)" : user.getNumberStacked() + ""
//        );
//        blocksStackedBox.getStatDisplay().setFont(Fonts.StatTextFont);
//        blocksStackedBox.translate(Pointf.subtract(center, 100f, 65f));

        if (playAgainButton != null) {
            playAgainButton.setText("Play Again");
            playAgainButton.setFill(Color.darkGray);
            playAgainButton.setFont(Fonts.ButtonTextFont);
            playAgainButton.setOutlineColor(Colors.Snowy);
            playAgainButton.setTextColor(Colors.Snowy);
        }

        if (mainMenuButton != null) {
            mainMenuButton.setText("Main Menu");
            mainMenuButton.setFill(Color.darkGray);
            mainMenuButton.setFont(Fonts.ButtonTextFont);
            mainMenuButton.setOutlineColor(Colors.Snowy);
            mainMenuButton.setTextColor(Colors.Snowy);
        }

        if (quitGameButton != null) {
            quitGameButton.setText("Quit Game");
            quitGameButton.setFill(Color.darkGray);
            quitGameButton.setFont(Fonts.ButtonTextFont);
            quitGameButton.setOutlineColor(Colors.Snowy);
            quitGameButton.setTextColor(Colors.Snowy);
            quitGameButton.setOnAction(mouseButtonEvent -> {
                mouseButtonEvent.consume();
                FastJEngine.runLater(FastJEngine.getDisplay()::close, CoreLoopState.Update);
            });
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        g.transform(getTransformation());

        alphaScreen.render(g);
        backgroundScreen.render(g);
        gameEndText.render(g);
        scoreBox.render(g);
//        blocksStackedBox.render(g);
        playAgainButton.render(g);
        mainMenuButton.render(g);
        quitGameButton.render(g);

        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(GameHandler origin) {
        super.destroyTheRest(origin);
        if (alphaScreen != null) {
            alphaScreen.destroy(origin);
            alphaScreen = null;
        }

        if (backgroundScreen != null) {
            backgroundScreen.destroy(origin);
            backgroundScreen = null;
        }

        if (gameEndText != null) {
            gameEndText.destroy(origin);
            gameEndText = null;
        }

        if (scoreBox != null) {
            scoreBox.destroy(origin);
            scoreBox = null;
        }

        if (blocksStackedBox != null) {
            blocksStackedBox.destroy(origin);
            blocksStackedBox = null;
        }

        if (playAgainButton != null) {
            playAgainButton.destroy(origin);
            playAgainButton = null;
        }

        if (mainMenuButton != null) {
            mainMenuButton.destroy(origin);
            mainMenuButton = null;
        }

        if (quitGameButton != null) {
            quitGameButton.destroy(origin);
            quitGameButton = null;
        }
    }
}
