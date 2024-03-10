/*
 * Copyright 2011-2015 喵♂呜. All rights reserved.
 */
package cc.bukkitPlugin.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Yum数据中心 数据统计类
 *
 * @since 2015年12月14日 下午1:36:42
 * @author 喵♂呜
 */
public class Statistics{

    /** 统计基础地址 */
    private static final String BASE_URL="http://api.yumc.pw";
    /** 统计间隔 分钟 */
    private static final int PING_INTERVAL=25;
    /** 统计资源路径 */
    private static final String REPORT_URL="/I/P/S/V/%s/P/%s";
    /** 统计系统版本 */
    private final static int REVISION=9;
    /** 统计插件基础配置文件 */
    private final static File configfile=new File(Bukkit.getUpdateFolderFile().getParentFile(),"PluginHelper"+File.separatorChar+"config.yml");
    /** UTF-8编码 */
    private static final Charset UTF_8=Charset.forName("UTF-8");
    /** 统计插件基础配置文件 */
    private YamlConfiguration config;
    /** 调试模式 */
    private boolean debug;
    /** 唯一服务器编码 */
    private String guid;
    /** 插件实体 */
    private final Plugin plugin;
    /** 线程任务 */
    private volatile BukkitTask task=null;
    /** 统计线程 */
    private volatile StatisticsTimer timer=null;
    /** 是否已经初始化 */
    private boolean mIsInit=false;

    /**
     * @param plugin
     *            插件实体
     * @throws IOException
     *             IO异常
     */
    public Statistics(Plugin plugin){
        if(plugin==null){
            throw new IllegalArgumentException("Plugin Can't NULL!");
        }
        this.plugin=plugin;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数
     * @return 所代表远程资源的响应结果
     */
    public static String postData(final String url,final String param){
        PrintWriter out=null;
        String result="";
        try{
            final URL realUrl=new URL(url);
            // 打开和URL之间的连接
            final URLConnection conn=realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Connection","Keep-Alive");
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
            // 设置超时时间 10秒
            conn.setReadTimeout(10000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out=new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.write(param);
            // flush输出流的缓冲
            out.flush();
            result=streamToString(conn.getInputStream(),UTF_8);
        }catch(final Exception e){
        }finally{
            if(out!=null){
                out.close();
            }
        }
        return result;
    }

    /**
     * 获取数据流转换为字符串
     *
     * @param in
     *            输入流
     * @param cs
     *            字符编码
     * @return 字符串
     */
    public static String streamToString(final InputStream in,final Charset cs){
        final BufferedReader reader=new BufferedReader(new InputStreamReader(in,cs));
        String response="";
        String result="";
        try{
            while((response=reader.readLine())!=null){
                result+=response;
            }
            reader.close();
        }catch(final IOException e){
        }
        return result;
    }

    private static void initFile(final YamlConfiguration config) throws IOException{
        if(config.getString("guid")==null){
            config.options().header("数据中心 http://www.yumc.pw 收集的数据仅用于统计插件使用情况").copyDefaults(true);
            config.set("guid",UUID.randomUUID().toString());
            config.set("debug",false);
            config.save(configfile);
        }
        if(!config.contains("YumAccount")){
            config.set("YumAccount.username","Username Not Set");
            config.set("YumAccount.password","Password NotSet");
            config.save(configfile);
        }
        if(!config.contains("TellrawManualHandle")){
            config.set("TellrawManualHandle",false);
            config.save(configfile);
        }
    }

    /**
     * URL编码 格式UTF-8
     *
     * @param text
     *            the text to encode
     * @return the encoded text, as UTF-8
     */
    private static String urlEncode(final String text) throws UnsupportedEncodingException{
        return URLEncoder.encode(text,"UTF-8");
    }

    /**
     * 简化输出
     *
     * @param msg
     *            输出对象
     */
    public void print(final String msg){
        if(debug){
            System.out.println("[Statistics] "+msg);
        }
    }

    private void initOnce(){
        if(!this.mIsInit){
            try{
                if(!configfile.exists()){
                    configfile.createNewFile();
                }
                config=YamlConfiguration.loadConfiguration(configfile);
                initFile(config);
            }catch(final IOException e){
                config=new YamlConfiguration();
                try{
                    initFile(config);
                }catch(final IOException e1){
                }
            }
            this.guid=config.getString("guid");
            this.debug=config.getBoolean("debug",false);
            this.mIsInit=true;
        }
    }

    /**
     * 开启数据统计 这将会在异步执行
     *
     * @return 是否运行成功.
     */
    public boolean start(){
        if(task!=null){
            return true;
        }else{
            initOnce();
        }
        timer=new StatisticsTimer();
        // 开启TPS统计线程
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,timer,0,20);
        // 开启发送数据线程
        task=plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,new Runnable(){

            @Override
            public void run(){
                try{
                    postPlugin();
                }catch(final Exception|Error e){
                    print(e.getMessage());
                }
            }
        },50,PING_INTERVAL*1200);
        return true;
    }

    public void stop(){
        if(task==null)
            return;
        Bukkit.getServer().getScheduler().cancelTask(this.task.getTaskId());
        task=null;
    }

    /**
     * 设置插件信息收集的状态
     */
    public void setEnable(boolean pEnable){
        if(pEnable){
            if(this.task==null){
                this.start();
                return;
            }
        }else{
            if(this.task!=null){
                this.stop();
                return;
            }
        }
    }

