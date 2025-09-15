package tomato.ui;

import tomato.Game;
import tomato.core.GameState;
import tomato.core.Renderer;
import tomato.core.World;
import tomato.core.WorldType;

public class MainMenu extends Menu
{

    private Button mainButton;

    private Menu createPlayWorldMenu()
    {
        Menu worldTypeMenu = new Menu();
        worldTypeMenu.addLabel("Pick a world type", 15);
        for (WorldType worldType : WorldType.values())
        {
            worldTypeMenu.addButton("Start (" + worldType.name() + ")", () -> {
                World.createWorld(worldType);
                mainButton.text = "Resume";
                mainButton.onClick = () -> {
                    GameState.CURRENT_STATE = GameState.GameStateType.PLAY;
                };
                GameState.Play();
            });
        }
        worldTypeMenu.addButton("Back", () -> {
            Game.RENDERER.setCurrentMenu(Renderer.MAIN_MENU);
        });
        return worldTypeMenu;
    }

    public MainMenu()
    {
        addLabel("Untitled Tank Game", 15);
        Menu playWorldMenu = createPlayWorldMenu();
        mainButton = addButton("Play", () -> {
            Game.RENDERER.setCurrentMenu(playWorldMenu);

        });

        addButton("Exit", () -> {
            System.exit(0);
        });
    }
}
