package tech.fastj.gj;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.LogLevel;

public class FastJGameJam2022 {

    public static final String GameName = "FastJ Game Jam 2022.1";

    public static void main(String[] args) {
        FastJEngine.init(GameName, new GameManager());
        FastJEngine.setTargetUPS(1);
        FastJEngine.configureLogging(LogLevel.Trace);

        try {
            FastJEngine.run();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.exit(0);
    }
}
