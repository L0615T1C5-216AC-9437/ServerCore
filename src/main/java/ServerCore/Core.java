package ServerCore;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.mod.Plugin;

public class Core extends Plugin {
    public static DiscordBot discordBot = null;

    @Override
    public void init() {
        Events.on(EventType.ServerLoadEvent.class, event -> {
            DiscordBot.loadBot();
        });

        Sql.RegisterEvents();//has to be first in order for other modules to be able to use SQL during ServerLoadEvent
        //ServerCore.Example.SqlBanList.RegisterEvents();
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("scconfig", "[name] [value...]", "Configure server settings.", arg -> {
            if (arg.length == 0) {
                Log.info("All config values:");
                for (PluginConfig c : PluginConfig.all) {
                    Log.info("&lk| @: @", c.name(), "&lc&fi" + c.get());
                    Log.info("&lk| | &lw" + c.description);
                    Log.info("&lk|");
                }
                Log.info("use the command with the value set to \"default\" in order to use the default value.");
                return;
            }

            try {
                PluginConfig c = PluginConfig.valueOf(arg[0]);
                if (arg.length == 1) {
                    Log.info("'@' is currently @.", c.name(), c.get());
                } else {
                    if (arg[1].equals("default")) {
                        c.set(c.defaultValue);
                    } else if (c.isBool()) {
                        c.set(arg[1].equals("on") || arg[1].equals("true"));
                    } else if (c.isNum()) {
                        try {
                            c.set(Integer.parseInt(arg[1]));
                        } catch (NumberFormatException e) {
                            Log.err("Not a valid number: @", arg[1]);
                            return;
                        }
                    } else if (c.isString()) {
                        if (c.isB64()) {
                            try {
                                c.set(JLib.longToB64(Long.parseLong(arg[1])));
                            } catch (Exception e) {
                                Log.err("Not a valid number: @", arg[1]);
                                return;
                            }
                        } else {
                            c.set(arg[1].replace("\\n", "\n"));
                        }
                    }

                    Log.info("@ set to @.", c.name(), c.get());
                    arc.Core.settings.forceSave();
                }
            } catch (IllegalArgumentException e) {
                Log.err("Unknown ServerPluginConfig: '@'. Run the command with no arguments to get a list of valid ServerPluginConfigs.", arg[0]);
            }
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        DiscordBot.registerClientCommands(handler);
    }
}
