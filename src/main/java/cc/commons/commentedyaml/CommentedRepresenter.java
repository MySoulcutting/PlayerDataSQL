package cc.commons.commentedyaml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

public class CommentedRepresenter extends Representer{

    /** 实现Serializable接口的类的类型标记 */
    public static String SerializableMark="===";
    /** 实现MapSerialize接口的类的类型标记 */
    public static String MapSerializeMark="==";

    public CommentedRepresenter(){
        this.multiRepresenters.put(CommentedSection.class,new CommentedSectionRepresent());
        this.multiRepresenters.put(CommentedValue.class,new CommentedValueRepresent());
        this.multiRepresenters.put(Serializable.class,new RepresentSerializable());
    }

    public Represent getNullRepresent(){
        return this.nullRepresenter;
    }

    public Map<Class<?>,Represent> getRepresents(){
        return this.representers;
    }

    public Map<Class<?>,Represent> getMultiRepresents(){
        return this.multiRepresenters;
    }

    public class RepresentSerializable extends RepresentJavaBean{

        @Override
        public Node representData(Object pData){
            MappingNode tNode=(MappingNode)super.representData(pData);
            tNode.setTag(Tag.MAP);

            Represent tStringRepresent=CommentedRepresenter.this.getRepresents().get(String.class);
            List<NodeTuple> tWarpFields=new ArrayList<>();
            tWarpFields.add(new NodeTuple(
                    tStringRepresent.representData(SerializableMark),
                    tStringRepresent.representData(pData.getClass().getName())));
            tWarpFields.addAll(tNode.getValue());
            tNode.setValue(tWarpFields);

            return tNode;
        }
    }

    public class CommentedSectionRepresent extends RepresentMap{

        @Override
        public Node representData(Object pData){
            return super.representData(((CommentedSection)pData).values());
        }
    };

    public class CommentedValueRepresent implements Represent{

        @Override
        public Node representData(Object pData){
            return CommentedRepresenter.this.representData(((CommentedValue)pData).getValue());
        }
    };

}
