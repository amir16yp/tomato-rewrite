package tomato.ui;

import tomato.core.GameState;

public class MainMenu extends Menu
{
    public MainMenu()
    {
        super("Main Menu");
        addButton("Start", () -> { GameState.CURRENT_STATE = GameState.GameStateType.PLAY; });
    }
}
