package tech.fastj.gj.scripts;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.scenes.game.GameState;
import tech.fastj.gj.scenes.game.MainGame;
import tech.fastj.gj.util.ExtraMaths;
import tech.fastj.graphics.Boundary;
import tech.fastj.graphics.game.GameObject;
import tech.fastj.math.Maths;
import tech.fastj.math.Pointf;
import tech.fastj.systems.behaviors.Behavior;
import tech.fastj.systems.control.SceneManager;

public class MusicNoteMovement implements Behavior {

    private final MainGame game;
    private final Conductor conductor;
    private final double noteBeat;
    private final double travelDistance;
    private final double spawnBeat;
    private final Pointf lerpDistance;

    public MusicNoteMovement(MainGame game, Conductor conductor, double noteBeat, double travelDistance) {
        this.game = game;
        this.conductor = conductor;
        this.noteBeat = noteBeat;
        this.spawnBeat = noteBeat - conductor.musicInfo.getBeatPeekCount();
        this.travelDistance = travelDistance;
        this.lerpDistance = new Pointf();
    }

    public MusicNoteMovement(Conductor conductor, double noteBeat, double travelDistance) {
        this(null, conductor, noteBeat, travelDistance);
    }

    @Override
    public void init(GameObject gameObject) {
    }

    @Override
    public void fixedUpdate(GameObject gameObject) {
    }

    @Override
    public void update(GameObject gameObject) {
        if (game != null && game.getGameState() == GameState.Paused) {
            return;
        }

        lerpDistance.y = Maths.lerp(
            0f,
            (float) travelDistance,
            ExtraMaths.normalize((float) conductor.songPositionInBeats, (float) spawnBeat, (float) noteBeat)
        );
        gameObject.setTranslation(lerpDistance);

        if (gameObject.getBound(Boundary.TopLeft).y > travelDistance + gameObject.height()) {
            FastJEngine.runLater(() -> gameObject.destroy(FastJEngine.<SceneManager>getLogicManager()
                .getCurrentScene()), CoreLoopState.Update);
        }
    }

    @Override
    public void destroy() {
        lerpDistance.reset();
    }
}
