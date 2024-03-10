package cc.bukkitPlugin.pds.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.INeedClose;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.IOUtil;
import cc.commons.util.StringUtil;
import cc.commons.util.ValidData;

public class MySQL extends AManager<PlayerDataSQL> implements IConfigModel, INeedClose, IStorage, Runnable {

    public static String url(String pHost, String pDBName, String pParams) {
        StringBuilder tSBuilder = new StringBuilder("jdbc:mysql://");
        tSBuilder.append(pHost);
        tSBuilder.append('/').append(pDBName);
        if (StringUtil.isNotEmpty(pParams)) {
            tSBuilder.append('?').append(pParams);
        }
        return tSBuilder.toString();
    }

    public static Connection createConn(String pUrl, String pUsername, String pPassword, int pLoginTimeout) throws SQLException {
        return DriverManager.getConnection(pUrl, pUsername, pPassword);
    }

    public final String DRIVER = "com.mysql.jdbc.Driver";

    protected String mDatabase = "PlayerDataSQL";
    protected String mTableName = "PlayerDataSQL";
    protected String mUsername = "root";
    protected String mPassword = "root";
    protected String mDBHost = "localhost:3306";
    protected String mURLParams = "useUnicode=true&characterEncoding=utf8&autoReconnect=true&useAffectedRows=true&useSSL=false";
    protected int mQueryTimeout = 5;
    protected int mNetworkTimeout = 10;
    protected int mLoginTimeout = 5;
    /** 当前配置的Hash值 */
    protected int mCfgHash = 0;
    protected ReentrantLock mLock = new ReentrantLock();

    protected Connection mConn = null;
    protected ConcurrentHashMap<String, PreparedStatement> mStatementCache = new ConcurrentHashMap<>();

    public MySQL(PlayerDataSQL pPlugin) {
        super(pPlugin);

        this.mPlugin.getConfigManager().registerConfigModel(this);
        this.mPlugin.registerCloseModel(this);

        new Thread(this).start();
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig) {
        CommentedSection tSecMain = pConfig.getOrCreateSection("MySQL", "MySql数据库配置");
        tSecMain.addDefault("DBHost", this.mDBHost, "数据库地址,包括端口");
        tSecMain.addDefault("Database", this.mDatabase, "数据库名");
        tSecMain.addDefault("URLParams", this.mURLParams, "数据库链接参数");
        tSecMain.addDefault("TableName", this.mTableName, "数据表名");
        tSecMain.addDefault("Username", this.mUsername, "用户名");
        tSecMain.addDefault("Password", this.mPassword, "密码");
    }

    @Override
    public void setConfig(CommandSender pSender, CommentedYamlConfig pConfig) {
        CommentedSection tSecMain = pConfig.getOrCreateSection("MySQL");

        this.mLoginTimeout = tSecMain.getInt("Timeout.Login", this.mLoginTimeout);
        this.mNetworkTimeout = Math.max(5, tSecMain.getInt("Timeout.Network", this.mNetworkTimeout));
        this.mQueryTimeout = tSecMain.getInt("Timeout.Query", this.mQueryTimeout);

        StringBuilder tSBuilder = new StringBuilder();
        tSBuilder.append(this.mDBHost = tSecMain.getString("DBHost", this.mDBHost)).append('\0');
        tSBuilder.append(this.mDatabase = tSecMain.getString("Database", this.mDatabase)).append('\0');
        tSBuilder.append(this.mURLParams = tSecMain.getString("URLParams", this.mURLParams)).append('\0');
        tSBuilder.append(this.mUsername = tSecMain.getString("Username", this.mUsername)).append('\0');
        tSBuilder.append(this.mPassword = tSecMain.getString("Password", this.mPassword)).append('\0');
        this.mTableName = tSecMain.getString("TableName", this.mTableName);

        int tNewCfghash = tSBuilder.toString().hashCode();
        boolean tTestConn = this.mConn == null;
        if (this.mCfgHash != tNewCfghash) {
            tTestConn = true;
            this.mCfgHash = tNewCfghash;
            this.handleConnClose();
        }
        if (tTestConn) {
            Bukkit.getScheduler().runTask(this.mPlugin, () -> {
                try {
                    this.getConn();
                    Log.info(this.mPlugin.C("MsgSuccessConnectToDB"));
                } catch (SQLException exp) {
                    Log.severe(this.mPlugin.C("MsgUnableConnectToDB"),exp);
                }
            });
        }
    }

