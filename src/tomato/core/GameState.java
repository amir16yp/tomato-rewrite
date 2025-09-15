package tomato.core;

public class GameState {
    public enum GameStateType {
        PAUSED,
        PLAY
    }

    public static GameStateType CURRENT_STATE = GameStateType.PAUSED;

    public static void Pause()
    {
        CURRENT_STATE = GameStateType.PAUSED;
    }

    public static void Play()
    {
        CURRENT_STATE = GameStateType.PLAY;
    }

    public static boolean isPaused()
    {
        return CURRENT_STATE == GameStateType.PAUSED;
    }

    public static boolean isPlaying()
    {
        return CURRENT_STATE == GameStateType.PAUSED;
    }
}
