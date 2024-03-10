package cc.bukkitPlugin.commons.plugin.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.commons.util.StringUtil;

public abstract class TACommandBase<T extends ABukkitPlugin<T>,E extends TCommandExc<T>>{

    /** 命令执行器 */
    protected E mExector;
    /** 主插件 */
    protected T mPlugin;
    /** 主命令,包括斜杠 */
    protected String mMainCmdLabel;
    /** 插件名,用于权限节点的生成 */
    protected String mPluginName;
    /** 命令的标签,首字母小写 */
    protected String mCmdLabel;
    /** 命令最大参数长度 */
    protected int mMaxArgLength=0;
    /** 命令名字映射,默认包含自带命令名字的小写 */
    protected final HashSet<String> mCmdAlias=new HashSet<>();
    /** 一级子命令集合,有序,建议小写,空表示不存在一级子命令 */
    protected final TreeSet<String> mSubCmd=new TreeSet<>();
    /**
     * 命令权限的头部分 结构 插件名.cmd.命令名小写
     */
    protected String mPermissionHead;
    /** 最后一次构造的权限 */
    protected String mLastConstructPermisson="";

    public static String getLabelFromClass(Class<?> pClazz){
        String tCmdLabel=pClazz.getSimpleName();
        if(tCmdLabel.toLowerCase().startsWith("command")&&tCmdLabel.length()>7){
            tCmdLabel=tCmdLabel.substring(7);
        }else if(tCmdLabel.toLowerCase().startsWith("tcommand")&&tCmdLabel.length()>8){
            tCmdLabel=tCmdLabel.substring(8);
        }
        return tCmdLabel;
    }

    /**
     * 构造一个命令,自动生成命令标签,最大参数长度为0
     * 
     * @param pExector
     *            命令执行器
     */
    public TACommandBase(E pExector){
        this(pExector,null,0);
    }

    /**
     * 构造一个命令,最大参数长度为0
     * 
     * @param pExector
     *            命令执行器
     * @param pCmdLabel
     *            此命令的标签,可以null,null时将取命令类名去掉Command部分的字符串
     */
    public TACommandBase(E pExector,String pCmdLabel){
        this(pExector,pCmdLabel,0);
    }

    /**
     * 构造一个命令,自动生成命令标签
     * 
     * @param pExector
     *            命令执行器
     * @param pMaxArgLength
     *            命令最大参数长度
     */
    public TACommandBase(E pExector,int pMaxArgLength){
        this(pExector,null,pMaxArgLength);
    }

    /**
     * 构造一个命令
     * 
     * @param pExector
     *            命令执行器
     * @param pCmdLabel
     *            此命令的标签
     * @param pMaxArgLength
     *            命令最大参数长度
     */
    public TACommandBase(E pExector,String pCmdLabel,int pMaxArgLength){
        this.mExector=pExector;
        this.mPlugin=pExector.getPlugin();
        this.mMainCmdLabel=pExector.getCmdLabel();
        this.mPluginName=this.mPlugin.getName();
        if(pCmdLabel==null){
            this.mCmdLabel=TACommandBase.getLabelFromClass(this.getClass());
        }else{
            this.mCmdLabel=pCmdLabel;
        }
        this.mCmdLabel=StringUtil.lowerFirst(this.mCmdLabel);
        this.mMaxArgLength=pMaxArgLength;
        this.mPermissionHead=new StringBuilder(pExector.getMainLabel()).append('.').append("cmd").append('.').append(this.mCmdLabel.toLowerCase()).toString();
        this.mCmdAlias.add(this.mCmdLabel.toLowerCase());
    }

    /**
     * 给玩家发送一条消息
     * 
     * @return true
     */
    public boolean send(CommandSender pSender,String pMsg){
        Log.send(pSender,pMsg);
        return true;
    }

    /**
     * 给玩家发送多条消息
     * 
     * @return true
     */
    public boolean send(CommandSender pSender,List<String> pMsgs){
        for(String sMsg : pMsgs)
            Log.send(pSender,sMsg);
        return true;
    }

    /** 翻译一个语言节点 */
    public String C(String pNode){
        return this.mPlugin.C(pNode);
    }

