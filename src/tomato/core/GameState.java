package tomato.core;

public class GameState {
    public enum GameStateType {
        PAUSED,
        PLAY
    }

    public static GameStateType CURRENT_STATE = GameStateType.PLAY;
}
