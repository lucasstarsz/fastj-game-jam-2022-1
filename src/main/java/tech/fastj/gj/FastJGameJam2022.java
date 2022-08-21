package tech.fastj.gj;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.logging.LogLevel;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;

public class FastJGameJam2022 {

    public static final String GameName = "Rhythm Game";

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        FlatDarkLaf.setup();
        FastJEngine.init(GameName, new GameManager());
        FastJEngine.setTargetUPS(1);
        FastJEngine.getGameLoop().getGameLoopStates().get(CoreLoopState.FixedUpdate).clear();
        FastJEngine.getGameLoop().getGameLoopStates().get(CoreLoopState.FixedUpdate).add(FastJEngine.GeneralFixedUpdate);
        FastJEngine.configureLogging(LogLevel.Debug);

        try {
            FastJEngine.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.exit(0);
    }
}
