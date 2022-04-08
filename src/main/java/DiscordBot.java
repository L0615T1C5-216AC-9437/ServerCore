import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.net.Administration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.sql.PreparedStatement;

public class DiscordBot extends ListenerAdapter {
    public static DiscordBot bot = null;

    public JDA api;
    public String prefix;
    public CommandHandler commandHandler;

    public Guild guild = null;
    public TextChannel console = null;

    public DiscordBot() {
        String apiKey = PluginConfig.DiscordApiKey.string();
        if (apiKey.isEmpty()) {
            Log.err("Discord API Key not setup. Unable to load discord bot.");
        } else {
            JLib.TimedLogging tl = new JLib.TimedLogging();
            try {
                api = JDABuilder.createDefault(apiKey)
                        .addEventListeners(this)//Listen for events such as message sent
                        .build();
                tl.log("Loaded Discord API!");
                prefix = PluginConfig.DiscordPrefix.string();
                tl.log("Bot prefix set to '" + prefix + "'");
                commandHandler = new CommandHandler(prefix);
                RegisterDiscordCommands(commandHandler);
                tl.log("Commands Loaded");
                bot = this;
            } catch (LoginException e) {
                e.printStackTrace();
                Log.err("Failed to load discord bot!");
            }
        }
    }

    public void shutdown() {
        api.shutdownNow();
    }

    //discord listener events
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        Log.info("Discord bot is online.");
        long guildID = PluginConfig.DiscordGuild.longNum();
        if (guildID == 0L) {
            Log.err("Guild ID not setup. Unable to load channels.");
        } else {
            guild = api.getGuildById(guildID);
            if (guild == null) {
                Log.err("No guild found/available with the guild id " + guildID);
                return;
            }
            long consoleID = PluginConfig.ConsoleChannel.longNum();
            if (consoleID == 0L) {
                Log.err("Console channel ID not setup.");
            } else {
                console = guild.getTextChannelById(consoleID);
                if (console == null) {
                    Log.err("No Text Channel with the ID " + consoleID + " found in " + guild.getName());
                }
                console.sendMessage("Discord bot loaded for server `" + Administration.Config.name.string() + "` port:" + Administration.Config.port.num()).queue();
            }
        }
    }

    public static void loadBot() {
        if (bot != null) {
            bot.shutdown();
            bot = null;
        }
        new DiscordBot();
    }

    public static void RegisterDiscordCommands(CommandHandler handler) {
        //insert commands here
    }

    public static void registerClientCommands(CommandHandler handler) {
        //insert commands here
    }

    public static void registerServerCommands(CommandHandler handler) {
        //insert commands here
    }

    public static void logCatch(Exception e) {

    }

    public static void logCatch(Exception e, PreparedStatement statement, Throwable t) {

    }
}