    @Override
    public void disable() {
        this.handleConnClose();
    }

    protected void handleConnClose() {
        this.mLock.lock();
        try {
            IOUtil.closeStream(this.mConn);
            this.mConn = null;
            this.mStatementCache.clear();
        } finally {
            this.mLock.unlock();
        }
    }

    /**
     * 获取数据库连接
     * 
     * @return 数据库连接
     * @throws SQLException
     *             连接到数据库时发生异常
     */
    public Connection getConn() throws SQLException {
        Connection tConn = null;
        Statement tStat = null;

        this.mLock.lock();
        try {
            if (this.mConn == null) {
                try {
                    tConn = createConn(url(this.mDBHost, "mysql", this.mURLParams), this.mUsername, this.mPassword, this.mLoginTimeout);
                    tStat = tConn.createStatement();
                    tStat.executeUpdate("CREATE DATABASE IF NOT EXISTS " + this.mDatabase + " CHARACTER SET UTF8");
                } catch (SQLException e) {

                } finally {
                    IOUtil.closeStream(tConn, tStat);
                    tConn = null;
                    tStat = null;
                }
            }

            boolean tConnClosed = false;
            try {
                tConnClosed = this.mConn == null || this.mConn.isClosed();
            } catch (SQLException exp) {
                tConnClosed = true;
            }
            if (tConnClosed) {
                this.mStatementCache.clear();
                try {
                    this.mConn = createConn(url(this.mDBHost, this.mDatabase, this.mURLParams), this.mUsername, this.mPassword, this.mLoginTimeout);
                    this.mConn.setAutoCommit(true);
                    tStat = this.mConn.createStatement();
                    tStat.execute("CREATE TABLE IF NOT EXISTS " + this.mTableName + "("
                            + User.COL_NAME + " varchar(64) primary key,"
                            + User.COL_LOCK + " tinyint(1),"
                            + User.COL_DATA + " LONGBLOB"
                            + ")");
                    // 强制更改data字段类型
                    tStat.execute("ALTER TABLE " + this.mTableName + " MODIFY COLUMN " + User.COL_DATA + " LONGBLOB;");
                    if (this.mNetworkTimeout > 0) {
                        try {
                            this.mConn.setNetworkTimeout((callback) -> callback.run(), this.mNetworkTimeout * 1000);
                        } catch (SQLFeatureNotSupportedException | AbstractMethodError exp) {
                            Log.info(ChatColor.YELLOW + "此数据库驱动不支持设置网络超时");
                        }
                    }
                } finally {
                    IOUtil.closeStream(tStat);
                    tStat = null;
                }
            }
        } finally {
            this.mLock.unlock();
        }
        return this.mConn;
    }

