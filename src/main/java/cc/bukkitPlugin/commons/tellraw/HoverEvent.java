package cc.bukkitPlugin.commons.tellraw;

public class HoverEvent implements Cloneable{

    public static enum Action{
        show_achievement,
        show_item,
        show_text
    }

    public final HoverEvent.Action mAction;
    public final String mValue;

    public HoverEvent(HoverEvent.Action pAction,String pValue){
        this.mAction=pAction;
        this.mValue=pValue;
    }

    @Override
    public HoverEvent clone(){
        HoverEvent tEvent=new HoverEvent(this.mAction,this.mValue);
        return tEvent;
    }
}
