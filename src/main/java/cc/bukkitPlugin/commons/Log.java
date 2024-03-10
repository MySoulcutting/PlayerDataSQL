package cc.bukkitPlugin.commons;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.commons.util.StringUtil;

public class Log{

    public enum Level{
        INFO,
        WARN("WordWarn","警告",ChatColor.YELLOW),
        SEVERE("WordError","错误",ChatColor.RED),
        DEBUG("WordDebug","调试",ChatColor.BLUE),
        DEVELOP("WordDebug","调试",ChatColor.BLUE);

        /** 名字key,如果为null,使用{@link Level#name()} */
        public final String mNameKey;
        /** 名字翻译 */
        public String mNameLang="";
        /** 颜色,可以为null,无默认 */
        public final ChatColor mPrefixColor;

        private Level(){
            this("","",(ChatColor)null);
        }

        private Level(String pNameKey,String pDefaultLang,ChatColor pColor){
            this.mNameKey=pNameKey;
            this.mNameLang=pDefaultLang;
            this.mPrefixColor=pColor;
        }

        /**
         * 设置前缀翻译
         * <p>
         * 不翻译颜色字符
         * </p>
         * 
         * @param pLang
         *            翻译
         */
        public void setLang(String pLang){
            if(pLang!=null){
                this.mNameLang=pLang;
            }
        }

        /**
         * 获取前缀翻译
         * 
         * @return
         */
        public String getLang(){
            return this.mNameLang;
        }

        public String getFormat(){
            String tPrefix=name();
            if(this.mNameLang!=null||this.mNameLang.isEmpty()){
                tPrefix=this.mNameLang;
            }
            String tColorStr=this.mPrefixColor==null?"":this.mPrefixColor.toString();
            if(!tPrefix.isEmpty()){
                return tColorStr+'['+tPrefix+']';
            }
            return tColorStr;
        }

    }

    /** 消息输出等级 */
    private static Level mLogLevel=Level.DEBUG;
    /***/
    private static boolean mLogStackTrace=true;
    /** 插件消息前缀 */
    private static String mMsgPrefix="§c[§7TempLog§c]§b";
    /** 所有的颜色字符 */
    private static String COLOR_CHARS="0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    /** 插件消息前缀最后的字体颜色和样式 */
    private static String mMsgPrefixLastStye=Log.getLastChatStyle(Log.mMsgPrefix);

    /**
     * 设置日志等级
     * 
     * @param pLevelStr
     *            等级字符串,如果未找到对应的等级这保持现有等级不变
     */
    public static void setLogLevel(String pLevelStr){
        for(Level sLevel : Level.values()){
            if(sLevel.name().equalsIgnoreCase(pLevelStr)){
                Log.mLogLevel=sLevel;
                return;
            }
        }
    }

    /**
     * 设置日志是否记录堆栈
     */
    public static void logStackTrace(boolean pEnable){
        Log.mLogStackTrace=pEnable;
    }

    /**
     * 查询日志是否记录堆栈
     */
    public static boolean logStackTrace(){
        return Log.mLogStackTrace;
    }

    /**
     * 获取日志等级
     */
    public static Level getLogLevel(){
        return Log.mLogLevel;
    }

    /** 获取聊天消息前缀(已经翻译颜色) */
    public static String getMsgPrefix(){
        return Log.mMsgPrefix;
    }

    /**
     * 设置聊天消息前缀,不允许设置empty
     * <p>
     * 不翻译颜色字符
     * </p>
     * 
     * @param pChatPrefix
     *            消息前缀
     */
    public static void setMsgPrefix(String pChatPrefix){
        if(pChatPrefix==null||pChatPrefix.isEmpty())
            return;

        Log.mMsgPrefix=pChatPrefix;
        Log.mMsgPrefixLastStye=Log.getLastChatStyle(Log.mMsgPrefix);
    }

    /** 获取消息前缀最后的字体颜色与样式 */
    public static String getMsgPrefixStyle(){
        return Log.mMsgPrefixLastStye;
    }

    /**
     * 是否应该输出该等级的消息
     * 
     * @param pLevel
     *            等级
     * @return 是否输出
     */
    public static boolean shouldLog(Level pLevel){
        return pLevel.ordinal()<=Log.getLogLevel().ordinal();
    }

    public static boolean isDebug(){
        return Log.getLogLevel().ordinal()>=Level.DEBUG.ordinal();
    }

    /**
     * 获取该字符串最后的样式
     * <p>
     * 不翻译颜色字符
     * </p>
     * 
     * @param pText
     *            字符串
     * @return 颜色加格式的字符串
     */
    public static String getLastChatStyle(String pText){
        if(StringUtil.isEmpty(pText))
            return "";
        ChatColor tLastColor=null;
        HashSet<ChatColor> tFontStyle=new HashSet<>();
        char[] tContent=pText.toCharArray();
        for(int i=0;i<tContent.length-1;i++){
            if(tContent[i]==ChatColor.COLOR_CHAR){
                ChatColor tColor=ChatColor.getByChar(tContent[i+1]);
                if(tLastColor!=null){
                    if(tColor.isColor()){
                        tLastColor=tColor;
                        tFontStyle.clear();
                    }else if(tColor.isFormat()){
                        tFontStyle.add(tLastColor);
                    }else{
                        tLastColor=null;
                        tFontStyle.clear();
                    }
                    i++;
                }else if(tContent[i+1]=='&'){
                    i++;
                }
            }
        }

        StringBuilder tSB=new StringBuilder();
        if(tLastColor!=null){
            tSB.append(tLastColor.toString());
        }
        for(ChatColor sFormat : tFontStyle){
            tSB.append(sFormat.toString());
        }
        return tSB.toString();
    }

