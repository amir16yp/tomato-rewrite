package tomato.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.logging.Level;


public class LinkLabel extends Label {
    private static final Logger logger = Logger.getLogger(LinkLabel.class.getName());
    private String url;
    
    public LinkLabel(int x, int y, int width, int height, String text, String url) {
        super(x, y, width, height, text);
        this.url = url;
        setHoverColor(Color.BLUE);
        setClickAction(() -> openLink(this.url));
    }
    
    public LinkLabel(int x, int y, String text, String url) {
        super(x, y, text);
        this.url = url;
        setHoverColor(Color.BLUE);
        setClickAction(() -> openLink(this.url));
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
        setClickAction(() -> openLink(this.url));
    }

    /**
     * Opens a URL in the default system browser
     * @param url The URL to open
     */
    public static void openLink(String url) {
        if (url == null || url.trim().isEmpty()) {
            logger.warning("Cannot open empty or null URL");
            return;
        }
        
        try {
            // Check if Desktop is supported on this platform
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                
                // Check if browse action is supported
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    // Ensure URL has a protocol
                    String processedUrl = url;
                    if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                        processedUrl = "https://" + url;
                    }
                    
                    URI uri = new URI(processedUrl);
                    desktop.browse(uri);
                    logger.info("Opened URL: " + processedUrl);
                } else {
                    logger.warning("Desktop browse action not supported on this platform");
                    fallbackOpenUrl(url);
                }
            } else {
                logger.warning("Desktop not supported on this platform");
                fallbackOpenUrl(url);
            }
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "Invalid URL syntax: " + url, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to open URL: " + url, e);
            fallbackOpenUrl(url);
        }
    }
    
    /**
     * Fallback method to open URL using system commands
     * @param url The URL to open
     */
    private static void fallbackOpenUrl(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();
            
            if (os.contains("win")) {
                // Windows
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                runtime.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Linux/Unix
                runtime.exec("xdg-open " + url);
            } else {
                logger.warning("Unsupported operating system for opening URLs: " + os);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fallback URL opening failed for: " + url, e);
        }
    }
}
