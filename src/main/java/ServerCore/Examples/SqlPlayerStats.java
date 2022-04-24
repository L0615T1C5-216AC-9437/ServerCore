package ServerCore.Examples;

import ServerCore.JLib;
import ServerCore.Sql;
import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SqlPlayerStats {
    private static final Seq<String> uuidsThisGame = new Seq<>();

    public static void RegisterEvents() {
        Events.on(EventType.WorldLoadEvent.class, event -> {
            uuidsThisGame.clear();
            final ArrayList<Sql.bulkData> playerUUIDs = new ArrayList<>();
            for (Player p : Groups.player) {
                playerUUIDs.add(new Sql.bulkData(JLib.B64Decoder.decode(p.uuid())));
                uuidsThisGame.add(p.uuid());
            }
            Sql.runBulk("INSERT INTO playerStats (`UUID`, `firstJoin`, `gamesPlayed`) VALUES (?, NOW(), 1) ON DUPLICATE KEY UPDATE gamesPlayed = gamesPlayed + 1;", playerUUIDs);
        });

        Events.on(EventType.PlayerJoin.class, event -> {
            if (!uuidsThisGame.contains(event.player.uuid())) {
                uuidsThisGame.add(event.player.uuid());
                Sql.run("INSERT INTO playerStats (`UUID`, `firstJoin`, `gamesPlayed`) VALUES (?, NOW(), 1) ON DUPLICATE KEY UPDATE gamesPlayed = gamesPlayed + 1;", JLib.B64Decoder.decode(event.player.uuid()));
            }
        });
    }

    public static void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("stats", "Get your stats.", (args, p) -> {//use `p` for player rather than `player` so you dont accidentally use the null player Vars.player
            Object[] data = Sql.get_SM(new Sql.DataType[]{Sql.DataType.Timestamp, Sql.DataType.Int}, "SELECT (`firstJoin`, `gamesPlayed`) FROM playerStats WHERE UUID = ? LIMIT 1;", JLib.B64Decoder.decode(p.uuid()));
            if (data == null) {
                p.sendMessage("No stats available!");
            } else {
                p.sendMessage("First Join: " + new SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format((Date) data[0]));
                p.sendMessage("Games Played: " + data[1]);
            }
        });
    }
}
