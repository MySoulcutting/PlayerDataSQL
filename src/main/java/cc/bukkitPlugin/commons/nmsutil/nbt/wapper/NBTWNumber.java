package cc.bukkitPlugin.commons.nmsutil.nbt.wapper;

public abstract class NBTWNumber<T>extends NBTWBase<T>{

    protected NBTWNumber(){}

    public NBTWNumber(Object pRawValue){
        super(pRawValue);
    }

    public byte byteValue(){
        return this.getNumber().byteValue();
    }

    public short shortValue(){
        return this.getNumber().shortValue();
    }

    public int intValue(){
        return this.getNumber().intValue();
    }

    public long longValue(){
        return this.getNumber().longValue();
    }

    public float floatValue(){
        return this.getNumber().floatValue();
    }

    public double doubleValue(){
        return this.getNumber().doubleValue();
    }

    public abstract Number getNumber();

}
