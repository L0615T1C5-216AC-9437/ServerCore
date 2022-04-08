import arc.Core;

public enum PluginConfig {
    DiscordApiKey("The API Key for Discord Bot", ""),
    DiscordPrefix("The prefix used for calling commands", "!"),
    DiscordGuild("The ID of the Discord Guild", JLib.longToB64(0L), true),
    ConsoleChannel("The ID of the console channel", JLib.longToB64(0L), true),
    SqlLoginString("The string used to create a JDBC Connection", "");

    public static final PluginConfig[] all = values();

    public final Object defaultValue;
    public final String key, description;
    final Runnable changed;
    final boolean b64;

    PluginConfig(String description, Object def) {
        this(description, def, null, null);
    }

    PluginConfig(String description, Object def, boolean b64) {
        this(description, def, null, null, b64);
    }

    PluginConfig(String description, Object def, String key, Runnable changed) {
        this(description, def, key, changed, false);
    }

    PluginConfig(String description, Object def, String key, Runnable changed, boolean b64) {
        this.description = description;
        this.key = "sc_" + (key == null ? name() : key);
        this.defaultValue = def;
        this.changed = changed == null ? () -> {
        } : changed;
        this.b64 = b64;
    }

    public boolean isNum() {
        return defaultValue instanceof Integer;
    }

    public boolean isBool() {
        return defaultValue instanceof Boolean;
    }

    public boolean isB64() {
        return b64;
    }

    public boolean isString() {
        return defaultValue instanceof String;
    }

    public Object get() {
        return Core.settings.get(key, defaultValue);
    }

    public boolean bool() {
        return Core.settings.getBool(key, (Boolean) defaultValue);
    }

    public int num() {
        return Core.settings.getInt(key, (Integer) defaultValue);
    }

    public String string() {
        return Core.settings.getString(key, (String) defaultValue);
    }

    public long longNum() {
        return JLib.b64ToLong(Core.settings.getString(key, (String) defaultValue));
    }

    public void set(Object value) {
        Core.settings.put(key, value);
        changed.run();
    }
}