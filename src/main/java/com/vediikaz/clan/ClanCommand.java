package com.vediikaz.clan;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public class ClanCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ClanCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Component getPrefix() {
        String prefix = plugin.getConfig().getString("prefix");
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);
    }

    private void sendMessage(Player player, String path) {
        String raw = plugin.getConfig().getString(path, "&cMensaje no definido");
        if (raw == null || raw.isEmpty()) return;

        String[] lines = raw.split("\\r?\\n"); 
        for (String line : lines) {
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
            player.sendMessage(getPrefix().append(component));
        }
    }


    private void sendRawMessage(Player player, String msg) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        player.sendMessage(getPrefix().append(component));
    }

    private int getMaxClanNameLength() {
        return plugin.getConfig().getInt("options.maxClanNameLength", 20);
    }

    private int getMinClanNameLength() {
        return plugin.getConfig().getInt("options.minClanNameLength", 3);
    }

    private int getMaxClansPerPlayer() {
        return plugin.getConfig().getInt("options.maxClansPerPlayer", 1);
    }

    private boolean allowClanJoinRequests() {
        return plugin.getConfig().getBoolean("options.allowClanJoinRequests", true);
    }

    private String getClanTagFormat(String tag) {
        String format = plugin.getConfig().getString("options.clanTagFormat", "[{tag}]");
        return format.replace("{tag}", tag);
    }

    private FileConfiguration getClansConfig() {
        if (plugin instanceof com.vediikaz.clan.ClanPlugin) {
            com.vediikaz.clan.ClanPlugin cp = (com.vediikaz.clan.ClanPlugin) plugin;
            return cp.getClansConfig();
        }
        return null;
    }

    private void saveClansConfig() {
        if (plugin instanceof com.vediikaz.clan.ClanPlugin) {
            com.vediikaz.clan.ClanPlugin cp = (com.vediikaz.clan.ClanPlugin) plugin;
            cp.saveClansConfig();
        }
    }

    private ItemStack makeItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
            if (lore != null) {
                List<Component> comps = new ArrayList<>();
                for (String line : lore) {
                    comps.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
                }
                meta.lore(comps);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openClanMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8Menú de Clanes"));

        String nameCreate = "&aCrear Clan";
        List<String> loreCreate = Arrays.asList("&7Usa esto para crear un clan.", "&eClick para usar");

        String nameJoin = "&bUnirse a Clan";
        List<String> loreJoin = Arrays.asList("&7Solicita unirte a un clan existente.", "&eClick para usar");

        String nameInfo = "&6Información";
        List<String> loreInfo = Arrays.asList("&7Muestra información de tu clan.", "&eClick para usar");

        String nameLeave = "&cSalir del Clan";
        List<String> loreLeave = Arrays.asList("&7Abandona tu clan actual.", "&eClick para usar");

        ItemStack create = makeItem(Material.NETHER_STAR, nameCreate, loreCreate);
        ItemStack join   = makeItem(Material.PAPER, nameJoin, loreJoin);
        ItemStack info   = makeItem(Material.COMPASS, nameInfo, loreInfo);
        ItemStack leave  = makeItem(Material.BARRIER, nameLeave, loreLeave);

        gui.setItem(10, create);
        gui.setItem(12, join);
        gui.setItem(14, info);
        gui.setItem(16, leave);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fmeta = filler.getItemMeta();
        if (fmeta != null) {
            fmeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(" "));
            filler.setItemMeta(fmeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) gui.setItem(i, filler);
        }

        ItemStack close = makeItem(Material.OAK_DOOR, "&cCerrar Menú", Arrays.asList("&7Cierra este menú"));
        gui.setItem(26, close);

        player.openInventory(gui);
    }

    private void sendClanStartMessage(Player player) {
        Component prefix = getPrefix();

        String[] lines = new String[] {
            "§aIniciando creación de clan...",
            "§ePon tu nombre de clan en los próximos 60 segundos.",
            "§cUsa \"cancelar\" para detener el proceso."
        };

        player.sendMessage(prefix.append(LegacyComponentSerializer.legacyAmpersand().deserialize("§7-------------------------")));

        for (String line : lines) {
            player.sendMessage(prefix.append(LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
        }

        player.sendMessage(prefix.append(LegacyComponentSerializer.legacyAmpersand().deserialize("§7-------------------------")));
    }

    private void sendMessageWithPlaceholders(Player player, String path, Map<String, String> extraPlaceholders) {
        String msg = plugin.getConfig().getString(path, "&cMensaje no definido");

        if (extraPlaceholders == null) extraPlaceholders = new HashMap<>();

        extraPlaceholders.put("{prefix}", LegacyComponentSerializer.legacyAmpersand().serialize(getPrefix()));
        extraPlaceholders.put("{jugador}", player.getName());

        for (Map.Entry<String, String> entry : extraPlaceholders.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
        }

        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        player.sendMessage(message);
    }

    private Map<String, String> getClanPlaceholders(String clanName) {
        Map<String, String> placeholders = new HashMap<>();
        FileConfiguration config = getClansConfig();

        if (config.contains("clans." + clanName)) {
            String ownerUUID = config.getString("clans." + clanName + ".owner");
            String created = config.getString("clans." + clanName + ".created");
            List<String> members = config.getStringList("clans." + clanName + ".members");

            placeholders.put("{clan}", clanName);
            placeholders.put("{owner}", Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName());
            placeholders.put("{created}", created);
            placeholders.put("{members}", String.valueOf(members.size()));
            placeholders.put("{member_list}", members.stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName())
                    .collect(Collectors.joining(", ")));
        }

        return placeholders;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration clansConfig = getClansConfig();
        if (clansConfig == null) {
            sendMessage(player, "messages.errorLoadingClans");
            return true;
        }

        if (args.length == 0) {
            sendMessage(player, "messages.usage");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "crear":
                if (plugin instanceof ClanPlugin) {
                    sendClanStartMessage(player);
                    ((ClanPlugin) plugin).startClanCreation(player);
                } else {
                    sendMessage(player, "messages.errorCreatingClan");
                }
                break;

            case "unirse": {
                if (!allowClanJoinRequests()) {
                    sendMessage(player, "messages.joinDisabled");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(player, "messages.noClanName");
                    return true;
                }

                String targetClan = args[1];

                if (!clansConfig.contains("clans." + targetClan)) {
                    sendRawMessage(player, "&cEse clan no existe.");
                    return true;
                }

                clansConfig.set("players." + player.getUniqueId(), targetClan);

                List<String> members = clansConfig.getStringList("clans." + targetClan + ".members");
                if (!members.contains(player.getUniqueId().toString())) {
                    members.add(player.getUniqueId().toString());
                }
                clansConfig.set("clans." + targetClan + ".members", members);

                saveClansConfig();
                sendMessage(player, "messages.clanJoined");
                break;
            }

            case "salir": {
                String clanName = clansConfig.getString("players." + player.getUniqueId());
                if (clanName != null) {
                    clansConfig.set("players." + player.getUniqueId(), null);

                    List<String> members = clansConfig.getStringList("clans." + clanName + ".members");
                    members.remove(player.getUniqueId().toString());
                    clansConfig.set("clans." + clanName + ".members", members);

                    saveClansConfig();
                    sendMessage(player, "messages.clanLeft");
                } else {
                    sendMessage(player, "messages.noClanAssigned");
                }
                break;
            }

            case "disolver": {
                String clanName = clansConfig.getString("players." + player.getUniqueId());
                if (clanName != null) {
                    String owner = clansConfig.getString("clans." + clanName + ".owner");
                    if (owner != null && owner.equals(player.getUniqueId().toString())) {
                        // Eliminar a todos los jugadores
                        List<String> members = clansConfig.getStringList("clans." + clanName + ".members");
                        for (String memberUUID : members) {
                            clansConfig.set("players." + memberUUID, null);
                        }

                        // Eliminar clan
                        clansConfig.set("clans." + clanName, null);
                        saveClansConfig();
                        sendMessage(player, "messages.clanDisbanded");
                    } else {
                        sendMessage(player, "messages.notOwner");
                    }
                } else {
                    sendMessage(player, "messages.noClanAssigned");
                }
                break;
            }

            case "info": {
                String clanName = clansConfig.getString("players." + player.getUniqueId());
                if (clanName != null) {
                    Map<String, String> placeholders = getClanPlaceholders(clanName);
                    sendMessageWithPlaceholders(player, "messages.clanInfo", placeholders);
                } else {
                    sendMessage(player, "messages.noClanAssigned");
                }
                break;
            }

            case "menu":
                openClanMenu(player);
                break;

            case "ayuda":
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&6Comandos de Clan:")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan crear <nombre> &7- Crear un clan")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan unirse <nombre> &7- Unirse a un clan")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan salir &7- Salir de tu clan")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan disolver &7- Disolver tu clan")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan info &7- Ver la información de tu clan")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan menu &7- Abrir el menú de clanes")));
                player.sendMessage(getPrefix().append(LegacyComponentSerializer.legacyAmpersand().deserialize("&e/clan ayuda &7- Mostrar este mensaje")));
                break;

            default:
                sendMessage(player, "messages.usage");
                break;
        }


        return true;
    }
}
