package tk.achmed.sqlecon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import tk.achmed.sqlecon.mysql.MySQL;

public class SQLEconomy extends JavaPlugin {

    private Plugin plugin;

    // Making variables for SQL connection static, escaping errors
    public static String host;
    public static String port;
    public static String database;
    public static String tablePrefix;
    public static String table;
    public static String user;
    public static String pass;
    public static String defMoney;

    private static MySQL MySQL = new MySQL(host, port, database, user, pass);

    public void onDisable() {
    }

    public void onEnable() {
        host = getConfig().getString("DatabaseHostIP");
        port = getConfig().getString("DatabasePort");
        database = getConfig().getString("DatabaseName");
        tablePrefix = getConfig().getString("DatabaseTablePrefix");
        table = getConfig().getString("DatabaseTable");
        user = getConfig().getString("DatabaseUsername");
        pass = getConfig().getString("DatabasePassword");
        defMoney = getConfig().getString("DefaultMoney");

        createTable();

        this.saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public synchronized static boolean playerDataContainsPlayer(Player player) {
        try {
            PreparedStatement sql = MySQL.getConnection().prepareStatement("SELECT * FROM `"
                    + table + " WHERE player=?;");
            sql.setString(1, player.getName());
            ResultSet resultSet = sql.executeQuery();
            boolean containsPlayer = resultSet.next();

            sql.close();
            resultSet.close();

            return containsPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        try {
            if (playerDataContainsPlayer(event.getPlayer())) {
                PreparedStatement econExistCheck = MySQL.getConnection()
                        .prepareStatement("SELECT player FROM `" + table
                                + "` WHERE player=?;");
                econExistCheck.setString(1, player.getName());

                ResultSet resultset = econExistCheck.executeQuery();
                resultset.next();
            } else {
                PreparedStatement econRegister = MySQL.getConnection()
                        .prepareStatement("INSERT INTO `" + table
                                + "` (player, money, active) VALUES (?, ?, ?);");
                econRegister.setString(1, event.getPlayer().getName());
                econRegister.setString(2, defMoney);
                econRegister.setString(3, "active");
                econRegister.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTable() {
        try {
            PreparedStatement tableCreate = MySQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `"
                            + table
                            + "` (`player_id` INT(11) AUTO_INCREMENT, `player` VARCHAR(16), `money` VARCHAR(10), `active` VARCHAR(1));");

            tableCreate.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void getBalance(String player) {
    	try {
    		PreparedStatement balance = MySQL.getConnection().prepareStatement("SELECT money FROM `" + table + "` WHERE player=?;");
    		balance.setString(1, player);
    		
    		balance.executeQuery();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public static void giveMoney(String player, int amount) {
    	try {
    		PreparedStatement getBal = MySQL.getConnection().prepareStatement("SELECT money FROM `" + table + "` WHERE player=?;"); 
    		getBal.setString(1, player);
    		String query = null;
    		getBal.executeQuery(query);
    		int money = Integer.parseInt(query);
    		
    		int Bal;
    		Bal = money + amount;
    		
    		PreparedStatement giveMon = MySQL.getConnection().prepareStatement("INSERT INTO `" + table + "` (`money`) VALUES ("?");");
    		giveMon.setString(1, Bal);
    		giveMon.executeUpdate();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public static void removeMoney(String player, int amount) {
    	try {
    		PreparedStatement getBal = MySQL.getConnection().prepareStatement("SELECT money FROM `" + table + "` WHERE player=?;"); 
    		getBal.setString(1, player);
    		String query = null;
    		getBal.executeQuery(query);
    		int money = Integer.parseInt(query);
    		
    		int Bal;
    		Bal = money - amount;
    		
    		PreparedStatement removeMon = MySQL.getConnection().prepareStatement("INSERT INTO `" + table + "` (`money`) VALUES ("?");");
    		removeMon.setString(1, Bal)
    		removeMon.executeUpdate();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public static void deleteAccount(String player) {
        try {
            PreparedStatement remAcc = MySQL.getConnection().prepareStatement("INSERT INTO `" + table + "` (`active`) VALUES ("active");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        if (cmd.getName().equalsIgnoreCase("money")) {
            try {
                PreparedStatement getMoney = MySQL.getConnection().prepareStatement("SELECT money FROM `" + table + "` WHERE player=?;");
                getMoney.setString(1, sender.getName());
                
                String query = null;
                
                getMoney.executeQuery(query);

                sender.sendMessage("" + query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
