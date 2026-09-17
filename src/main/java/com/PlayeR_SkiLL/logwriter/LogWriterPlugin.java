package com.Player_SkiLL.logwriter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LogWriterPlugin extends JavaPlugin implements CommandExecutor {
    private Map<Integer, String> logMappings = new HashMap<>();
    private Map<Integer, File> logFiles = new HashMap<>();

    @Override
    public void onEnable() {
        // Создаем папку, если нет
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Загружаем конфиг (уже есть по умолчанию)
        saveDefaultConfig();

        // Загружаем связи из конфигурационного файла
        loadMappings();

        // Инициализация файлов логов
        initializeLogFiles();

        // Регистрация команды
        this.getCommand("logwrite").setExecutor(this);
    }

    private void loadMappings() {
        // Читаем раздел logs из config.yml
        for (String keyStr : getConfig().getConfigurationSection("logs").getKeys(false)) {
            try {
                int key = Integer.parseInt(keyStr);
                String filename = getConfig().getString("logs." + keyStr);
                logMappings.put(key, filename);
            } catch (NumberFormatException e) {
                getLogger().warning("Некорректный ключ в конфиге: " + keyStr);
            }
        }
    }

    private void initializeLogFiles() {
        for (Map.Entry<Integer, String> entry : logMappings.entrySet()) {
            File logFile = new File(getDataFolder(), entry.getValue());
            logFiles.put(entry.getKey(), logFile);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("logwrite")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Используйте: /logwrite <число> <сообщение>");
                return true;
            }

            int logNumber;
            try {
                logNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Первый аргумент должен быть числом");
                return true;
            }

            String message = String.join(" ", args, 1, args.length);
            File logFile = logFiles.get(logNumber);

            if (logFile == null) {
                sender.sendMessage(ChatColor.RED + "Для этого номера лог файла не настроен");
                return true;
            }

            try (FileWriter writer = new FileWriter(logFile, true)) {
                String logEntry = "[" + java.time.LocalDateTime.now() + "] " + message + "\n";
                writer.write(logEntry);
                sender.sendMessage(ChatColor.GREEN + "Сообщение записано в лог " + logNumber);
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Ошибка записи в лог файл");
            }
            return true;
        }
        return false;
    }
}
      
