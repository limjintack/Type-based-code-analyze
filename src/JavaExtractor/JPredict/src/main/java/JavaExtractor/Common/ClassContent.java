package JavaExtractor.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.Node;

public class ClassContent {

	private Map<String, String> c_Field_Types = new HashMap<String, String>();
	private ArrayList<Map> c_Block_Types = new ArrayList<>();

	public ClassContent(Map<String, String> field_Types) {
		this.c_Field_Types = field_Types;
	}
	
	public void AddBlock(Map block_type) {
		c_Block_Types.add(block_type);
	}

	public Map<String, String> getField() {
		return c_Field_Types;
	}
	
	public ArrayList<Map> getBlock() {
		return c_Block_Types;
	}

}
