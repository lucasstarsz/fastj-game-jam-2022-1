package tech.fastj.gj.gameobjects;

import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.collections.Pair;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SimpleManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

public class KeyCircle extends GameObject {

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
    private BasicStroke outlineStroke;
    private final Font font;

    public KeyCircle(Keys key, float radius, String fontName) {
        Pair<Pointf[], Point[]> circleMesh = DrawUtil.createCircle(0f, radius, radius);
        setCollisionPath(DrawUtil.createPath(circleMesh.getLeft(), circleMesh.getRight()));

        this.key = key;
        this.font = new Font(fontName, Font.BOLD, 12);
        setFill(DefaultFill);
    }

    /**
     * Sets the {@code KeyCircle}'s {@code Color}.
     *
     * @param newColor The new {@code Color} value.
     * @return The {@code KeyCircle} instance, for method chaining.
     */
    public KeyCircle setFill(Color newColor) {
        fillColor = newColor;
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
    public void destroy(Scene origin) {
        fillColor = DefaultFill;
        outlineColor = DefaultOutlineColor;
        outlineStroke = DefaultOutlineStroke;
        super.destroyTheRest(origin);
    }

    @Override
    public void destroy(SimpleManager origin) {
        fillColor = DefaultFill;
        outlineColor = DefaultOutlineColor;
        outlineStroke = DefaultOutlineStroke;
        super.destroyTheRest(origin);
    }
}
