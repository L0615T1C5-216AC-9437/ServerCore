import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.Packets;

public class ExampleModule {
    public static void RegisterEvents() {
        Events.on(EventType.PlayerJoin.class, event -> {
            Player p = event.player;
            Object[] data = Sql.get_SM(new Sql.DataType[]{Sql.DataType.Long, Sql.DataType.Boolean}, "SELECT `lastKick`, `ban` FROM banList WHERE UUID = ? LIMIT 1;", JLib.B64Decoder.decode(p.uuid()));
            if (data != null) {
                if (data[1] != null && (boolean) data[1]) {
                    Call.kick(p.con, Packets.KickReason.banned);
                    p.con.close();//Some hack clients can ignore the Call.kick, thus a  forced connection close is required.
                } else if (data[0] != null && (long) data[0] > System.currentTimeMillis()) {
                    Call.kick(p.con, Packets.KickReason.recentKick);
                    p.con.close();//Some hack clients can ignore the Call.kick, thus a  forced connection close is required.
                }
            }
        });
    }
}