    /**
     * 获得在线玩家人数
     *
     * @return 在线玩家人数
     */
    private int getOnlinePlayers(){
        try{
            final Method onlinePlayerMethod=Server.class.getMethod("getOnlinePlayers");
            if(onlinePlayerMethod.getReturnType().equals(Collection.class)){
                return ((Collection<?>)onlinePlayerMethod.invoke(Bukkit.getServer())).size();
            }
            return ((Player[])onlinePlayerMethod.invoke(Bukkit.getServer())).length;
        }catch(final Exception ex){
            print(ex.getMessage());
        }
        return 0;
    }

    /**
     * 发送服务器数据到统计网页
     */
    private void postPlugin() throws IOException{
        // 服务器数据获取
        final PluginDescriptionFile description=this.plugin.getDescription();
        final boolean onlinemode=Bukkit.getServer().getOnlineMode(); // TRUE if
                                                                     // online
                                                                     // mode is
                                                                     // enabled
        final String pluginname=description.getName();
        final String pluginversion=description.getVersion();
        final String serverversion=Bukkit.getVersion();
        final int serverport=this.plugin.getServer().getPort();
        final double servertps=this.timer.getAverageTPS();
        final int playersonline=getOnlinePlayers();
        final String tmposarch=System.getProperty("os.arch");
        final String osname=System.getProperty("os.name");
        final String osarch=tmposarch.equalsIgnoreCase("amd64")?"x86_64":tmposarch;
        final String osversion=System.getProperty("os.version");
        final int oscores=Runtime.getRuntime().availableProcessors();
        final long osusemem=(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024/1024;
        final String javaversion=System.getProperty("java.version");
        final int authmode=onlinemode?1:0;

        // 实例化模型 写入参数
        final StatisticModule sm=new StatisticModule().setGuid(this.guid).setServer_version(serverversion).setServer_port(serverport).setServer_tps(servertps).setPlugin_version(pluginversion).setPlayers_online(playersonline).setOs_name(osname).setOs_arch(osarch).setOs_version(osversion).setOs_usemem(osusemem).setOs_cores(oscores).setAuth_mode(authmode).setJava_version(javaversion);

        final String jsondata=sm.toJson();

        final String url=BASE_URL+String.format(REPORT_URL,REVISION,urlEncode(pluginname));
        if(this.debug){
            print("Plugin: "+pluginname+" Send Data To CityCraft Data Center");
            print("Address: "+url);
            print("Data: "+jsondata);
        }
        // 发送数据
        final JSONObject result=(JSONObject)JSONValue.parse(postData(url,jsondata));
        if(this.debug){
            print("Plugin: "+pluginname+" Recover Data From CityCraft Data Center: "+result.get("info"));
        }
    }

    public static class StatisticModule{

        public Map<String,Object> info;

        public StatisticModule(){
            this.info=new HashMap<>();
        }

        public StatisticModule setAuth_mode(final int auth_mode){
            this.info.put("auth_mode",auth_mode);
            return this;
        }

        public StatisticModule setGuid(final String guid){
            this.info.put("guid",guid);
            return this;
        }

        public StatisticModule setJava_version(final String java_version){
            this.info.put("java_version",java_version);
            return this;
        }

        public StatisticModule setOs_arch(final String os_arch){
            this.info.put("os_arch",os_arch);
            return this;
        }

        public StatisticModule setOs_cores(final int os_cores){
            this.info.put("os_cores",os_cores);
            return this;
        }

        public StatisticModule setOs_name(final String os_name){
            this.info.put("os_name",os_name);
            return this;
        }

        public StatisticModule setOs_usemem(final long os_usemem){
            this.info.put("os_usemem",os_usemem);
            return this;
        }

        public StatisticModule setOs_version(final String os_version){
            this.info.put("os_version",os_version);
            return this;
        }

        public StatisticModule setPlayers_online(final int players_online){
            this.info.put("players_online",players_online);
            return this;
        }

        public StatisticModule setPlugin_version(final String plugin_version){
            this.info.put("plugin_version",plugin_version);
            return this;
        }

        public StatisticModule setServer_port(final int server_port){
            this.info.put("server_port",server_port);
            return this;
        }

        public StatisticModule setServer_tps(final double server_tps){
            this.info.put("server_tps",server_tps);
            return this;
        }

        public StatisticModule setServer_version(final String server_version){
            this.info.put("server_version",server_version);
            return this;
        }

        public String toJson(){
            return "Info="+JSONValue.toJSONString(info);
        }
    }

    public class StatisticsTimer implements Runnable{

        private final LinkedList<Double> history=new LinkedList<Double>();
        private transient long lastPoll=System.nanoTime();

        public double getAverageTPS(){
            double avg=0.0D;
            for(final Double f : history){
                if(f!=null){
                    avg+=f.doubleValue();
                }
            }
            return avg/history.size();
        }

        @Override
        public void run(){
            final long startTime=System.nanoTime();
            long timeSpent=(startTime-lastPoll)/1000;
            if(timeSpent==0){
                timeSpent=1;
            }
            if(history.size()>10){
                history.remove();
            }
            final double ttps=2.0E7D/timeSpent;
            if(ttps<=21.0D){
                history.add(ttps);
            }
            lastPoll=startTime;
        }
    }
}
