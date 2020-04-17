package JavaExtractor.Common;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;

public class MethodContent {
    private final ArrayList<Node> leaves;
    private final String name;
	private int Class_index;

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
}
