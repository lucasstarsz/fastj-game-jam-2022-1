package tech.fastj.gj.scenes.game;

import tech.fastj.engine.FastJEngine;
import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.Drawable;
import tech.fastj.graphics.game.Polygon2D;
import tech.fastj.graphics.game.RenderStyle;
import tech.fastj.graphics.game.Text2D;
import tech.fastj.graphics.ui.UIElement;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.mouse.events.MouseActionEvent;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import tech.fastj.gj.ui.BetterButton;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.Shapes;

public class PauseMenu extends UIElement<MouseActionEvent> {

    private final MainGame origin;
    private Polygon2D backgroundScreen;
    private Text2D pausedText;
    private BetterButton resumeButton;

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

        pausedText = Text2D.create("Paused")
                .withFont(Fonts.TitleTextFont)
                .withFill(Colors.Snowy)
                .build();
        pausedText.setTranslation(Pointf.subtract(center, pausedText.width() / 2f, 100f));

        resumeButton = new BetterButton(origin, backgroundScreen.getCenter().add(-100f, 100f), Shapes.ButtonSize);
        resumeButton.setText("Resume Game");
        resumeButton.setFill(Color.darkGray);
        resumeButton.setFont(Fonts.ButtonTextFont);
        resumeButton.setOutlineColor(Colors.Snowy);
        resumeButton.setTextColor(Colors.Snowy);
        resumeButton.setOnAction(mouseButtonEvent -> {
            mouseButtonEvent.consume();
            FastJEngine.runAfterRender(() -> origin.changeState(GameState.Playing));
        });

        origin.drawableManager.addUIElement(this);
        origin.drawableManager.removeUIElement(resumeButton);

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
        } else {
            if (resumeButton != null) {
                origin.inputManager.removeMouseActionListener(resumeButton);
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
    }
}
