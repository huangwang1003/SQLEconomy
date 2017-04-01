package io.github.andrewward2001.sqlecon;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.andrewward2001.sqlecon.hooks.VaultConnector;
import io.github.andrewward2001.sqlecon.hooks.Dependency;
import io.github.andrewward2001.sqlecon.mysql.*;
import net.milkbowl.vault.economy.Economy;

import java.sql.Connection;

public class SQLEconomy extends JavaPlugin implements Listener {

	private Plugin plugin;
	
	public static SQLEconomy S;

	// Making variables for SQL connection static, escaping errors
	private static String host;
	private static String port;
	private static String database;
	private static String table;
	private static String user;
	private static String pass;
	private static String defMoney;

	public static String moneyUnit;

	private static MySQL MySQL;
	static Connection c;

	public void onDisable() {
	}

	public void onEnable() {
		this.saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		this.saveConfig();

		host = getConfig().getString("DatabaseHostIP");
		port = getConfig().getString("DatabasePort");
		database = getConfig().getString("DatabaseName");
		table = getConfig().getString("DatabaseTable");
		user = getConfig().getString("DatabaseUsername");
		pass = getConfig().getString("DatabasePassword");
		defMoney = getConfig().getString("DefaultMoney");

		moneyUnit = getConfig().getString("MoneyUnit");

		MySQL = new MySQL(host, port, database, user, pass);
		try {
			c = MySQL.openConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		SQLEconomyActions.createTable();

		this.getCommand("money").setExecutor(new MoneyCommand(this));
		Bukkit.getServer().getPluginManager().registerEvents(new SQLEconomyListener(table, defMoney, c), this);
		
		registerEconomy();
	}
	
	public static String getTable() {
		return table;
	}
	
	public static String getDefaultMoney() {
		return defMoney;
	}
	
	public static SQLEconomyAPI getAPI() {
		return new SQLEconomyAPI();
	}
	
	private void registerEconomy() {
        if (Dependency.DEP.vault.exists()) {
            final ServicesManager sm = getServer().getServicesManager();
            sm.register(Economy.class, new VaultConnector(), this, ServicePriority.Highest);
            getLogger().info("Registered Vault interface.");
        } else {
        	getLogger().info("Vault not found. Other plugins may not be able to access Gringotts accounts.");
        }
    }

	public boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

}