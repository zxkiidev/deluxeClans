package com.vediikaz.clan;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class ClanPlugin extends JavaPlugin implements Listener {

    private String bannerEnable;
    private String bannerDisable;

    private File clansFile;
    private FileConfiguration clansConfig;

    private final Map<UUID, BossBar> creatingClan = new HashMap<>();
    private final Map<UUID, BukkitTask> timers = new HashMap<>();

    @Override
    public void onEnable() {
        String version = getPluginMeta().getVersion();

        bannerEnable =
                "╔══════════════════════════╗\n" +
                "║   DELUXECLANS v" + version + "     ║\n" +
                "║       ACTIVADO ✅        ║\n" +
                "║  Gracias por instalar!   ║\n" +
                "║   Creado por Vediikaz    ║\n" +
                "╚══════════════════════════╝";

        bannerDisable =
                "╔══════════════════════════╗\n" +
                "║   DELUXECLANS v" + version + "     ║\n" +
                "║      DESACTIVADO ❌      ║\n" +
                "║         Adios!           ║\n" +
                "║   Creado por Vediikaz    ║\n" +
                "╚══════════════════════════╝";

        getLogger().info("\n" + bannerEnable);

        saveDefaultConfig();
        createClansFile();

        this.getCommand("clan").setExecutor(new ClanCommand(this));

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("\n" + bannerDisable);
    }

    // Comienzo de métodos utilitarios
    public int getMaxClanNameLength() {
        return getConfig().getInt("options.maxClanNameLength", 20);
    }

    public int getMinClanNameLength() {
        return getConfig().getInt("options.minClanNameLength", 3);
    }

    public int getMaxClansPerPlayer() {
        return getConfig().getInt("options.maxClansPerPlayer", 1);
    }

    public boolean allowClanJoinRequests() {
        return getConfig().getBoolean("options.allowClanJoinRequests", true);
    }

    private void createClansFile() {
        clansFile = new File(getDataFolder(), "clans.yml");

        if (!clansFile.exists()) {
            clansFile.getParentFile().mkdirs();
            try {
                clansFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        clansConfig = YamlConfiguration.loadConfiguration(clansFile);
    }

    public FileConfiguration getClansConfig() {
        return clansConfig;
    }

    public void saveClansConfig() {
        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadClansConfig() {
        clansConfig = YamlConfiguration.loadConfiguration(clansFile);
    }

    // Inicia el proceso de creación de clan para un jugador
    public void startClanCreation(Player player) {
        UUID uuid = player.getUniqueId();

        if (creatingClan.containsKey(uuid)) {
            player.sendMessage("§cYa estás creando un clan.");
            return;
        }

        BossBar bar = Bukkit.createBossBar(
                "§eEscribe el nombre de tu clan (60s)", 
                BarColor.BLUE, 
                BarStyle.SOLID
        );
        bar.addPlayer(player);
        bar.setProgress(1.0);

        creatingClan.put(uuid, bar);

        // Temporizador de 60s
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            int time = 60;

            @Override
            public void run() {
                if (!creatingClan.containsKey(uuid)) {
                    cancelTask(uuid);
                    return;
                }

                time--;
                double progress = time / 60.0;
                bar.setProgress(progress);
                bar.setTitle("§eEscribe el nombre de tu clan (" + time + "s)");

                if (time <= 0) {
                    player.sendMessage("§cSe acabó el tiempo para crear un clan.");
                    bar.removeAll();
                    creatingClan.remove(uuid);
                    cancelTask(uuid);
                }
            }
        }, 20, 20);

        timers.put(uuid, task);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!creatingClan.containsKey(uuid)) return;

        event.setCancelled(true); 

        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancelar")) {
            BossBar bar = creatingClan.remove(uuid);
            if (bar != null) bar.removeAll();
            cancelTask(uuid);

            player.sendMessage("§cCreación de clan cancelada.");
            return;
        }

        if (message.length() < getMinClanNameLength() || message.length() > getMaxClanNameLength()) {
            player.sendMessage("§cEl nombre no cumple con los requisitos.");
            return;
        }

        FileConfiguration clansConfig = getClansConfig();
        if (clansConfig.getString("players." + uuid) != null) {
            player.sendMessage("§cYa tienes un clan asignado.");
            return;
        }

        // Guardamos clan con owner y members
        clansConfig.set("players." + uuid, message);

        String path = "clans." + message;
        if (!clansConfig.contains(path)) {
            clansConfig.set(path + ".owner", uuid.toString());
            clansConfig.set(path + ".created", String.valueOf(System.currentTimeMillis()));
            List<String> members = new ArrayList<>();
            members.add(uuid.toString());
            clansConfig.set(path + ".members", members);
        }

        saveClansConfig();

        player.sendMessage("§aClan creado con éxito: §e" + message);

        BossBar bar = creatingClan.remove(uuid);
        if (bar != null) bar.removeAll();
        cancelTask(uuid);
    }

    private void cancelTask(UUID uuid) {
        if (timers.containsKey(uuid)) {
            timers.get(uuid).cancel();
            timers.remove(uuid);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        String title = LegacyComponentSerializer.legacySection().serialize(event.getView().title());

        if (title.equalsIgnoreCase("§8Menú de Clanes")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            String name = LegacyComponentSerializer.legacySection().serialize(event.getCurrentItem().getItemMeta().displayName());

            switch (name) {
                case "§aCrear Clan":
                    player.closeInventory();
                    player.performCommand("clan crear");
                    break;
                case "§bUnirse a Clan":
                    player.closeInventory();
                    player.sendMessage("§eEscribe en el chat: /clan unirse <nombre>");
                    break;
                case "§6Información":
                    player.closeInventory();
                    player.performCommand("clan info");
                    break;
                case "§cSalir del Clan":
                    player.closeInventory();
                    player.performCommand("clan salir");
                    break;
                case "§cCerrar Menú":
                    player.closeInventory();
                    break;
            }
        }
    }

}
