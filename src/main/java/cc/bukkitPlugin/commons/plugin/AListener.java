package cc.bukkitPlugin.commons.plugin;

import org.bukkit.event.Listener;

public class AListener<T extends ABukkitPlugin<T>> implements Listener{

    protected final T mPlugin;

    public AListener(T pPlugin){
        this.mPlugin=pPlugin;
        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
    }

    public T getPlugin(){
        return this.mPlugin;
    }

}
