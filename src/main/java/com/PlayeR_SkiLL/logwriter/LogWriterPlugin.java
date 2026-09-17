package com.Player_SkiLL.logwriter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
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

        // Создаем файл конфигурации log-mappings.yml, если его нет
        File mappingConfig = new File(getDataFolder(), "log-mappings.yml");
        if (!mappingConfig.exists()) {
            try (InputStream in = getResource("log-mappings.yml")) {
                if (in != null) {
                    Files.copy(in, mappingConfig.toPath());
                }
            } catch (IOException e) {
                getLogger().severe("Не удалось создать log-mappings.yml");
            }
        }

        // Загружаем связи из конфигурации
        loadMappings();

        // Инициализация файлов логов
        initializeLogFiles();

        // Регистрация команды
        this.getCommand("logwrite").setExecutor(this);
    }

    private void loadMappings() {
        try (Reader reader = new FileReader(new File(getDataFolder(), "log-mappings.yml"))) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            if (data != null && data.containsKey("logs")) {
                Map<String, Object> logs = (Map<String, Object>) data.get("logs");
                for (Map.Entry<String, Object> entry : logs.entrySet()) {
                    try {
                        int key = Integer.parseInt(entry.getKey());
                        String filename = entry.getValue().toString();
                        logMappings.put(key, filename);
                    } catch (NumberFormatException e) {
                        getLogger().warning("Некорректный ключ в log-mappings.yml: " + entry.getKey());
                    }
                }
            }
        } catch (IOException e) {
            getLogger().severe("Ошибка чтения log-mappings.yml");
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