    /** 翻译一个语言节点 */
    public String C(String pNode,String pPlaceHolder,Object pParam){
        return this.mPlugin.C(pNode,pPlaceHolder,pParam);
    }

    /** 翻译一个语言节点 */
    public String C(String pNode,String[] pPlaceHolders,Object...pParams){
        return this.mPlugin.C(pNode,pPlaceHolders,pParams);
    }

    /**
     * 发送该命令的帮助到指定的用户
     * 
     * @param pSender
     *            需要获取帮助的用户
     * @param pLabel
     *            要获取帮助的命令标签映射
     * @return 永远为true
     */
    public boolean help(CommandSender pSender,String pLabel){
        ArrayList<String> chelp=getHelp(pSender,pLabel);
        if(chelp==null||chelp.isEmpty()){
            return false;
        }else for(String ccc : chelp){
            send(pSender,ccc);
        }
        return true;
    }

    /**
     * 如果命令无法处理其子命令时调用,命令类名字的格式必须是Command***
     * <p>
     * 调用此函数的命令需要支持命令的子命令help<br>
     * 最好在子命令条数大于2条或者帮助行数>=4行数调用此函数
     * </p>
     * 
     * @param pSender
     *            命令发送者
     * @param pLabel
     *            执行该命令时的标签
     * @param pUnknowLabel
     *            未知的子命令标签
     * @return true
     */
    public boolean unknowChildCommand(CommandSender pSender,String pLabel,String pUnknowLabel){
        ArrayList<String> helpList=this.getHelp(pSender,pLabel);
        if(helpList==null||helpList.isEmpty())
            return send(pSender,this.C("MsgUnknowCommand"));
        if(pUnknowLabel==null)
            pUnknowLabel="";
        else pLabel="["+pUnknowLabel+"]";
        return send(pSender,C("MsgUnknowChildCommand")+pLabel+","+C("WordInput")+this.mExector.getCmdLabel()+" "+this.getCommandLabel()+" help "+C("MsgGetHelp"));
    }

    @Deprecated
    public boolean noPermission(CommandSender pSender){
        return this.send(pSender,this.C("MsgNoPermitDoThisCommand"));
    }

    public boolean noPermission(CommandSender pSender,String pPermission){
        return this.send(pSender,this.C("MsgLackPermitDoThisCommand").replace("%permission%",pPermission));
    }

    public boolean consoleNotAllow(CommandSender pSender){
        return this.send(pSender,this.C("MsgConsoleNotAllow"));
    }

    /**
     * 发送参数数量错误消息
     * 
     * @param pSender
     *            命令发起者
     * @param pNumber
     *            错误的参数个数,会自动添加命令和主命令的数量2
     * @return true
     */
    public boolean errorArgsNumber(CommandSender pSender,int pNumber){
        return this.send(pSender,this.C("MsgErrorArgsNumber")+"("+(pNumber+2)+")");
    }

    /**
     * 发送参数错误消息
     * 
     * @param pSender
     *            命令发起者
     * @param pArg
     *            错误的参数
     * @param pIndex
     *            参数所在位置,会自动添加命令和主命令的偏移量3
     * @return true
     */
    public boolean errorArg(CommandSender pSender,String pArg,int pIndex){
        Log.sendPartMsg(pSender,C("MsgErrorArg")," &c"+pArg+" ",C("MsgAtPosition")+(pIndex+3));
        return true;
    }

    /**
     * 获取该命令的标签
     */
    public String getCommandLabel(){
        return this.mCmdLabel;
    }

    /**
     * 获取插件命令映射的拷贝
     * 
     * @return 非null
     */
    public HashSet<String> getCommandLabelAlias(){
        return this.mCmdAlias;
    }

