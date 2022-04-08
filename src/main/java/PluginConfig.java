import arc.Core;

public enum PluginConfig {
    DiscordApiKey("The API Key for Discord Bot", ""),
    DiscordPrefix("The prefix used for calling commands", "!"),
    DiscordGuild("The ID of the console channel", JLib.longToB64(0L)),
    ConsoleChannel("The ID of the console channel", JLib.longToB64(0L)),
    SqlLoginString("The string used to create a JDBC Connection", "");

    public static final PluginConfig[] all = values();

    public final Object defaultValue;
    public final String key, description;
    final Runnable changed;

    PluginConfig(String description, Object def) {
        this(description, def, null, null);
    }

    PluginConfig(String description, Object def, String key) {
        this(description, def, key, null);
    }

    PluginConfig(String description, Object def, Runnable changed) {
        this(description, def, null, changed);
    }

    PluginConfig(String description, Object def, String key, Runnable changed) {
        this.description = description;
        this.key = "sc_" + (key == null ? name() : key);
        this.defaultValue = def;
        this.changed = changed == null ? () -> {
        } : changed;
    }

    public boolean isNum() {
        return defaultValue instanceof Integer;
    }

    public boolean isBool() {
        return defaultValue instanceof Boolean;
    }

    public boolean isB64() {
        try {
            JLib.B64Decoder.decode((String) defaultValue);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
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