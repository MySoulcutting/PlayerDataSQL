package cc.commons.commentedyaml.serialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by xjboss on 2017/5/29.
 */
public class SerializableYamlObject implements Serializable {
    public final HashMap<String, ArrayList<String>> getComments() {
        return comments;
    }

    final transient HashMap<String,ArrayList<String>> comments=new HashMap<>();
    public final ArrayList<String> getCommentInfo(String... tag){
        StringBuilder p1=new StringBuilder();
        for(String t:tag){
            p1.append(t.replace(".","\\.")).append(".");
        }
        return comments.get(p1.deleteCharAt(p1.length()-1).toString());
    }
    public final ArrayList<String> get(ArrayList<String> paths){
        StringBuilder p1=new StringBuilder();
        paths.forEach((p)->p1.append(p.replace(".","\\.")).append("."));
        return this.comments.get(p1.deleteCharAt(p1.length()-1).toString());
    }
    public final void add(ArrayList<String> paths,ArrayList<String> comments){
        StringBuilder p1=new StringBuilder();
        paths.forEach((p)->p1.append(p.replace(".","\\.")).append("."));
        p1.deleteCharAt(p1.length()-1);
        this.comments.put(p1.toString(),comments);
    }
}
