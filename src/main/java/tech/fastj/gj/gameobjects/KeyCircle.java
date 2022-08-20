package tech.fastj.gj.gameobjects;

import tech.fastj.engine.FastJEngine;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.util.DrawUtil;
import tech.fastj.graphics.util.PointsAndAlts;
import tech.fastj.input.keyboard.KeyboardActionListener;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.input.keyboard.events.KeyboardStateEvent;
import tech.fastj.math.Maths;
import tech.fastj.math.Pointf;
import tech.fastj.systems.behaviors.Behavior;
import tech.fastj.systems.behaviors.BehaviorHandler;
import tech.fastj.systems.control.GameHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

public class KeyCircle extends GameObject implements KeyboardActionListener, Behavior {

    /** {@link Color} representing the default color value of {@code (0, 0, 0)}. */
    public static final Color DefaultFill = Color.black;
    /** {@link Stroke} representing the default outline stroke value as a 1px outline with sharp edges. */
    public static final BasicStroke DefaultOutlineStroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f);
    /** {@link Color} representing the default outline color value as the color black. */
    public static final Color DefaultOutlineColor = Color.black;

    private static final Pointf OriginInstance = Pointf.origin();

    private final Keys key;
    private Color fillColor;
    private Color outlineColor;
    private Color baseColor;
    private float deltaTimeBuildup;
    private BasicStroke outlineStroke;
    private final Font font;

    public KeyCircle(Keys key, float radius, String fontName, BehaviorHandler handler) {
        PointsAndAlts circleMesh = DrawUtil.createCircle(0f, radius, radius);
        setCollisionPath(DrawUtil.createPath(circleMesh.points(), circleMesh.altIndexes()));

        this.key = key;
        this.font = new Font(fontName, Font.BOLD, 12);
        setFill(DefaultFill);

        addLateBehavior(this, handler);
    }

    /**
     * Sets the {@code KeyCircle}'s {@code Color}.
     *
     * @param newColor The new {@code Color} value.
     * @return The {@code KeyCircle} instance, for method chaining.
     */
    public KeyCircle setFill(Color newColor) {
        return setFill(newColor, true);
    }

    /**
     * Sets the {@code KeyCircle}'s {@code Color}.
     *
     * @param newColor The new {@code Color} value.
     * @return The {@code KeyCircle} instance, for method chaining.
     */
    public KeyCircle setFill(Color newColor, boolean changeBase) {
        fillColor = newColor;
        if (changeBase) {
            baseColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillColor.getAlpha());
        } else {
            deltaTimeBuildup = 0f;
        }
        return this;
    }

    /**
     * Sets the polygon's outline color.
     *
     * @param newOutlineColor The outline {@code Color} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public KeyCircle setOutlineColor(Color newOutlineColor) {
        outlineColor = newOutlineColor;
        return this;
    }

    /**
     * Sets the polygon's outline stroke.
     *
     * @param newOutlineStroke The outline {@code BasicStroke} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public KeyCircle setOutlineStroke(BasicStroke newOutlineStroke) {
        outlineStroke = newOutlineStroke;
        return this;
    }

    /**
     * Sets the polygon's outline stroke and color.
     *
     * @param newOutlineStroke The outline {@code BasicStroke} to be used for the polygon.
     * @param newOutlineColor  The outline {@code Color} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public KeyCircle setOutline(BasicStroke newOutlineStroke, Color newOutlineColor) {
        outlineStroke = newOutlineStroke;
        outlineColor = newOutlineColor;
        return this;
    }

    /**
     * Gets the note's fill paint.
     *
     * @return The {@code Paint} set for this polygon.
     */
    public Paint getFill() {
        return fillColor;
    }

    /**
     * Gets the note's outline color.
     *
     * @return The note's outline {@code Color}.
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Gets the note's outline stroke.
     *
     * @return The note's outline {@code BasicStroke}.
     */
    public BasicStroke getOutlineStroke() {
        return outlineStroke;
    }

    public Keys getKey() {
        return key;
    }

    @Override
    public void onKeyRecentlyPressed(KeyboardStateEvent keyboardStateEvent) {
        if (keyboardStateEvent.getKey() == key) {
            fillColor = Color.white;
        }
    }

    @Override
    public void init(GameObject gameObject) {
    }

    @Override
    public void fixedUpdate(GameObject gameObject) {
    }

    @Override
    public void update(GameObject gameObject) {
        if (!fillColor.equals(baseColor)) {
            deltaTimeBuildup = Maths.clamp(deltaTimeBuildup + (FastJEngine.getDeltaTime() / 10f), 0f, 1f);
            fillColor = DrawUtil.colorLerp(fillColor, baseColor, deltaTimeBuildup);
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();

        FontMetrics fm = g.getFontMetrics(font);
        int textWidth = fm.stringWidth(key.name());
        int textHeight = fm.getHeight();

        g.transform(getTransformation());

        g.setPaint(fillColor);
        g.fill(collisionPath);

        g.setStroke(outlineStroke);
        g.setPaint(outlineColor);
        g.draw(collisionPath);

        g.setFont(font);
        g.setPaint(Color.black);
        g.drawString(key.name(), OriginInstance.x - (textWidth / 2f), ((textHeight * 0.5f) + height()) / 2f);

        g.setTransform(oldTransform);
        g.setFont(oldFont);
        g.setColor(oldColor);
    }

    @Override
    public void destroy(GameHandler origin) {
        fillColor = DefaultFill;
        outlineColor = DefaultOutlineColor;
        outlineStroke = DefaultOutlineStroke;
        origin.inputManager().removeKeyboardActionListener(this);
        super.destroyTheRest(origin);
    }
}