    /**
     * 发送消息给指定玩家
     * <p>
     * 此消息各段消息间的格式不互相影响,全部默认继承自插件前缀样式 不翻译消息颜色字符
     * </p>
     * 
     * @param pSender
     *            发送给谁
     * @param pParts
     *            各段消息
     * @return true
     */
    public static boolean sendPartMsg(CommandSender pSender,String...pParts){
        if(pParts==null||pParts.length==0)
            return true;
        if(pParts.length==1){
            Log.send(pSender,pParts[0]);
        }else{
            String tStyle=ChatColor.RESET.toString()+Log.getMsgPrefixStyle();
            StringBuilder tSB=new StringBuilder(Log.getMsgPrefix());
            for(int i=1;i<pParts.length;i++){
                tSB.append(tStyle).append(pParts[i]);
            }
            Log.send(pSender,tSB.toString());
        }
        return true;
    }

    /**
     * 发送普通消息到Console消息,颜色依据插件前缀设置
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void info(String pMsg){
        Log.send(Bukkit.getConsoleSender(),Level.INFO,pMsg);
    }

    /**
     * 发送普通消息给指定玩家,颜色依据插件前缀设置
     * <p>
     * 如果玩家不是控制台,将同时发送消息到控制台 <br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void info(CommandSender pSender,String pMsg){
        Log.send(pSender,Level.INFO,pMsg);
        if(pSender instanceof Player){
            Log.send(Bukkit.getConsoleSender(),Level.INFO,pMsg);
        }
    }

    /**
     * 发送调试到Console消息,蓝色
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void debug(String pMsg){
        Log.debug(Bukkit.getConsoleSender(),pMsg);
    }

    /**
     * 发送调试到指定玩家,蓝色
     * <p>
     * 如果玩家不是控制台,将同时发送消息到控制台 <br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void debug(CommandSender pSender,String pMsg){
        Log.send(pSender,Level.DEBUG,pMsg);
        if(pSender instanceof Player){
            Log.send(Bukkit.getConsoleSender(),Level.DEBUG,pMsg);
        }
    }

    /**
     * 发送警告到Console消息,黄色
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void warn(String pMsg){
        Log.send(Bukkit.getConsoleSender(),Level.WARN,pMsg);
    }

    /**
     * 发送警告消息到指定玩家,黄色
     * <p>
     * 如果玩家不是控制台,将同时发送消息到控制台<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void warn(CommandSender pSender,String pMsg){
        Log.send(pSender,Level.WARN,pMsg);
        if(pSender instanceof Player){
            Log.send(Bukkit.getConsoleSender(),Level.WARN,pMsg);
        }
    }

    /**
     * 发送警告到Console消息,黄色
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void developInfo(String pMsg){
        Log.send(Bukkit.getConsoleSender(),Level.DEVELOP,pMsg);
    }

    /**
     * 发送警告消息到指定玩家,黄色
     * <p>
     * 如果玩家不是控制台,将同时发送消息到控制台<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void developInfo(CommandSender pSender,String pMsg){
        Log.send(pSender,Level.DEVELOP,pMsg);
        if(pSender instanceof Player){
            Log.send(Bukkit.getConsoleSender(),Level.DEVELOP,pMsg);
        }
    }

    /**
     * 发送错误的Console消息,暗红色消息
     * <p>
     * 如果玩家不是控制台,将同时发送消息到控制台<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void severe(String pMsg){
        Log.send(Bukkit.getConsoleSender(),Level.SEVERE,pMsg);
    }

    /**
     * 发送错误的消息给指定玩家,暗红色消息
     * <p>
     * 如果玩家不是控制台,将同时发送消息到控制台<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     */
    public static void severe(CommandSender pSender,String pMsg){
        Log.send(pSender,Level.SEVERE,pMsg);
        if(pSender instanceof Player){
            Log.send(Bukkit.getConsoleSender(),Level.SEVERE,pMsg);
        }
    }

    /**
     * 输出错误信息到Console
     * <p>
     * 发送错误堆栈的起始错误信息 如果配置文件启用调试模式,会同时发送错误堆栈<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pExp
     *            异常
     */
    public static void severe(Throwable pExp){
        while(pExp.getCause()!=null)
            pExp=pExp.getCause();
        String tMsg=pExp.getClass().getName()+": "+pExp.getMessage();
        if(Log.logStackTrace()){
            tMsg=pExp.getStackTrace()[0].toString().trim()+','+tMsg;
        }
        Log.severe(Bukkit.getConsoleSender(),tMsg,pExp,false);
    }

