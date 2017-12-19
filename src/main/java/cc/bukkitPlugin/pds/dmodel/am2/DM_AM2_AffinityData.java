package cc.bukkitPlugin.pds.dmodel.am2;

import cc.bukkitPlugin.pds.PlayerDataSQL;

public class DM_AM2_AffinityData extends ADM_AM2{

    public DM_AM2_AffinityData(PlayerDataSQL pPlugin){
        super(pPlugin,"am2.playerextensions.AffinityData","AffinityData");
    }

    @Override
    public String getModelId(){
        return "ArsMagica2_Affinity";
    }

    @Override
    public String getDesc(){
        return "魔法艺术2-亲和力";
    }

    @Override
    protected boolean initOnce() throws Exception{
        this.initExProp();
        super.initOnce();

        // AM2 Affinity TAG
        this.mModelTags.add("AffinityDepthData");
        this.mModelTags.add("AffinityLocked");

        return true;
    }

}
