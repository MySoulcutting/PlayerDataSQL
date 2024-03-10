package cc.bukkitPlugin.commons.plugin.command;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;


public class TCommandHelp<T extends ABukkitPlugin<T>,E extends TCommandExc<T>>extends TACommandBase<T,E>{

    public TCommandHelp(E pExector){
        super(pExector,"help");
    }

    public TCommandHelp(E pExector,int pMaxArgLength){
        super(pExector,"help",pMaxArgLength);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);
        int page=1;
        if(pArgs.length>1)
            return errorArgsNumber(pSender,pArgs.length);
        if(pArgs.length==1){
            try{
                page=Integer.parseInt(pArgs[0]);
            }catch(NumberFormatException nfexp){
                return errorArg(pSender,pArgs[0],0);
            }
        }
        ArrayList<String> allHelps=this.getHelp(pSender,null);
        if(allHelps.isEmpty())
            return false;
        int page_numb=allHelps.size()%10==0?allHelps.size()/10:allHelps.size()/10+1;
        if(page<1||page>page_numb)
            page=1;
        send(pSender,"================("+page+"/"+page_numb+")===============");
        for(int i=(page-1)*10;(i<(page*10))&&(i<allHelps.size());i++){
            send(pSender,allHelps.get(i));
        }
        return true;
    }

    @Override
    public boolean help(CommandSender pSender,String pLabel){
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> list=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            for(String sLabel : this.mExector.keySet()){
                TACommandBase<T,? extends TCommandExc<T>> cmd=this.mExector.getCommand(sLabel);
                if(cmd==this)
                    continue;
                ArrayList<String> helplist=cmd.getHelp(pSender,sLabel);
                if(helplist==null)
                    throw new IllegalArgumentException("命令模块 "+cmd.getClass().getSimpleName()+" 的帮助结果为null,请联系作者完善代码");
                list.addAll(helplist);
            }
        }
        return list;
    }

}
