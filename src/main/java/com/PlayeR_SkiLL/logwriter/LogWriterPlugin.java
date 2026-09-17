package com.Player_SkiLL.logwriter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriterPlugin extends JavaPlugin implements Listener {

    // Статическое сообщение, которое не редактируется через конфиг
    private static final String LOGWRITER_INFO = "&f[&aLogWriter&f] &7Простой плагин от &6PlayeR_SkiLL &7для логирования действий.";

    @Override
    public void onEnable() {
        // Регистрация слушателя событий
        getServer().getPluginManager().registerEvents(this, this);
        // Вывод фиксированного сообщения в консоль при включении плагина
        getLogger().info(ChatColor.translateAlternateColorCodes('&', LOGWRITER_INFO));
    }

    @Override
    public void onDisable() {
        getLogger().info("LogWriter выключен");
    }

    // Обработка команды /logwrite
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("logwrite")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Используйте: /logwrite <сообщение>");
                return true;
            }
            String message = String.join(" ", args);
            logMessage(sender.getName(), message);
            sender.sendMessage(ChatColor.GREEN + "Сообщение записано в лог.");
            return true;
        }
        return false;
    }

    // Обработка события входа игрока
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        logMessage("SYSTEM", "Игрок " + player.getName() + " зашел на сервер");
    }

    // Метод для записи логов
    private void logMessage(String senderName, String message) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logLine = "[" + timeStamp + "] [" + senderName + "] " + message;

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // Создаем папку, если ее нет
        }

        try (FileWriter writer = new FileWriter(new File(dataFolder, "log.txt"), true)) {
            writer.write(logLine + "\n");
        } catch (IOException e) {
            getLogger().warning("Ошибка записи лога: " + e.getMessage());
        }
    }
}
