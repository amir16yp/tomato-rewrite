package tomato.ui;
import tomato.Game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Menu extends UIElement implements MouseMotionListener, MouseListener, MouseWheelListener {
    // Color scheme for military/tactical theme
    private static final Color MILITARY_GREEN = new Color(20, 30, 40, 220);   // Dark blue-gray with transparency
    private static final Color BRASS_HIGHLIGHT = new Color(210, 60, 50);      // Red accent color (replacing brass)
    private static final Color STEEL_GRAY = new Color(40, 50, 60);           // Darker blue-gray for inactive elements
    private static final Color TACTICAL_RED = new Color(255, 70, 70);        // Bright red for emphasis
    private static final Color MENU_BACKGROUND = MILITARY_GREEN;             // Main menu background

    private final List<UIElement> elements; // List to store all UI elements
    private final int buttonWidth = (int)(tomato.Game.WIDTH * 0.7); // 70% of screen width
    private final int buttonHeight = 20;
    private final int spacing = 25;
    private final ArrayList<Menu> childMenus = new ArrayList<>();
    private final String label;
    
    // Pagination variables
    private static final int ELEMENTS_PER_PAGE = 8; // Number of elements visible per page
    private int currentPage = 0;
    private int totalPages = 0;
    private Button prevPageButton;
    private Button nextPageButton;
    private Label pageIndicator;
    
    public int selectedButtonIndex = 0;
    
    // Keyboard navigation flags
    private boolean isKeyboardNavigationEnabled = true;
    
    // Keyboard navigation timing using delta time accumulation
    private float keyPressTimer = 0.0f;
    private static final float KEY_REPEAT_DELAY = 0.15f; // 150ms in seconds
    private static final float SLIDER_REPEAT_DELAY = 0.05f; // 50ms in seconds

    public Menu(String label) {
        super(0, 0, tomato.Game.WIDTH, tomato.Game.HEIGHT, false);
        setBackgroundColor(MENU_BACKGROUND);
        this.label = label;
        this.elements = new ArrayList<>();
        
        // Create navigation buttons
        createNavigationButtons();
    }
    
    private void createNavigationButtons() {
        // Previous page button (bottom left)
        prevPageButton = new Button(20, tomato.Game.HEIGHT - 60, 80, 25, "← Previous");
        prevPageButton.setOnSelectedAction(this::previousPage);
        prevPageButton.setBackgroundColor(STEEL_GRAY);
        prevPageButton.setTextColor(BRASS_HIGHLIGHT);
        prevPageButton.setHighlightColor(TACTICAL_RED);
        
        // Next page button (bottom right)
        nextPageButton = new Button(tomato.Game.WIDTH - 100, tomato.Game.HEIGHT - 60, 80, 25, "Next →");
        nextPageButton.setOnSelectedAction(this::nextPage);
        nextPageButton.setBackgroundColor(STEEL_GRAY);
        nextPageButton.setTextColor(BRASS_HIGHLIGHT);
        nextPageButton.setHighlightColor(TACTICAL_RED);
        
        // Page indicator (center bottom)
        pageIndicator = new Label(tomato.Game.WIDTH / 2 - 50, tomato.Game.HEIGHT - 60, 100, 25, "");
        pageIndicator.setTextColor(BRASS_HIGHLIGHT);
        pageIndicator.setCentered(true);
    }
    
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePageDisplay();
            updateElementPositions();
        }
    }
    
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePageDisplay();
            updateElementPositions();
        }
    }
    
    private void updatePageDisplay() {
        // Update page indicator text
        if (totalPages > 1) {
            pageIndicator.setText("Page " + (currentPage + 1) + " of " + totalPages);
        } else {
            pageIndicator.setText("");
        }
        
        // Update navigation button states
        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < totalPages - 1);
    }
    
    private void updateElementPositions() {
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        for (int i = 0; i < elements.size(); i++) {
            UIElement element = elements.get(i);
            if (i >= startIndex && i < endIndex) {
                // Element is on current page
                int pageIndex = i - startIndex;
                int y = startY() + pageIndex * spacing;
                element.setY(y);
                element.setVisible(true);
            } else {
                // Element is not on current page
                element.setVisible(false);
            }
        }
    }
    
    private void navigateUp() {
        if (!isVisible() || !isKeyboardNavigationEnabled || elements.isEmpty()) return;
        
        // Find the previous selectable element on the current page
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        int previousIndex = selectedButtonIndex;
        do {
            selectedButtonIndex = (selectedButtonIndex - 1 + elements.size()) % elements.size();
            // Break if we've checked all elements and none are selectable
            if (selectedButtonIndex == previousIndex) break;
        } while (!(elements.get(selectedButtonIndex) instanceof Button || 
                 elements.get(selectedButtonIndex) instanceof Checkbox ||
                 elements.get(selectedButtonIndex) instanceof RangeSlider) ||
                 selectedButtonIndex < startIndex || selectedButtonIndex >= endIndex);
        
        updateSelection();
    }
    
    private void navigateDown() {
        if (!isVisible() || !isKeyboardNavigationEnabled || elements.isEmpty()) return;
        
        // Find the next selectable element on the current page
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        int previousIndex = selectedButtonIndex;
        do {
            selectedButtonIndex = (selectedButtonIndex + 1) % elements.size();
            // Break if we've checked all elements and none are selectable
            if (selectedButtonIndex == previousIndex) break;
        } while (!(elements.get(selectedButtonIndex) instanceof Button || 
                 elements.get(selectedButtonIndex) instanceof Checkbox ||
                 elements.get(selectedButtonIndex) instanceof RangeSlider) ||
                 selectedButtonIndex < startIndex || selectedButtonIndex >= endIndex);
        
        updateSelection();
    }
    
    private void updateSelection() {
        // Update visual selection state of all menu elements
        for (int i = 0; i < elements.size(); i++) {
            UIElement element = elements.get(i);
            element.setSelected(i == selectedButtonIndex);
        }
    }
    
    public void setKeyboardNavigationEnabled(boolean enabled) {
        isKeyboardNavigationEnabled = enabled;
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<Menu> getChildMenus() {
        return childMenus;
    }

    // Update the addLabel method to use consistent text color
    public Label addLabel(String text) {
        int x = startX();
        int y = startY() + elements.size() * spacing;
        Label label = new Label(x, y, buttonWidth, buttonHeight, text);
        label.setTextColor(BRASS_HIGHLIGHT);  // Brass colored text for labels
        label.setCentered(true);
        addElement(label);
        return label;
    }

    public LinkLabel addLinkLabel(String text, String url) {
        int x = startX();
        int y = startY() + elements.size() * spacing;
        LinkLabel label = new LinkLabel(x, y, buttonWidth, buttonHeight, text, url);
        label.setTextColor(BRASS_HIGHLIGHT);  // Brass colored text for labels
        label.setCentered(true);
        addElement(label);
        return label;
    }

    // Update the second addLabel method as well
    public Label addLabel(String text, boolean centered) {
        int x = startX();
        int y = startY() + elements.size() * spacing;
        Label label = new Label(x, y, buttonWidth, buttonHeight, text);
        label.setTextColor(BRASS_HIGHLIGHT);  // Brass colored text for labels
        label.setCentered(centered);
        addElement(label);
        return label;
    }

    public void addChildMenu(Menu menu) {
        this.childMenus.add(menu);
//        menu.addButton("Back", () -> {
//            Game.RENDERER.setCurrentMenu(this);
//        });
//        this.addButton(menu.label, () -> {
//            Game.RENDERER.setCurrentMenu(menu);
//        });
    }

    public Menu findChildMenu(String childMenuName) {
        return getChildMenus().stream()
                .filter(menu -> menu.getLabel().equals(childMenuName))
                .findFirst()
                .orElse(null);
    }

    public Menu findMenu(String menuName) {
        if (getLabel().equals(menuName)) {
            return this;
        }
        
        for (Menu childMenu : getChildMenus()) {
            Menu foundMenu = childMenu.findMenu(menuName);
            if (foundMenu != null) {
                return foundMenu;
            }
        }
        
        return null;
    }

    public Button findButton(String buttonName) {
        return (Button) getElements().stream()
                .filter(element -> element instanceof Button)
                .filter(element -> ((Button) element).getText().equals(buttonName))
                .findFirst()
                .orElse(null);
    }

    public Checkbox findCheckbox(String checkboxLabel) {
        return (Checkbox) getElements().stream()
                .filter(element -> element instanceof Checkbox)
                .filter(element -> ((Checkbox) element).getLabel().equals(checkboxLabel))
                .findFirst()
                .orElse(null);
    }

    public Label findLabel(String labelText) {
        return (Label) getElements().stream()
                .filter(element -> element instanceof Label)
                .filter(element -> ((Label) element).getText().equals(labelText))
                .findFirst()
                .orElse(null);
    }

    public Button addButton(String text, Runnable action) {
        int x = startX();
        int y = startY() + elements.size() * spacing;
        Button button = new Button(x, y, buttonWidth, buttonHeight, text);
        button.setOnSelectedAction(action);

            // Standard styling
            button.setBackgroundColor(STEEL_GRAY);
            button.setTextColor(BRASS_HIGHLIGHT);
            button.setHighlightColor(TACTICAL_RED);
        
        addElement(button);
        return button;
    }

    public Checkbox addCheckbox(String label, Runnable onTrue, Runnable onFalse) {
        int x = startX();
        int y = startY() + elements.size() * spacing;
        Checkbox checkbox = new Checkbox(x, y, buttonHeight, buttonHeight, true, label, onTrue, onFalse);
        addElement(checkbox);
        return checkbox;
    }

    public RangeSlider addSlider(double minValue, double maxValue, double initialValue, int decimalPlaces) {
        int x = startX();
        int y = startY() + elements.size() * spacing;
        RangeSlider slider = new RangeSlider(x, y, buttonWidth, buttonHeight, minValue, maxValue, initialValue, decimalPlaces);
        addElement(slider);
        return slider;
    }

    // Add a vertical spacer (empty label with custom height)
    public void addVerticalSpacer(int height) {
        Label spacer = addLabel("");
        spacer.setPreferredHeight(height);
    }

    private int startX() {
        return (tomato.Game.WIDTH - buttonWidth) / 2;
    }

    private int startY() {
        return getY() + 35; // Add 35 pixels of padding at the top
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        for (UIElement element : elements) {
            element.setVisible(visible);
        }
        
        // When menu becomes visible, select the first selectable element
        if (visible) {
            selectFirstSelectableElement();
            updatePageDisplay();
            updateElementPositions();
        }
    }
    
    private void selectFirstSelectableElement() {
        // Find the first selectable element on the current page
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            UIElement element = elements.get(i);
            if (element instanceof Button || 
                element instanceof Checkbox || 
                element instanceof RangeSlider) {
                selectedButtonIndex = i;
                updateSelection();
                return;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (isVisible()) {
            // Enable anti-aliasing for smoother text rendering
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Use a more modern, tactical font for the menu
            Font originalFont = g2d.getFont();
            Font tacticalFont = new Font("SansSerif", Font.BOLD, originalFont.getSize());
            g2d.setFont(tacticalFont);
            
            // Draw all visible UI elements on current page
            int startIndex = currentPage * ELEMENTS_PER_PAGE;
            int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
            
            for (int i = startIndex; i < endIndex; i++) {
                UIElement element = elements.get(i);
                if (element.isVisible()) {
                    element.draw(g2d);
                }
            }

            // Draw navigation elements if there are multiple pages
            if (totalPages > 1) {
                prevPageButton.draw(g2d);
                nextPageButton.draw(g2d);
                pageIndicator.draw(g2d);
            }
            
            // Restore original font
            g2d.setFont(originalFont);
        }
    }

    @Override
    public void update() {
        super.update();
        
        // Update visible elements on current page
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            UIElement element = elements.get(i);
            if (element.isVisible()) {
                element.update();
            }
        }
        
        // Update navigation buttons
        if (totalPages > 1) {
            prevPageButton.update();
            nextPageButton.update();
        }
        
        // Handle keyboard input using isKeyPressed
        if (isVisible() && isKeyboardNavigationEnabled) {
            keyPressTimer += tomato.Game.GAME_LOOP.getDeltaTime(); // Accumulate delta time
            
            // Handle menu navigation with arrow keys
            if (tomato.Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_UP)) {
                if (keyPressTimer >= KEY_REPEAT_DELAY) {
                    navigateUp();
                    keyPressTimer = 0.0f; // Reset timer after action
                }
            }
            if (tomato.Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_DOWN)) {
                if (keyPressTimer >= KEY_REPEAT_DELAY) {
                    navigateDown();
                    keyPressTimer = 0.0f; // Reset timer after action
                }
            }
            
            // Handle enter key for activation
            if (tomato.Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_ENTER)) {
                if (keyPressTimer >= KEY_REPEAT_DELAY) {
                    activateSelected();
                    keyPressTimer = 0.0f; // Reset timer after action
                }
            }
            
            // Handle left/right for sliders and page navigation
            if (tomato.Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_LEFT)) {
                if (keyPressTimer >= KEY_REPEAT_DELAY) {
                    if (!elements.isEmpty() && selectedButtonIndex >= 0 && selectedButtonIndex < elements.size()) {
                        UIElement selected = elements.get(selectedButtonIndex);
                        if (selected instanceof RangeSlider) {
                            ((RangeSlider) selected).decreaseValue();
                        }
                    } else if (currentPage > 0) {
                        previousPage();
                    }
                    keyPressTimer = 0.0f; // Reset timer after action
                }
            }
            if (tomato.Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_RIGHT)) {
                if (keyPressTimer >= KEY_REPEAT_DELAY) {
                    if (!elements.isEmpty() && selectedButtonIndex >= 0 && selectedButtonIndex < elements.size()) {
                        UIElement selected = elements.get(selectedButtonIndex);
                        if (selected instanceof RangeSlider) {
                            ((RangeSlider) selected).increaseValue();
                        }
                    } else if (currentPage < totalPages - 1) {
                        nextPage();
                    }
                    keyPressTimer = 0.0f; // Reset timer after action
                }
            }

