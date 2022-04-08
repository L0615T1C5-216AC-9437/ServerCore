import arc.Events;
import arc.util.Log;
import mindustry.game.EventType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Sql {
    private static final String driverString = "com.mysql.cj.jdbc.Driver";
    private static String url;
    //e.g. jdbc:mysql://localhost:3306/mindustry?user=usernameHere&password=passwordHere&allowMultiQueries=true
    //allowMultiQueries=true is required to have multiple queries per connection, aka a query like "SELECT 1;\nSELECT 2;\nSELECT 3;"

    public static void RegisterEvents() {
        Events.on(EventType.ServerLoadEvent.class, event -> {
            url = PluginConfig.SqlLoginString.string();
            if (url.isEmpty()) {
                Log.err("No SQL login string setup!");
            } else {
                try {
                    SqlCon sc = new SqlCon("SELECT 1;");
                    if (!sc.valid()) {
                        Log.err("Unable to create SQL Connection.");
                    } else {
                        sc.getResultSet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.err("Unable to create SQL Connection.");
                }
            }
        });
    }

    private static class SqlCon {
        public Connection connection = null;
        public PreparedStatement statement = null;
        private ResultSet rs;

        public SqlCon() {
        }

        public SqlCon(String query) {
            try {
                getConnection();
                setStatement(query);
            } catch (SQLException e) {
                DiscordBot.logCatch(e);
            }
        }

        public void getConnection() throws SQLException {
            //makes sure the JDBC driver is available
            try {
                Class.forName(driverString);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
            connection = DriverManager.getConnection(PluginConfig.SqlLoginString.string());
        }

        public void setStatement(String query) throws SQLException {
            statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        }

        public void setStatementAI(String query, String aiColumn) throws SQLException {
            statement = connection.prepareStatement(query, new String[]{aiColumn});
        }

        public boolean valid() {
            return connection != null && statement != null;
        }

        public void setObject(Object object) throws SQLException {
            statement.setObject(1, object);
        }

        public void setObjects(Object... objects) throws SQLException {
            int p = 0;
            for (Object o : objects) statement.setObject(++p, o);
        }

        public void setBulkObjects(Object... objects) throws SQLException {
            statement.addBatch();
        }

        public void getResultSet() {
            try {
                rs = statement.executeQuery();
            } catch (SQLException e) {
                DiscordBot.logCatch(e, statement, new Throwable());
            }
        }

        public Object[] getValues(DataType[] dt) throws SQLException {
            Object[] values = new Object[rs.getMetaData().getColumnCount()];
            for (int i = 0, c = 1; i < values.length; c++, i++) {
                Object o = dt[i].get(rs, c);
                if (rs.wasNull()) o = null;
                values[i] = o;
            }
            return values;
        }

        public int getRowCount() throws SQLException {
            rs.last();
            int rows = rs.getRow();
            rs.beforeFirst();
            return rows;
        }

        public void closeConnection() {
            try {
                connection.close();
            } catch (SQLException e) {
                DiscordBot.logCatch(e, statement, new Throwable());
            }
        }
    }

    public enum DataType {
        //Data types are required to ensure the objects returned can be properly casted.
        //e.g. If you always used `rs.getObject(column)`, in order to get a long you would need something like the following:
        //new BigInteger(rs.getObject(column)).longValue();
        //other wise you would get a cast exception
        String {
            @Override
            public String get(ResultSet rs, int column) throws SQLException {
                return rs.getString(column);
            }
        },
        Boolean {
            @Override
            public Boolean get(ResultSet rs, int column) throws SQLException {
                return rs.getBoolean(column);
            }
        },
        Byte {
            @Override
            public Byte get(ResultSet rs, int column) throws SQLException {
                return rs.getByte(column);
            }
        },
        Short {
            @Override
            public Short get(ResultSet rs, int column) throws SQLException {
                return rs.getShort(column);
            }
        },
        Int {
            @Override
            public Integer get(ResultSet rs, int column) throws SQLException {
                return rs.getInt(column);
            }
        },
        Long {
            @Override
            public Long get(ResultSet rs, int column) throws SQLException {
                return rs.getLong(column);
            }
        },
        Float {
            @Override
            public Float get(ResultSet rs, int column) throws SQLException {
                return rs.getFloat(column);
            }
        },
        Double {
            @Override
            public Double get(ResultSet rs, int column) throws SQLException {
                return rs.getDouble(column);
            }
        },
        BigDecimal {
            @Override
            public java.math.BigDecimal get(ResultSet rs, int column) throws SQLException {
                return rs.getBigDecimal(column);
            }
        },
        Bytes {
            @Override
            public byte[] get(ResultSet rs, int column) throws SQLException {
                return rs.getBytes(column);
            }
        },
        Date {
            @Override
            public Date get(ResultSet rs, int column) throws SQLException {
                return rs.getDate(column);
            }
        },
        Time {
            @Override
            public Time get(ResultSet rs, int column) throws SQLException {
                return rs.getTime(column);
            }
        },
        Timestamp {
            @Override
            public Timestamp get(ResultSet rs, int column) throws SQLException {
                return rs.getTimestamp(column);
            }
        },
        AsciiStream {
            @Override
            public java.io.InputStream get(ResultSet rs, int column) throws SQLException {
                return rs.getAsciiStream(column);
            }
        },
        BinaryStream {
            @Override
            public java.io.InputStream get(ResultSet rs, int column) throws SQLException {
                return rs.getBinaryStream(column);
            }
        };

        public Object get(ResultSet rs, int column) throws SQLException {
            return rs.getObject(column);
        }
    }

    @Nullable
    @CheckReturnValue
    public static Boolean getBoolean(String query) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                } else {
                    return sc.rs.next();
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Boolean getBoolean(String query, Object var) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(var);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                } else {
                    return sc.rs.next();
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Boolean getBoolean(String query, Object... vars) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObjects(vars);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                } else {
                    return sc.rs.next();
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object get_SS_AI(String aiColumn, String query, Object... vars) {
        SqlCon sc = new SqlCon();
        try {
            sc.getConnection();
            sc.setStatementAI(query, aiColumn);
        } catch (SQLException e) {
            e.printStackTrace();
            DiscordBot.logCatch(e, sc.statement, new Throwable());
            return null;
        }
        if (sc.valid()) {
            try {
                sc.setObjects(vars);
                sc.statement.executeUpdate();
                sc.rs = sc.statement.getGeneratedKeys();
                if (sc.rs == null) {
                    return null;
                } else if (sc.rs.next()) {
                    Object o = sc.rs.getLong(1);
                    if (sc.rs.wasNull()) o = null;
                    return o;
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object get_SS(DataType dt, String query) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                } else if (sc.rs.next()) {
                    Object o = dt.get(sc.rs, 1);
                    if (sc.rs.wasNull()) o = null;
                    return o;
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object get_SS(DataType dt, String query, Object var) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(var);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                } else if (sc.rs.next()) {
                    Object o = dt.get(sc.rs, 1);
                    if (sc.rs.wasNull()) o = null;
                    return o;
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object get_SS(DataType dt, String query, Object... vars) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObjects(vars);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                } else if (sc.rs.next()) {
                    Object o = dt.get(sc.rs, 1);
                    if (sc.rs.wasNull()) o = null;
                    return o;
                }
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[] get_SM(DataType[] dt, String query) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.getResultSet();
                if (sc.rs == null || !sc.rs.next()) {
                    return null;
                }
                return sc.getValues(dt);
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[] get_SM(DataType[] dt, String query, Object var) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(var);
                sc.getResultSet();
                if (sc.rs == null || !sc.rs.next()) {
                    return null;
                }
                return sc.getValues(dt);
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[] get_SM(DataType[] dt, String query, Object... vars) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObjects(vars);
                sc.getResultSet();
                if (sc.rs == null || !sc.rs.next()) {
                    return null;
                }
                return sc.getValues(dt);
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[] get_MS(DataType dt, String query) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                }
                Object[] out = new Object[sc.getRowCount()];
                int i = 0;
                while (sc.rs.next()) {
                    Object o = dt.get(sc.rs, 1);
                    if (sc.rs.wasNull()) o = null;
                    out[i++] = o;
                }
                return out;
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[] get_MS(DataType dt, String query, Object var) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(var);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                }
                Object[] out = new Object[sc.getRowCount()];
                int i = 0;
                while (sc.rs.next()) {
                    Object o = dt.get(sc.rs, 1);
                    if (sc.rs.wasNull()) o = null;
                    out[i++] = o;
                }
                return out;
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[] get_MS(DataType dt, String query, Object... vars) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObjects(vars);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                }
                Object[] out = new Object[sc.getRowCount()];
                int i = 0;
                while (sc.rs.next()) {
                    Object o = dt.get(sc.rs, 1);
                    if (sc.rs.wasNull()) o = null;
                    out[i++] = o;
                }
                return out;
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[][] get_MM(DataType[] dt, String query) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                }
                Object[][] out = new Object[sc.getRowCount()][];
                int i = 0;
                while (sc.rs.next()) out[i++] = sc.getValues(dt);
                return out;
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[][] get_MM(DataType[] dt, String query, Object var) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(var);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                }
                Object[][] out = new Object[sc.getRowCount()][];
                int i = 0;
                while (sc.rs.next()) out[i++] = sc.getValues(dt);
                return out;
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    public static Object[][] get_MM(DataType[] dt, String query, Object... vars) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(vars);
                sc.getResultSet();
                if (sc.rs == null) {
                    return null;
                }
                Object[][] out = new Object[sc.getRowCount()][];
                int i = 0;
                while (sc.rs.next()) out[i++] = sc.getValues(dt);
                return out;
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return null;
    }

    public static int run(String query, Object var) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObject(var);
                return sc.statement.executeUpdate();
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return -1;
    }

    public static int run(String query, Object... vars) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            try {
                sc.setObjects(vars);
                return sc.statement.executeUpdate();
            } catch (SQLException e) {
                DiscordBot.logCatch(e, sc.statement, new Throwable());
            } finally {
                sc.closeConnection();
            }
        }
        return -1;
    }

    public static int[] runMultiple(ArrayList<executeQueueData> queue) {
        SqlCon sc = new SqlCon();
        try {
            sc.getConnection();
        } catch (SQLException e) {
            DiscordBot.logCatch(e);
            return null;
        }
        int[] out = new int[queue.size()];
        Arrays.fill(out, -1);

        if (sc.connection != null) {
            for (int i = 0; i < queue.size(); i++) {
                var eqd = queue.get(i);
                try {
                    sc.setStatement(eqd.query);
                    if (sc.valid()) {
                        sc.setObjects(eqd.args);
                        out[i] = sc.statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    DiscordBot.logCatch(e, sc.statement, new Throwable());
                }
            }
        }
        sc.closeConnection();
        return out;
    }

    public static int[] runBulk(String query, ArrayList<bulkData> queue) {
        SqlCon sc = new SqlCon(query);
        if (sc.valid()) {
            for (bulkData ebd : queue) {
                try {
                    sc.setObjects(ebd.args);
                    sc.statement.addBatch();
                } catch (SQLException e) {
                    DiscordBot.logCatch(e, sc.statement, new Throwable());
                }
            }
            try {
                return sc.statement.executeBatch();
            } catch (SQLException e) {
                DiscordBot.logCatch(e);
                return new int[0];
            } finally {
                sc.closeConnection();
            }
        } else return null;
    }

    public static record executeQueueData(String query, Object... args) {
    }

    public static record bulkData(Object... args) {
    }
}
