package JavaExtractor.Common;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MethodContent {
    private final ArrayList<Node> leaves;
    private final String name;
	private int Class_index;
	private String Caption;
	private Map<String, String> Variable_list = new HashMap<String, String>();

    public MethodContent(ArrayList<Node> leaves, String name, int Class_index) {
        this.leaves = leaves;
        this.name = name;
		this.Class_index = Class_index;
    }

    public ArrayList<Node> getLeaves() {
        return leaves;
    }

    public String getName() {
        return name;
    }
    
	public int getClassId() {
		return Class_index;
	}
	
	public void setCaption(String caption) {
		this.Caption = caption;
	}
	
	public String getCaption() {
		return Caption;
	}
	
	public void setVariables(ArrayList<String> varNames, ArrayList<String> varTypes) {
		for (int i = 0; i < varNames.size(); i++) {
			Variable_list.put(varNames.get(i), varTypes.get(i));
		}
	}
	
	public Map<String, String> getVariables() {
		return Variable_list;
	}
}
