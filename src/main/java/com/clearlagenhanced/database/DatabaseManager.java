package com.clearlagenhanced.database;

import com.clearlagenhanced.ClearLaggEnhanced;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    
    private final ClearLaggEnhanced plugin;
    private Connection connection;
    private final String databasePath;
    
    public DatabaseManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder() + File.separator + 
            plugin.getConfigManager().getString("database.file", "data.db");
        
        if (plugin.getConfigManager().getBoolean("database.enabled", true)) {
            initialize();
        }
    }
    
    private void initialize() {
        try {
            createConnection();
            createTables();
            plugin.getLogger().info("Database initialized successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }
    
    private void createConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        
        String url = "jdbc:sqlite:" + databasePath;
        connection = DriverManager.getConnection(url);
    }
    
    private void createTables() throws SQLException {
        String clearingHistoryTable = """
            CREATE TABLE IF NOT EXISTS clearing_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL,
                entities_cleared INTEGER NOT NULL,
                worlds_affected TEXT NOT NULL,
                clear_type TEXT NOT NULL,
                duration_ms INTEGER NOT NULL
            )
        """;
        
        String performanceDataTable = """
            CREATE TABLE IF NOT EXISTS performance_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL,
                tps REAL NOT NULL,
                ram_used INTEGER NOT NULL,
                ram_max INTEGER NOT NULL,
                entity_count INTEGER NOT NULL
            )
        """;
        
        String laggyChunksTable = """
            CREATE TABLE IF NOT EXISTS laggy_chunks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                world TEXT NOT NULL,
                chunk_x INTEGER NOT NULL,
                chunk_z INTEGER NOT NULL,
                entity_count INTEGER NOT NULL,
                last_scanned TEXT NOT NULL
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(clearingHistoryTable);
            stmt.execute(performanceDataTable);
            stmt.execute(laggyChunksTable);
        }
    }
    
    public CompletableFuture<Void> logClearingHistory(int entitiesCleared, List<String> worlds, 
                                                     String clearType, long durationMs) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO clearing_history (timestamp, entities_cleared, worlds_affected, clear_type, duration_ms)
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try {
                createConnection();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, LocalDateTime.now().toString());
                    stmt.setInt(2, entitiesCleared);
                    stmt.setString(3, String.join(",", worlds));
                    stmt.setString(4, clearType);
                    stmt.setLong(5, durationMs);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log clearing history: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<Void> logPerformanceData(double tps, long ramUsed, long ramMax, int entityCount) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO performance_data (timestamp, tps, ram_used, ram_max, entity_count)
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try {
                createConnection();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, LocalDateTime.now().toString());
                    stmt.setDouble(2, tps);
                    stmt.setLong(3, ramUsed);
                    stmt.setLong(4, ramMax);
                    stmt.setInt(5, entityCount);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log performance data: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<List<LaggyChunkData>> getLaggyChunks(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<LaggyChunkData> chunks = new ArrayList<>();
            String sql = "SELECT world, chunk_x, chunk_z, entity_count FROM laggy_chunks ORDER BY entity_count DESC LIMIT ?";
            
            try {
                createConnection();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, limit);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            chunks.add(new LaggyChunkData(
                                rs.getString("world"),
                                rs.getInt("chunk_x"),
                                rs.getInt("chunk_z"),
                                rs.getInt("entity_count")
                            ));
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get laggy chunks: " + e.getMessage());
            }
            
            return chunks;
        });
    }
    
    public CompletableFuture<Void> updateLaggyChunks(List<LaggyChunkData> chunks) {
        return CompletableFuture.runAsync(() -> {
            try {
                createConnection();
                connection.setAutoCommit(false);
                
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("DELETE FROM laggy_chunks");
                }
                
                String sql = """
                    INSERT INTO laggy_chunks (world, chunk_x, chunk_z, entity_count, last_scanned)
                    VALUES (?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    for (LaggyChunkData chunk : chunks) {
                        stmt.setString(1, chunk.world());
                        stmt.setInt(2, chunk.chunkX());
                        stmt.setInt(3, chunk.chunkZ());
                        stmt.setInt(4, chunk.entityCount());
                        stmt.setString(5, LocalDateTime.now().toString());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                
                connection.commit();
                connection.setAutoCommit(true);
                
            } catch (SQLException e) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException rollbackEx) {
                    plugin.getLogger().warning("Failed to rollback transaction: " + rollbackEx.getMessage());
                }
                plugin.getLogger().warning("Failed to update laggy chunks: " + e.getMessage());
            }
        });
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing database connection: " + e.getMessage());
        }
    }
    
    public record LaggyChunkData(String world, int chunkX, int chunkZ, int entityCount) {}
}