    /**
     * 根据参数以及请求的Tab标签,获取Tab补全的全部结果
     * <p>
     * Tab结果无需进行Tab的适配,但可以进行权限检查<br>
     * 默认为集合为{@link #getSubCmd(CommandSender, String)},请确保不为null<br>
     * </p>
     * 
     * @param pSender
     *            发起请求的玩家
     * @param pLabel
     *            发起请求的命令标签
     * @param pArgs
     *            玩家Tab的文字参数
     */
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        return new ArrayList<>(this.getSubCmd(pSender,pLabel));
    }

    /**
     * 在按下Tab时,完成子命令提示
     * <p>
     * 此函数默认只处理子命令参数为1位的情况,如需处理多位,请重写方法
     * </p>
     * 
     * @param pSender
     *            Tab请求用户
     * @param pArgs
     *            已有的参数
     */
    public ArrayList<String> onTabComplete(CommandSender pSender,String[] pArgs){
        if(pArgs==null)
            return new ArrayList<>(0);

        if(!pSender.hasPermission(this.mExector.getMainLabel()+".cmdcomplete."+this.mCmdLabel))
            return new ArrayList<>(0);

        ArrayList<String> subLabels=this.getTabSubCmd(pSender,null,pArgs);
        String lastParam=pArgs[pArgs.length-1];
        if(StringUtil.isEmpty(lastParam))
            return subLabels;
        ArrayList<String> findTabs=new ArrayList<>();
        Collection<String> findLabels=StringUtil.getSamePrefixIgnoreCase(subLabels,lastParam,true);
        if(findLabels.size()==0){
            String findLabel=StringUtil.getIgnoreCase(subLabels,lastParam);
            if(findLabel!=null){
                if(this.getMaxArgumentLength(null)>pArgs.length)
                    findTabs.add(findLabel+" ");
                else findTabs.add(findLabel);
            }
        }else if(findLabels.size()==1){
            String findLabel=findLabels.toArray()[0].toString();
            if(this.getMaxArgumentLength(null)>pArgs.length)
                findTabs.add(findLabel+" ");
            else findTabs.add(findLabel);
        }else findTabs.addAll(findLabels);
        return findTabs;
    }

    /**
     * 获取除去子命令自身的参数最大长度
     * 
     * @param pLabel
     *            发起请求的命令标签,可以为null
     * @return >=0
     */
    public int getMaxArgumentLength(String pLabel){
        return this.mMaxArgLength;
    }

    /**
     * 检查玩家是否有{@link #mPluginName}.cmd.{@link #mCmdLabel}.Tail1.Tail2..的权限
     * <p>
     * 注意命令自己的权限字符串统一小写,子级权限字符串大小写不变
     * </p>
     * 
     * @param pSender
     *            命令发送者
     * @param pCmdTails
     *            子权限
     * @return 是否有权限
     */
    public boolean hasCmdPermission(CommandSender pSender,String...pCmdTails){
        StringBuilder tPermission=new StringBuilder(this.mPermissionHead);
        if(pCmdTails!=null&&pCmdTails.length>0){
            for(String sCmdTail : pCmdTails){
                tPermission.append('.').append(sCmdTail);
            }
        }
        this.mLastConstructPermisson=tPermission.toString();
        return pSender.hasPermission(this.mLastConstructPermisson);
    }

    /**
     * 构造命令用法
     * <p>
     * 构造的命令用法以{@link TCommandExc#mCmdUsagePrefix}开头<br>
     * 然后追加主命令标签,空格+命令标签,最后追加空格+各个子命令标签
     * </p>
     * 
     * @param tCmd
     *            命令
     * @param pCmdTails
     *            子命令
     * @return 构造后的命令用法
     */
    public String constructCmdUsage(String...pCmdTails){
        StringBuilder tDesc=new StringBuilder(this.mExector.mCmdUsagePrefix).append(this.mMainCmdLabel).append(' ').append(this.mCmdLabel);
        if(pCmdTails!=null&&pCmdTails.length>0){
            for(String sCmdTail : pCmdTails){
                tDesc.append(' ').append(sCmdTail);
            }
        }
        return tDesc.toString();
    }

    /**
     * 构造命令使用描述
     * <p>
     * 先构造命令描述的翻译key 翻译key以Help开头,再追加命令标签,再追加各个首字母大写的子命令标签
     * 然后将key翻译成对应的语言,最后再在翻译的语言头部添加{@link TCommandExc#mCmdDescPrefix}
     * </p>
     * 
     * @param tCmd
     *            命令
     * @param pCmdTail
     *            子命令
     * @return 构造并翻译的命令描述
     */
    public String constructCmdDesc(String...pCmdTails){
        StringBuilder tDesc=new StringBuilder("Help").append(StringUtil.upperFirst(this.mCmdLabel));
        if(pCmdTails!=null&&pCmdTails.length>0){
            for(String sCmdTail : pCmdTails){
                tDesc.append(StringUtil.upperFirst(sCmdTail.trim()));
            }
        }
        return this.mExector.mCmdDescPrefix+this.mPlugin.C(tDesc.toString());
    }

    /**
     * 获取该命令的帮助的文本
     * 
     * @param pSender
     *            获取命令的玩家
     * @param pLabel
     *            要获取帮助的命令的标签,默认不使用
     * @return 帮助文本的合集,如果没有帮助指令,集合为Empty
     */
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> tHelps;
        Collection<String> tSubCmds=this.getSubCmd(pSender,pLabel);
        if(tSubCmds==null||tSubCmds.isEmpty()){
            tHelps=new ArrayList<>(2);
            if(hasCmdPermission(pSender)&&this.preSubCmdHelpWrite(pSender,pLabel,tHelps,null)){
                tHelps.add(this.constructCmdUsage());
                tHelps.add(this.constructCmdDesc());
                this.postSubCmdHelpWrite(pSender,pLabel,tHelps,null);
            }
        }else{
            tHelps=new ArrayList<>(2*tSubCmds.size());
            for(String sSubCmd : tSubCmds){
                if(hasCmdPermission(pSender,sSubCmd)&&this.preSubCmdHelpWrite(pSender,pLabel,tHelps,sSubCmd)){
                    tHelps.add(this.constructCmdUsage(sSubCmd));
                    tHelps.add(this.constructCmdDesc(sSubCmd));
                    this.postSubCmdHelpWrite(pSender,pLabel,tHelps,sSubCmd);
                }
            }
        }

        return tHelps;
    }

    /**
     * 在getHelp中,子命令pSubCmd的帮助被写入钱调用,如果返回false,将不为该标签写入帮助
     * <p>
     * 子命令的使用权限已经检查
     * </p>
     * 
     * @param pSender
     *            帮助请求玩家
     * @param pLabel
     *            要获取帮助的命令的标签
     * @param pHelps
     *            已经写入的帮助文本
     * @param pSubCmd
     *            此时的子命令标签,null表示此命令的帮助,通常是该命令无子命令
     */
    protected boolean preSubCmdHelpWrite(CommandSender pSender,String pLabel,List<String> pHelps,String pSubCmd){
        return true;
    }

    /**
     * 在getHelp中,子命令pSubCmd的帮助被写入后调用
     * <p>
     * 子命令的使用权限已经检查
     * </p>
     * 
     * @param pSender
     *            帮助请求玩家
     * @param pLabel
     *            要获取帮助的命令的标签
     * @param pHelps
     *            已经写入的帮助文本
     * @param pSubCmd
     *            此时的子命令标签,null表示此命令的帮助,通常是该命令无子命令
     */
    protected void postSubCmdHelpWrite(CommandSender pSender,String pLabel,List<String> pHelps,String pSubCmd){

    }

    /**
     * 获取此命令的第一级子命令的集合,可以为null
     * <p>
     * 强烈要求对子命令集合进行权限检查<br>
     * 命令结构: /主命令 此命令标签 一级子命令
     * </p>
     * 
     * @param pSender
     *            发起请求的玩家
     * @param pLabel
     *            发起请求的命令标签,默认不使用
     * @return 一级命令的集合
     */
    public Collection<String> getSubCmd(CommandSender pSender,String pLabel){
        return this.mSubCmd;
    }

    /**
     * 执行该命令
     * 
     * @param pSender
     *            命令的执行者
     * @param pLabel
     *            执行该命令时的标签
     * @param pArgs
     *            参数
     * @return 执行成功与否(默认为true)
     */
    public abstract boolean execute(CommandSender pSender,String pLabel,String[] pArgs);

}
