package cc.bukkitPlugin.commons.tellraw;

public class ClickEvent implements Cloneable{

    public static enum Action{
        open_url,
        run_command,
        suggest_command,
    }

    public final ClickEvent.Action mAction;
    public final String mValue;

    public ClickEvent(ClickEvent.Action pAction,String pValue){
        this.mAction=pAction;
        this.mValue=pValue;
    }

    @Override
    public ClickEvent clone(){
        ClickEvent tEvent=new ClickEvent(this.mAction,this.mValue);
        return tEvent;
    }
}
