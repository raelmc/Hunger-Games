// Updated GameManager.java

public class GameManager {
    // New member variables for border logic, session duration, and player stats
    private Border border;
    private long sessionStartTime;
    private Map<Player, PlayerStats> playerStats;

    public GameManager() {
        playerStats = new HashMap<>();
        sessionStartTime = System.currentTimeMillis();
        // Initialize border logic
        border = new Border();
    }

    // New method to track kills and deaths
    public void recordPlayerKill(Player killer, Player victim) {
        playerStats.get(killer).incrementKills();
        playerStats.get(victim).incrementDeaths();
    }

    // Other existing methods

    // Method to check session duration
    public void checkSessionDuration() {
        long duration = System.currentTimeMillis() - sessionStartTime;
        // Logic to handle the session duration check
    }
}

class PlayerStats {
    private int kills;
    private int deaths;

    public void incrementKills() {
        kills++;
    }

    public void incrementDeaths() {
        deaths++;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }
}