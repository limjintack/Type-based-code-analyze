package JavaExtractor;

import JavaExtractor.Common.ClassContent;
import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.Common.MethodContent;
import JavaExtractor.FeaturesEntities.ProgramFeatures;
import JavaExtractor.FeaturesEntities.Property;
import JavaExtractor.Visitors.FunctionVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONObject;

@SuppressWarnings("StringEquality")
class FeatureExtractor {
    private final static String upSymbol = "|";
    private final static String downSymbol = "|";
    private final static String typeSym = "|";
    //private final static String typeSym = "&";
    private static final Set<String> s_ParentTypeToAddChildId = Stream
            .of("AssignExpr", "ArrayAccessExpr", "FieldAccessExpr", "MethodCallExpr")
            .collect(Collectors.toCollection(HashSet::new));
    private final CommandLineValues m_CommandLineValues;

	ArrayList<String> varNames = new ArrayList<String>();
	ArrayList<String> varTypes = new ArrayList<String>();

	private Map<String, String> Field_Types = new HashMap<String, String>();
	private Map<Integer, Map> Block_Types = new HashMap<Integer, Map>();
	
	private ArrayList<ClassContent> Class_Types = new ArrayList<>();
	
	
    public FeatureExtractor(CommandLineValues commandLineValues) {
        this.m_CommandLineValues = commandLineValues;
    }

    private static ArrayList<Node> getTreeStack(Node node) {
        ArrayList<Node> upStack = new ArrayList<>();
        Node current = node;
        while (current != null) {
            upStack.add(current);
            current = current.getParentNode();
        }
        return upStack;
    }

    
    public ProgramFeatures extractFeatures(JSONObject JsonObject) {
    	
    	ArrayList<String> caption_list = (ArrayList<String>) JsonObject.get("nl");
    	ArrayList<String> code_list = (ArrayList<String>) JsonObject.get("code");
    	varNames = (ArrayList<String>) JsonObject.get("varNames");
    	varTypes = (ArrayList<String>) JsonObject.get("varTypes");

    	String caption = String.join(" ", caption_list);
    	String code = String.join(" ", code_list);
    	
    	
        CompilationUnit m_CompilationUnit = parseFileWithRetries(code);
        FunctionVisitor functionVisitor = new FunctionVisitor(m_CommandLineValues);

        functionVisitor.visit(m_CompilationUnit, null);

        MethodContent methods = functionVisitor.getMethodContents();
        methods.setCaption(caption);
        methods.setVariables(varNames, varTypes);
        
		Class_Types = functionVisitor.getClassContents();

        return generatePathFeatures(methods);
    }

    private CompilationUnit parseFileWithRetries(String code) {
        final String classPrefix = "public class Test {";
        final String classSuffix = "}";
        final String methodPrefix = "SomeUnknownReturnType f() {";
        final String methodSuffix = "return noSuchReturnValue; }";

        String content = code;
        CompilationUnit parsed;
        try {
            parsed = JavaParser.parse(content);
        } catch (ParseProblemException e1) {
            // Wrap with a class and method
            try {
                content = classPrefix + methodPrefix + code + methodSuffix + classSuffix;
                parsed = JavaParser.parse(content);
            } catch (ParseProblemException e2) {
                // Wrap with a class only
                content = classPrefix + code + classSuffix;
                parsed = JavaParser.parse(content);
            }
        }

        return parsed;
    }

    private ProgramFeatures generatePathFeatures(MethodContent method) {
        ArrayList<ProgramFeatures> methodsFeatures = new ArrayList<>();
        ProgramFeatures singleMethodFeatures = generatePathFeaturesForFunction(method);
        return singleMethodFeatures;
    }

    private ProgramFeatures generatePathFeaturesForFunction(MethodContent methodContent) {
        ArrayList<Node> functionLeaves = methodContent.getLeaves();
        
        
        //여기 name에 caption 넣기
        ProgramFeatures programFeatures = new ProgramFeatures(methodContent.getCaption());

        
        for (int i = 0; i < functionLeaves.size(); i++) {
            for (int j = i + 1; j < functionLeaves.size(); j++) {
                String separator = Common.EmptyString;

                String path = generatePath(functionLeaves.get(i), functionLeaves.get(j), separator, methodContent.getClassId());
                if (path != Common.EmptyString) {
                    Property source = functionLeaves.get(i).getUserData(Common.PropertyKey);
                    Property target = functionLeaves.get(j).getUserData(Common.PropertyKey);
                    programFeatures.addFeature(source, path, target);
                }
            }
        }
        return programFeatures;
    }

    
    ////////////////////////////////////////////////////////
	private ArrayList<Integer> getParentBlock(Node node) {
		ArrayList<Integer> Block_number = new ArrayList<>();
		Node current = node;
		while (true) {
			if (current.getParentNode() instanceof ClassOrInterfaceDeclaration || current.getParentNode() == null) {
				break;
			}
			if (current instanceof BlockStmt) {
				Block_number.add(current.getUserData(Common.BlockId));
			}
			current = current.getParentNode();
		}
		return Block_number;
	}

