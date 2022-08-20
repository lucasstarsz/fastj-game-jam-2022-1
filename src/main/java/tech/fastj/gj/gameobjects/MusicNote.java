package tech.fastj.gj.gameobjects;

import tech.fastj.graphics.game.GameObject;
import tech.fastj.graphics.util.DrawUtil;
import tech.fastj.graphics.util.PointsAndAlts;
import tech.fastj.math.Pointf;
import tech.fastj.systems.control.GameHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Objects;

public class MusicNote extends GameObject {

    /** {@link Paint} representing the default fill paint value as the color black. */
    public static final Paint DefaultFill = Color.black;
    /** {@link Stroke} representing the default outline stroke value as a 1px outline with sharp edges. */
    public static final BasicStroke DefaultOutlineStroke = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f);
    /** {@link Color} representing the default outline color value as the color black. */
    public static final Color DefaultOutlineColor = Color.black;

    public static final Pointf[] MusicNoteEmblem = new Pointf[]{
            new Pointf(3f, 0f),
            new Pointf(5f, 2f),
            new Pointf(5.1f, 2.5f),
            new Pointf(3.5f, 1f),
            new Pointf(3.5f, 5.5f),
            new Pointf(2.5f, 6.5f),
            new Pointf(1f, 6.5f),
            new Pointf(0f, 6f),
            new Pointf(-0.5f, 5f),
            new Pointf(0f, 4f),
            new Pointf(1f, 3.5f),
            new Pointf(2.5f, 3.5f),
            new Pointf(2.5f, 0f)
    };

    private Paint fillPaint;
    private Color outlineColor;
    private BasicStroke outlineStroke;
    private final Path2D.Float musicNoteEmblem;

    public MusicNote(Pointf location, float size) {
        PointsAndAlts pathOutline = DrawUtil.createCircle(location.x, location.y, size);
        setCollisionPath(DrawUtil.createPath(pathOutline.points(), pathOutline.altIndexes()));

        Pointf[] musicNoteEmblemMesh = new Pointf[MusicNoteEmblem.length];
        for (int i = 0; i < MusicNoteEmblem.length; i++) {
            musicNoteEmblemMesh[i] = MusicNoteEmblem[i].copy()
                    .add(0.5f)
                    .divide(5.6f, 6.5f)
                    .multiply(size)
                    .add(location)
                    .subtract(size / 2f);
        }
        this.musicNoteEmblem = DrawUtil.createPath(musicNoteEmblemMesh);
    }

    /**
     * Sets the polygon's fill paint.
     *
     * @param newPaint The fill {@code Paint} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public MusicNote setFill(Paint newPaint) {
        fillPaint = Objects.requireNonNull(newPaint);
        return this;
    }

    /**
     * Sets the polygon's outline color.
     *
     * @param newOutlineColor The outline {@code Color} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public MusicNote setOutlineColor(Color newOutlineColor) {
        outlineColor = newOutlineColor;
        return this;
    }

    /**
     * Sets the polygon's outline stroke.
     *
     * @param newOutlineStroke The outline {@code BasicStroke} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public MusicNote setOutlineStroke(BasicStroke newOutlineStroke) {
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
    public MusicNote setOutline(BasicStroke newOutlineStroke, Color newOutlineColor) {
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
        return fillPaint;
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
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();

        g.transform(getTransformation());

        g.setPaint(fillPaint);
        g.fill(collisionPath);

        g.setStroke(outlineStroke);
        g.setPaint(outlineColor);
        g.draw(collisionPath);

        g.setPaint(Color.black);
        g.fill(musicNoteEmblem);

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(GameHandler origin) {
        fillPaint = DefaultFill;
        outlineColor = DefaultOutlineColor;
        outlineStroke = DefaultOutlineStroke;
        super.destroyTheRest(origin);
    }
}
