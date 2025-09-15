package tomato.core;

import tomato.entity.Entity;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Spatial partitioning system for efficient collision detection.
 * Divides the world into a grid to avoid O(n²) collision checks.
 */
public class SpatialGrid {
    private final int cellSize;
    private final Map<Point, Set<Entity>> grid;
    
    public SpatialGrid(int cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
    }
    
    /**
     * Clear all entities from the grid
     */
    public void clear() {
        grid.clear();
    }
    
    /**
     * Add an entity to the spatial grid
     */
    public void addEntity(Entity entity) {
        Rectangle hitbox = entity.getHitbox();
        Set<Point> cells = getCellsForRectangle(hitbox);
        
        for (Point cell : cells) {
            grid.computeIfAbsent(cell, _key -> new HashSet<>()).add(entity);
        }
    }
    
    /**
     * Remove an entity from the spatial grid
     */
    public void removeEntity(Entity entity) {
        Rectangle hitbox = entity.getHitbox();
        Set<Point> cells = getCellsForRectangle(hitbox);
        
        for (Point cell : cells) {
            Set<Entity> cellEntities = grid.get(cell);
            if (cellEntities != null) {
                cellEntities.remove(entity);
                if (cellEntities.isEmpty()) {
                    grid.remove(cell);
                }
            }
        }
    }
    
    /**
     * Get all entities that could potentially collide with the given entity
     */
    public Set<Entity> getPotentialCollisions(Entity entity) {
        Rectangle hitbox = entity.getHitbox();
        Set<Point> cells = getCellsForRectangle(hitbox);
        Set<Entity> potentialCollisions = new HashSet<>();
        
        for (Point cell : cells) {
            Set<Entity> cellEntities = grid.get(cell);
            if (cellEntities != null) {
                for (Entity other : cellEntities) {
                    if (other != entity) {
                        potentialCollisions.add(other);
                    }
                }
            }
        }
        
        return potentialCollisions;
    }
    
    /**
     * Get the first entity that collides with the given entity
     */
    public Entity getFirstCollision(Entity entity) {
        Rectangle hitbox = entity.getHitbox();
        Set<Point> cells = getCellsForRectangle(hitbox);
        
        for (Point cell : cells) {
            Set<Entity> cellEntities = grid.get(cell);
            if (cellEntities != null) {
                for (Entity other : cellEntities) {
                    if (other != entity && hitbox.intersects(other.getHitbox())) {
                        return other;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all entities that actually collide with the given entity
     */
    public List<Entity> getActualCollisions(Entity entity) {
        Rectangle hitbox = entity.getHitbox();
        Set<Entity> potentialCollisions = getPotentialCollisions(entity);
        List<Entity> actualCollisions = new ArrayList<>();
        
        for (Entity other : potentialCollisions) {
            if (hitbox.intersects(other.getHitbox())) {
                actualCollisions.add(other);
            }
        }
        
        return actualCollisions;
    }
    
    /**
     * Check if entity collides with any non-projectile entities
     */
    public boolean hasCollisionWithNonProjectiles(Entity entity) {
        Rectangle hitbox = entity.getHitbox();
        Set<Entity> potentialCollisions = getPotentialCollisions(entity);
        
        for (Entity other : potentialCollisions) {
            if (!(other instanceof tomato.entity.Projectile) && hitbox.intersects(other.getHitbox())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all grid cells that a rectangle overlaps
     */
    private Set<Point> getCellsForRectangle(Rectangle rect) {
        Set<Point> cells = new HashSet<>();
        
        int minX = rect.x / cellSize;
        int minY = rect.y / cellSize;
        int maxX = (rect.x + rect.width - 1) / cellSize;
        int maxY = (rect.y + rect.height - 1) / cellSize;
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                cells.add(new Point(x, y));
            }
        }
        
        return cells;
    }
    
    /**
     * Get statistics about the spatial grid for debugging
     */
    public String getStats() {
        int totalCells = grid.size();
        int totalEntities = 0;
        int maxEntitiesPerCell = 0;
        
        for (Set<Entity> cellEntities : grid.values()) {
            int count = cellEntities.size();
            totalEntities += count;
            maxEntitiesPerCell = Math.max(maxEntitiesPerCell, count);
        }
        
        double avgEntitiesPerCell = totalCells > 0 ? (double) totalEntities / totalCells : 0;
        
        return String.format("SpatialGrid Stats: %d cells, %d entities, %.1f avg/cell, %d max/cell", 
                           totalCells, totalEntities, avgEntitiesPerCell, maxEntitiesPerCell);
    }
}