	private String SearchType(ArrayList<Integer> Block_number, Node node, int ClassId) {

		Map<String, String> Types = new HashMap<String, String>();
		String type = "";

		if (node instanceof PrimitiveType || node instanceof VoidType) {
			return node.toString();
		}
		else if (node instanceof Parameter) {
			return node.getChildrenNodes().get(1).toString();
		}
		else if (node.getParentNode() instanceof Parameter) {
			return node.getParentNode().getChildrenNodes().get(1).toString();
		}
		else if (node instanceof VariableDeclarationExpr) {
			return node.toString().split(" ")[0];
		}
		for (int i = 0; i < Block_number.size(); i++) {
			Types = Class_Types.get(ClassId).getBlock().get(Block_number.get(i));
			if (Types.get(node.toString()) != null) {
				type = Types.get(node.toString());
				break;
			}
		}
		
		
		if (type == "") {
			int var_index = varNames.indexOf(node.toString());
			if (var_index != -1) {
				type = varTypes.get(var_index);
			}
		}
		return type;
	}
	////////////////////////////////////////////////////////////////
	
    private String generatePath(Node source, Node target, String separator, int ClassId) {

        StringJoiner stringBuilder = new StringJoiner(separator);
        ArrayList<Node> sourceStack = getTreeStack(source);
        ArrayList<Node> targetStack = getTreeStack(target);

        
        List Map_List = new ArrayList<Object>();
        ArrayList<Integer> Block_number = new ArrayList<>();
		String type;
		
		
        int commonPrefix = 0;
        int currentSourceAncestorIndex = sourceStack.size() - 1;
        int currentTargetAncestorIndex = targetStack.size() - 1;
        while (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0
                && sourceStack.get(currentSourceAncestorIndex) == targetStack.get(currentTargetAncestorIndex)) {
            commonPrefix++;
            currentSourceAncestorIndex--;
            currentTargetAncestorIndex--;
        }

        int pathLength = sourceStack.size() + targetStack.size() - 2 * commonPrefix;
        if (pathLength > m_CommandLineValues.MaxPathLength) {
            return Common.EmptyString;
        }

        if (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0) {
            int pathWidth = targetStack.get(currentTargetAncestorIndex).getUserData(Common.ChildId)
                    - sourceStack.get(currentSourceAncestorIndex).getUserData(Common.ChildId);
            if (pathWidth > m_CommandLineValues.MaxPathWidth) {
                return Common.EmptyString;
            }
        }

		
		for (int i = 0; i < sourceStack.size() - commonPrefix; i++) {
			Node currentNode = sourceStack.get(i);
			String childId = Common.EmptyString;
			String parentRawType = currentNode.getParentNode().getUserData(Common.PropertyKey).getRawType();
			
			try {
				Block_number = getParentBlock(currentNode);
				type = SearchType(Block_number, currentNode, ClassId);
			} catch (Exception e1) {
				type = "";
			}
			if (i == 0 || s_ParentTypeToAddChildId.contains(parentRawType)) {
				childId = saturateChildId(currentNode.getUserData(Common.ChildId))
						.toString();
			}
			if (type != "") {
				stringBuilder.add(String.format("%s%s%s%s%s",
						currentNode.getUserData(Common.PropertyKey).getType(true), childId,
						typeSym, type, upSymbol));
			}
			else {
				stringBuilder.add(String.format("%s%s%s", 
						currentNode.getUserData(Common.PropertyKey).getType(true), childId, upSymbol));
			}
		}


        Node commonNode = sourceStack.get(sourceStack.size() - commonPrefix);
        String commonNodeChildId = Common.EmptyString;
        Property parentNodeProperty = commonNode.getParentNode().getUserData(Common.PropertyKey);
        String commonNodeParentRawType = Common.EmptyString;
        if (parentNodeProperty != null) {
            commonNodeParentRawType = parentNodeProperty.getRawType();
        }
        if (s_ParentTypeToAddChildId.contains(commonNodeParentRawType)) {
            commonNodeChildId = saturateChildId(commonNode.getUserData(Common.ChildId))
                    .toString();
        }

		try {
			Block_number = getParentBlock(commonNode);
			type = SearchType(Block_number, commonNode, ClassId);
		} catch(Exception e1) {
			type = "";
		}
		if (type != "") {
			stringBuilder.add(String.format("%s%s%s%s",
					commonNode.getUserData(Common.PropertyKey).getType(true), commonNodeChildId,
					typeSym, type));
		}
		else {
			stringBuilder.add(String.format("%s%s",
					commonNode.getUserData(Common.PropertyKey).getType(true), commonNodeChildId));
		}

		for (int i = targetStack.size() - commonPrefix - 1; i >= 0; i--) {
			Node currentNode = targetStack.get(i);
			String childId = Common.EmptyString;
			
			try {
				Block_number = getParentBlock(currentNode);
				type = SearchType(Block_number, currentNode, ClassId);
			} catch (Exception e1) {
				type = "";
			}
			
			if (i == 0 || s_ParentTypeToAddChildId.contains(currentNode.getUserData(Common.PropertyKey).getRawType())) {
				childId = saturateChildId(currentNode.getUserData(Common.ChildId))
						.toString();
			}
			
			if (type != "") {
				stringBuilder.add(String.format("%s%s%s%s%s", downSymbol,
						currentNode.getUserData(Common.PropertyKey).getType(true), childId,
						typeSym, type));
			}
			else {
				stringBuilder.add(String.format("%s%s%s", downSymbol,
						currentNode.getUserData(Common.PropertyKey).getType(true), childId));
			}

		}

        return stringBuilder.toString();
    }

    private Integer saturateChildId(int childId) {
        return Math.min(childId, m_CommandLineValues.MaxChildId);
    }
}
