package tech.fastj.gj.scenes.game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.Drawable;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogOptions;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.SimpleDisplay;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.RenderStyle;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.UIElement;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.mouse.events.MouseActionEvent;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;
import tech.fastj.systems.control.SimpleManager;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;

public class PauseMenu extends UIElement<MouseActionEvent> {

    private final MainGame origin;
    private Polygon2D backgroundScreen;
    private Text2D pausedText;
    private BetterButton resumeButton;
    private BetterButton mainMenuButton;

    public PauseMenu(MainGame origin) {
        super(origin);

        Pointf center = FastJEngine.getCanvas().getCanvasCenter();
        Point end = FastJEngine.getCanvas().getResolution().copy();
        Pointf[] backgroundMesh = DrawUtil.createBox(50f, 50f, end.subtract(120, 140).asPointf());

        setCollisionPath(DrawUtil.createPath(backgroundMesh));

        backgroundScreen = Polygon2D.create(backgroundMesh)
                .withFill(Colors.gray(50))
                .withOutline(Shapes.ThickerRoundedStroke, Color.black)
                .withRenderStyle(RenderStyle.FillAndOutline)
                .build();

        pausedText = Text2D.create("Game Paused")
                .withFont(Fonts.TitleTextFont)
                .withFill(Colors.Snowy)
                .build();
        pausedText.setTranslation(Pointf.subtract(center, (pausedText.width() / 2f) + (Fonts.TitleTextFont.getSize2D() * 0.1f), 100f));

        resumeButton = new BetterButton(origin, backgroundScreen.getCenter().add(-100f, 50f), Shapes.ButtonSize);
        resumeButton.setText("Resume Game");
        resumeButton.setFill(Color.darkGray);
        resumeButton.setFont(Fonts.ButtonTextFont);
        resumeButton.setOutlineColor(Colors.Snowy);
        resumeButton.setTextColor(Colors.Snowy);
        resumeButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runAfterRender(() -> origin.changeState(GameState.Playing));
        });

        mainMenuButton = new BetterButton(origin, backgroundScreen.getCenter().add(-100f, 125f), Shapes.ButtonSize);
        mainMenuButton.setText("Main Menu");
        mainMenuButton.setFill(Color.darkGray);
        mainMenuButton.setFont(Fonts.ButtonTextFont);
        mainMenuButton.setOutlineColor(Colors.Snowy);
        mainMenuButton.setTextColor(Colors.Snowy);
        mainMenuButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            SwingUtilities.invokeLater(() -> {
                boolean confirmReturn = DialogUtil.showConfirmationDialog(
                        DialogConfig.create()
                                .withTitle("Return to Main Menu")
                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                .withPrompt("Return to main menu? Any unfinished gameplay will be lost.")
                                .build(),
                        DialogOptions.YesNoCancel
                );

                if (confirmReturn) {
                    FastJEngine.runAfterRender(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu));
                }
            });
        });

        origin.drawableManager.addUIElement(this);
        origin.drawableManager.removeUIElement(resumeButton);
        origin.drawableManager.removeUIElement(mainMenuButton);

        this.origin = origin;
    }

    @Override
    public Drawable setShouldRender(boolean shouldBeRendered) {
        if (shouldBeRendered == shouldRender()) {
            return this;
        }

        if (shouldBeRendered) {
            if (resumeButton != null) {
                origin.inputManager.addMouseActionListener(resumeButton);
            }

            if (mainMenuButton != null) {
                origin.inputManager.addMouseActionListener(mainMenuButton);
            }
        } else {
            if (resumeButton != null) {
                origin.inputManager.removeMouseActionListener(resumeButton);
            }

            if (mainMenuButton != null) {
                origin.inputManager.removeMouseActionListener(mainMenuButton);
            }
        }

        return super.setShouldRender(shouldBeRendered);
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        g.transform(getTransformation());

        backgroundScreen.render(g);
        pausedText.render(g);
        resumeButton.render(g);
        mainMenuButton.render(g);

        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(Scene origin) {
        super.destroyTheRest(origin);
        if (backgroundScreen != null) {
            backgroundScreen.destroy(origin);
            backgroundScreen = null;
        }

        if (pausedText != null) {
            pausedText.destroy(origin);
            pausedText = null;
        }

        if (resumeButton != null) {
            resumeButton.destroy(origin);
            resumeButton = null;
        }

        if (mainMenuButton != null) {
            mainMenuButton.destroy(origin);
            mainMenuButton = null;
        }
    }

    @Override
    public void destroy(SimpleManager origin) {
        super.destroyTheRest(origin);
        if (backgroundScreen != null) {
            backgroundScreen.destroy(origin);
            backgroundScreen = null;
        }

        if (pausedText != null) {
            pausedText.destroy(origin);
            pausedText = null;
        }

        if (resumeButton != null) {
            resumeButton.destroy(origin);
            resumeButton = null;
        }

        if (mainMenuButton != null) {
            mainMenuButton.destroy(origin);
            mainMenuButton = null;
        }
    }
}