    protected PreparedStatement getOrCreate(Connection pConn, String pSQL) throws SQLException {
        PreparedStatement tStatement = this.mStatementCache.get(pSQL);
        if (tStatement == null || tStatement.isClosed()) {
            tStatement = pConn.prepareStatement(pSQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            tStatement.setQueryTimeout(5);
            this.mStatementCache.put(pSQL, tStatement);
        }
        return tStatement;
    }

    @Override
    public User get(CPlayer pPlayer) throws SQLException {
        ResultSet tResult = null;
        User tUser = null;
        this.mLock.lock();

        try {
            PreparedStatement tStatement = this.getOrCreate(this.getConn(), "SELECT * FROM " + this.mTableName + " WHERE " + User.COL_NAME + "=? LIMIT 1");
            tStatement.setString(1, pPlayer.getUUIDOrName());
            tResult = tStatement.executeQuery();
            if (tResult.first()) {
                tUser = new User(pPlayer);
                tUser.mLocked = tResult.getBoolean(User.COL_LOCK);
                tUser.setData(tResult.getBytes(User.COL_DATA));
            }
        } finally {
            try {
                IOUtil.closeStream(tResult);
                Log.developInfo("Read user " + pPlayer.getName() + " at time " + System.nanoTime() + ",hasdata=" + (tUser != null));
            } finally {
                this.mLock.unlock();
            }
        }
        return tUser;
    }

    @Override
    public ArrayList<User> getall() throws SQLException {
        ResultSet tResult = null;
        HashSet<String> tOnlinePlayers = new HashSet();
        for (Player p : Bukkit.getOnlinePlayers()) {
            tOnlinePlayers.add(p.getName().toLowerCase());
        }
        ArrayList<User> tUsers = new ArrayList();
        this.mLock.lock();
        try {
            PreparedStatement tStatement = this.getOrCreate(this.getConn(), "SELECT * FROM " + this.mTableName + " WHERE " + User.COL_LOCK + "=?");
            tStatement.setBoolean(1, false);
            tResult = tStatement.executeQuery();

            while (tResult.next()) {
                User tUser = new User(CPlayer.fromNameOrUUID(tResult.getString(User.COL_NAME)));
                if (tOnlinePlayers.contains(tUser.getOwnerName().toLowerCase())) {
                    continue;
                }
                tUser.mLocked = tResult.getBoolean(User.COL_LOCK);
                tUser.setData(tResult.getBytes(User.COL_DATA));
                tUsers.add(tUser);
            }
        } finally {
            try {
                IOUtil.closeStream(tResult);
                Log.developInfo("Read all users " + " at time " + System.nanoTime());
            } finally {
                this.mLock.unlock();
            }
        }
        return tUsers;
    }

    @Override
    public boolean update(CPlayer pPlayer, User pUser) throws SQLException {
        this.mLock.lock();
        try {
            PreparedStatement tStatement = this.getOrCreate(this.getConn(), "REPLACE INTO "
                    + this.mTableName + " (" + User.COL_NAME + "," + User.COL_LOCK + "," + User.COL_DATA + ") "
                    + "VALUES (?,?,?)");
            tStatement.setString(1, pPlayer.getUUIDOrName());
            tStatement.setBoolean(2, pUser.isLocked());
            tStatement.setBytes(3, pUser.getData());
            return tStatement.executeUpdate() != 0;
        } finally {
            this.mLock.unlock();
            Log.developInfo("Update user " + pUser.getOwnerName() + " at time " + System.nanoTime());
        }
    }

    @Override
    public boolean update(CPlayer pPlayer, String[] pCols, Object...pValues) throws SQLException {
        ValidData.valid(pCols.length != 0 && pCols.length == pValues.length, "SQL 参数错误,参数数量为0或不相等");

        StringBuilder tSBuilder = new StringBuilder();
        tSBuilder.append("UPDATE " + this.mTableName + " SET ");
        for (int i = 0; i < pCols.length; i++) {
            tSBuilder.append(pCols[i]).append('=').append('?').append(',');
        }

        tSBuilder.deleteCharAt(tSBuilder.length() - 1);
        tSBuilder.append(" WHERE ").append(User.COL_NAME).append('=').append('?');
        this.mLock.lock();
        try {
            PreparedStatement tStatement = this.getOrCreate(this.getConn(), tSBuilder.toString());
            tStatement.setString(pCols.length + 1, pPlayer.getUUIDOrName());

            for (int i = 0; i < pCols.length; i++) {
                tStatement.setObject(i + 1, pValues[i]);
            }

            return tStatement.executeUpdate() != 0;
        } finally {
            this.mLock.unlock();
        }
    }

    private int mCheckSecs = 0;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exp) {
                Log.severe(exp);
            }

            if (!this.mPlugin.isEnabled()) break;
            this.mLock.lock();
            try {
                if (this.mConn == null) continue;

                this.mCheckSecs++;
                if (this.mCheckSecs >= 5) {
                    this.mCheckSecs = 0;
                    try {
                        this.mConn.isValid(5);
                    } catch (SQLException exp) {
                        Log.severe(exp);
                    }
                }
            } finally {
                this.mLock.unlock();
            }
        }
    }

    @Override
    public int update(String pSQL) throws SQLException {
        pSQL = pSQL.replace("%table_name%", this.mTableName);

        this.mLock.lock();
        try {
            PreparedStatement tStatement = this.getOrCreate(this.getConn(), pSQL);
            return tStatement.executeUpdate();
        } finally {
            this.mLock.unlock();
            Log.developInfo("invoke SQL (" + pSQL + ")");
        }
    }

}