    /**
     * 输出错误信息到Console
     * <p>
     * 如果配置文件启用调试模式,会同时发送错误堆栈<br>
     * 如果未启用调试模式,将会自动为pMsg加上异常的消息,格式<code>pMsg+": "+pExp.getMessage()</code><br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     * @param pExp
     *            异常
     */
    public static void severe(String pMsg,Throwable pExp){
        Log.severe(Bukkit.getConsoleSender(),pMsg,pExp);
    }

    /**
     * 发送错误信息到指定玩家
     * <p>
     * 如果指定玩家不是控制台,将同时输出消息到控制台<br>
     * 堆栈信息不会发送给玩家<br>
     * 如果配置文件启用调试模式,会同时发送错误堆栈<br>
     * 如果未启用调试模式,消息pMsg后会添加异常的的类型和消息<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     * @param pExp
     *            异常
     */
    public static void severe(CommandSender pSender,String pMsg,Throwable pExp){
        Log.severe(pSender,pMsg,pExp,true);
    }

    /**
     * 发送错误信息到指定玩家
     * <p>
     * 如果指定玩家不是控制台,将同时输出消息到控制台<br>
     * 堆栈信息不会发送给玩家<br>
     * 如果配置文件启用调试模式,会同时发送错误堆栈<br>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pMsg
     *            消息,不需要加前缀
     * @param pExp
     *            异常
     * @param pFixSuffix
     *            是否在未启用调试模式时,在消息pMsg后添加异常的的类型和消息
     */
    public static void severe(CommandSender pSender,String pMsg,Throwable pExp,boolean pFixSuffix){
        if(Log.logStackTrace()){
            Log.send(pSender,Level.SEVERE,pMsg);
            StringWriter tSW=new StringWriter();
            PrintWriter tPW=new PrintWriter(tSW,true);
            pExp.printStackTrace(tPW);
            String[] lines=tSW.getBuffer().toString().split("(\r?\n)+");
            // 堆栈不发送给Player
            pSender=Bukkit.getConsoleSender();
            for(String sLine : lines){
                Log.send(pSender,Level.SEVERE,sLine);
            }
        }else{
            if(pFixSuffix){
                pMsg+=','+pExp.getClass().getName()+": "+pMsg;
            }
            Log.send(pSender,Level.SEVERE,pMsg);
        }
    }

    /**
     * 发送消息到指定玩家
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pSender
     *            发送给谁
     * @param pMsg
     *            消息,不需要加前缀
     * @return true
     */
    public static boolean send(CommandSender pSender,String pMsg){
        return Log.send(pSender,Level.INFO,pMsg);
    }

    /**
     * 向用户发送消息
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pSender
     *            发送给谁
     * @param pMsg
     *            消息,不需要加前缀
     * @param pLogLevel
     *            消息等级
     * @return true
     */
    public static boolean send(CommandSender pSender,Level pLogLevel,String pMsg){
        if(pLogLevel==null){
            pLogLevel=Level.INFO;
        }else if(!Log.shouldLog(pLogLevel)){
            return true;
        }

        String tPrefix=Log.getMsgPrefix();
        String tFormat=pLogLevel.getFormat();
        if(tFormat!=null&&!tFormat.isEmpty())
            tPrefix+=tFormat;
        return send(pSender,tPrefix+" ",pMsg);
    }

    /**
     * 向用户发送消息
     * <p>
     * 不翻译消息颜色字符
     * </p>
     * 
     * @param pSender
     *            目标
     * @param pMsg
     *            消息
     * @param pPrefix
     *            消息前缀
     * @return true
     */
    public static boolean send(CommandSender pSender,String pPrefix,String pMsg){
        if(pMsg==null||pMsg.isEmpty())
            return true;
        if(pSender==null)
            pSender=Bukkit.getConsoleSender();
        if(pPrefix==null||pPrefix.isEmpty()){
            pPrefix=Log.getMsgPrefixStyle();
        }

        for(String sMsg : pMsg.split("\n+")){
            pSender.sendMessage(pPrefix+sMsg);
        }
        return true;
    }

    /**
     * 替换字符中的&+颜色字符为§+颜色字符,&&只会替换成&
     * 
     * @param pText
     *            消息
     * @return 替换后的消息
     */
    public static String color(String pText){
        if(StringUtil.isEmpty(pText))
            return pText;
        int tPoint=0,i=0;
        char[] b=pText.toCharArray();
        for(;i<b.length-1;i++){
            if(b[i]=='&'){
                if(COLOR_CHARS.indexOf(b[i+1])>-1){
                    b[tPoint++]=ChatColor.COLOR_CHAR;
                    b[tPoint++]=Character.toLowerCase(b[i+1]);
                    i++;
                    continue;
                }else if(b[i+1]=='&'){
                    b[tPoint++]='&';
                    i++;
                    continue;
                }
            }
            b[tPoint++]=b[i];
        }
        if(i<b.length){
            b[tPoint++]=b[i];
        }
        return new String(b,0,tPoint);
    }

}
