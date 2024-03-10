package cc.bukkitPlugin.pds;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.pds.api.PDSAPI;
import cc.bukkitPlugin.pds.api.event.CallDataModelRegisterEvent;
import cc.bukkitPlugin.pds.command.CommandExc;
import cc.bukkitPlugin.pds.dmodel.ADataModel;
import cc.bukkitPlugin.pds.listener.PlayerListener;
import cc.bukkitPlugin.pds.listener.PreventListener;
import cc.bukkitPlugin.pds.manager.ConfigManager;
import cc.bukkitPlugin.pds.manager.LangManager;
import cc.bukkitPlugin.pds.storage.IStorage;
import cc.bukkitPlugin.pds.storage.MySQL;
import cc.bukkitPlugin.pds.user.UserLockMark;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.bukkitPlugin.pds.util.CPlayer;
import cc.bukkitPlugin.pds.util.PDSNBTUtil;
import cc.commons.util.reflect.ClassUtil;
import lombok.Getter;

public class PlayerDataSQL extends ABukkitPlugin<PlayerDataSQL> {

    /**
     * 踢出玩家,线程安全的
     * 
     * @param pPlayer
     *            玩家名字
     */
    public static void kickPlayerOnError(CPlayer pPlayer) {
        if (Bukkit.isPrimaryThread()) {
            PlayerDataSQL.kickPlayerOnError0(pPlayer);
        } else {
            Bukkit.getScheduler().runTask(getInstance(), () -> PlayerDataSQL.kickPlayerOnError0(pPlayer));
        }
    }

    private static void kickPlayerOnError0(CPlayer pPlayer) {
        if (pPlayer.isOnline()) {
            pPlayer.getPlayer().kickPlayer(PlayerDataSQL.getInstance().C("MsgDataExpection"));
        }
    }

    private UserManager mUserMan;
    private IStorage mStorage;
    @Getter
    private List<File> scripts = new ArrayList();

    private static boolean PRE_UUID_MODE = false;
    private static Function<Player, String> IDGetter = pPlayer -> pPlayer.getName();

    /**
     * 此函数中不要进行模块间的互相调用<br />
     * 调用操作请在reload函数中进行
     */
    public void onEnable() {
        Log.info("§2该版本由白魂(WhiteSoul)魔改");
        Log.info("§b修改内容如下：");
        Log.info("§a精简只剩下1.12.2原版数据");
        Log.info("§a不同步玩家血量数据(修复AP卡血BUG)");
        Log.info("§a增加数据加载时提示玩家");
        Log.info("§c出现问题概不负责！");
        // 强制初始化工具类
        String.valueOf(NMSUtil.mTestAPIVersion).length();
        String.valueOf(PDSNBTUtil.method_NBTCompressedStreamTools_readCompressed).length();
        // 注册管理器
        this.setLangManager(new LangManager(this));
        this.registerManager(this.getLangManager());
        this.setConfigManager(new ConfigManager(this));
        this.registerManager(this.getConfigManager());
        this.registerManager(this.mUserMan = new UserManager(this));
        this.registerManager((MySQL)(this.mStorage = new MySQL(this)));

        new PDSAPI(this);

        // 注册监听器
        new PreventListener(this, this.mUserMan);
        new PlayerListener(this, this.mUserMan);

        // 绑定命令管理器
        new CommandExc(this);

        this.registerDM();
        Bukkit.getPluginManager().callEvent(new CallDataModelRegisterEvent());

        // 初始化管理器并载入配置
        this.reloadPlugin(null);

        PDSAPI.checkModels(true); // 先加载配置,再决定启用哪些配置

        UserLockMark.unlockLoggedUser(this); //先解锁缓存锁定的玩家再添加保存任务
        for (Player sPlayer : BukkitUtil.getOnlinePlayers()) {
            this.mUserMan.createSaveTask(new CPlayer(sPlayer));
        }
        // 等待锁定/保存任务预先执行一次后再保存锁定玩家列表
        Bukkit.getScheduler().runTaskLater(this, ()->UserLockMark.save(), 2);
    }

    @Override
    public void reloadPlugin(CommandSender pSender) {
        File tScriptDir = new File(getDataFolder(), "scripts");
        if (!tScriptDir.exists()) tScriptDir.mkdir();
        scripts.clear();
        File[] tScriptFiles = tScriptDir.listFiles();
        if (tScriptFiles != null)
            for (File sFile : tScriptFiles) {
                scripts.add(sFile);
            }
        super.reloadPlugin(pSender);

        boolean tUUIDModel = this.getConfigManager().getConfig().getBoolean("Plugin.UseUUID");
        if (tUUIDModel != PRE_UUID_MODE) {
            PRE_UUID_MODE = tUUIDModel;
            if (PRE_UUID_MODE) IDGetter = pPlayer -> pPlayer.getUniqueId().toString();
            else IDGetter = pPlayer -> pPlayer.getName().toString();
        }
    }

    @Override
    public void onDisable() {
        for (Player sPlayer : BukkitUtil.getOnlinePlayers()) {
            this.mUserMan.saveUser(new CPlayer(sPlayer), false);
        }

        Iterator<Runnable> tIt = PlayerListener.QUIT_SAVE_TASK.iterator();
        while (tIt.hasNext()) {
            Runnable tTask = tIt.next();
            tIt.remove();
            tTask.run();
        }
        UserLockMark.save();

        super.onDisable();
    }

    private void registerDM() {
        String tPackage = ADataModel.class.getPackage().getName();
        tPackage = tPackage == null ? "cc.bukkitPlugin.pds.dmodel" : tPackage;

        List<Class<?>> tDMClasses = null;
        try {
            tDMClasses = ClassUtil.getPackageClasses(tPackage, true);
        } catch (IOException exp) {
            Log.severe("Error on auto register datamodel", exp);
            return;
        }

        int tRegisteredCount = 0;
        for (Class<?> tClass : tDMClasses) {
            if (tClass.isInterface() || Modifier.isAbstract(tClass.getModifiers()) || !ADataModel.class.isAssignableFrom(tClass))
                continue;

            try {
                ADataModel tModel = ClassUtil.newInstance((Class<? extends ADataModel>)tClass, PlayerDataSQL.class, this);
                PDSAPI.registerModel(tModel);

                if (IConfigModel.class.isAssignableFrom(tClass)) {
                    this.getConfigManager().registerConfigModel((IConfigModel)tModel);
                }

                tRegisteredCount++;
            } catch (Throwable exp) {
                Log.severe("Error on auto register datamodel class: " + tClass.getName(), exp);
            }
        }

        Log.debug("auto registered " + tRegisteredCount + " datamodel");
    }

    @Override
    public ConfigManager getConfigManager() {
        return (ConfigManager)super.getConfigManager();
    }

    @Override
    public LangManager getLangManager() {
        return (LangManager)super.getLangManager();
    }

    public UserManager getUserManager() {
        return this.mUserMan;
    }

    public IStorage getStorage() {
        return this.mStorage;
    }

    public static PlayerDataSQL getInstance() {
        return ABukkitPlugin.getInstance(PlayerDataSQL.class);
    }

    public static String getPlayerID(CPlayer pPlayer) {
        return getPlayerID(pPlayer.getPlayer());
    }

    public static String getPlayerID(Player pPlayer) {
        return IDGetter.apply(pPlayer);
    }

}