//            if (Game.KEY_REGISTRY.isKeyPressed(KeyEvent.VK_X) && Game.RENDERER.getCurrentMenu().equals(Game.GAME.getMainMenu()))
//            {
//                MainMenu.regenerateRandomBackground();
//            }


        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // No-op: Only handle activation on mouseReleased for less sensitivity
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // No-op: Only handle activation on mouseReleased for less sensitivity
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        // Check navigation buttons first
        if (totalPages > 1) {
            if (prevPageButton.containsPoint(x, y)) {
                prevPageButton.onOptionSelected();
                return;
            }
            if (nextPageButton.containsPoint(x, y)) {
                nextPageButton.onOptionSelected();
                return;
            }
        }
        
        // Handle UI element clicks on current page
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            UIElement element = elements.get(i);
            if (element.isVisible() && element.containsPoint(x, y)) {
                if (element instanceof RangeSlider) {
                    ((RangeSlider) element).handleMouseEvent(e);
                } else {
                    activate(element);
                }
                break;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Handle dragging for RangeSliders on current page
        int x = e.getX();
        int y = e.getY();
        
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            UIElement element = elements.get(i);
            if (element.isVisible() && element.containsPoint(x, y)) {
                if (element instanceof RangeSlider) {
                    ((RangeSlider) element).handleMouseEvent(e);
                }
                break;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        // Update navigation button hover states
        if (totalPages > 1) {
            prevPageButton.setHovered(prevPageButton.containsPoint(x, y));
            nextPageButton.setHovered(nextPageButton.containsPoint(x, y));
        }
        
        // Update UI element hover states on current page
        int startIndex = currentPage * ELEMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ELEMENTS_PER_PAGE, elements.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            UIElement element = elements.get(i);
            boolean containsPoint = element.containsPoint(x, y);

            if (element instanceof Button) {
                Button button = (Button) element;
                button.setSelected(containsPoint);
                button.setHovered(containsPoint);
                if (containsPoint) selectedButtonIndex = i;
            } else if (element instanceof Checkbox) {
                Checkbox checkbox = (Checkbox) element;
                checkbox.setSelected(containsPoint);
                checkbox.setHovered(containsPoint);
                if (containsPoint) selectedButtonIndex = i;
            } else if (element instanceof RangeSlider) {
                // Let the RangeSlider handle its own hover states
                RangeSlider slider = (RangeSlider) element;
                slider.handleMouseMove(x, y);
            } else if (element instanceof Label) {
                // Handle Label hover states (includes LinkLabel)
                Label label = (Label) element;
                label.updateHoverState(x, y);
                if (containsPoint) selectedButtonIndex = i;
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Use mouse wheel for page navigation
        int notches = e.getWheelRotation();
        if (notches > 0 && currentPage < totalPages - 1) {
            nextPage();
        } else if (notches < 0 && currentPage > 0) {
            previousPage();
        }
    }

    public void addElement(UIElement element) {
        elements.add(element);
        updateTotalPages();
        updateElementPositions();
    }
    
    private void updateTotalPages() {
        totalPages = (int) Math.ceil((double) elements.size() / ELEMENTS_PER_PAGE);
        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1;
        }
        updatePageDisplay();
    }

    public List<UIElement> getElements() {
        return this.elements;
    }

    public void activate(UIElement element) {
        if (element instanceof Button) {
            ((Button) element).onOptionSelected();
        } else if (element instanceof Checkbox) {
            ((Checkbox) element).toggleChecked();
        } else if (element instanceof Label) {
            ((Label) element).onClick();
        }
        // RangeSliders are not activatable - only their track responds to clicks
    }

    private void activateSelected() {
        if (!elements.isEmpty() && selectedButtonIndex >= 0 && selectedButtonIndex < elements.size()) {
            UIElement element = elements.get(selectedButtonIndex);
            activate(element);
        }
    }

    /**
     * Gets the current page number (0-based)
     * @return The current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the total number of pages
     * @return The total number of pages
     */
    public int getTotalPages() {
        return totalPages;
    }
}