package tomato.ui;

import tomato.core.GameState;

public class MainMenu extends Menu
{
    public MainMenu()
    {
        addLabel("Untitled Tank Game", 15);
        addButton("Resume", () -> {
            GameState.CURRENT_STATE = GameState.GameStateType.PLAY;
        });
    }
}
