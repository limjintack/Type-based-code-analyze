package JavaExtractor.Visitors;

import JavaExtractor.Common.ClassContent;
import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.Common.MethodContent;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("StringEquality")
public class FunctionVisitor extends VoidVisitorAdapter<Object> {
    private final ArrayList<MethodContent> m_Methods = new ArrayList<>();
    private final CommandLineValues m_CommandLineValues;
    
	private ArrayList<ClassContent> c_Content = new ArrayList<>();
	private int Block_id;
	private int Class_id = -1;

    public FunctionVisitor(CommandLineValues commandLineValues) {
        this.m_CommandLineValues = commandLineValues;
    }

    @Override
    public void visit(MethodDeclaration node, Object arg) {
        visitMethod(node);

        super.visit(node, arg);
    }


	@Override
	public void visit(ClassOrInterfaceDeclaration node, Object arg) {
		Block_id = 0;
		visitVariable(node, arg);
		
		super.visit(node, arg);
	}

	@Override
	public void visit(BlockStmt node, Object arg) {

		visitBlock(node, arg);
		
		super.visit(node, arg);
	}
	
	private void visitBlock(BlockStmt node, Object obj) {
		
	    Map<String, String> Variable_List = new HashMap<String, String>();
		List<Node> Child = new ArrayList<>();
		List<String> Split_Type = new ArrayList<String>();

		node.setUserData(Common.BlockId, Block_id);
		Child = node.getChildrenNodes();

		for (int i = 0; i < Child.size(); i++) {
			List<Node> Childs = new ArrayList<>();
			Childs = Child.get(i).getChildrenNodes();
			for (int j = 0; j < Childs.size(); j++) {
				if (Childs.get(j) instanceof VariableDeclarationExpr) {
					String type = ((VariableDeclarationExpr) Childs.get(j)).getElementType().toString();
					for (int p = 0 ; p < ((VariableDeclarationExpr) Childs.get(j)).getVariables().size(); p++) {
						String var = ((VariableDeclarationExpr) Childs.get(j)).getVariables().get(p).toString().split("=")[0].trim();
						Variable_List.put(var, type);
					}
				}
			}
		}
		
		if (node.getParentNode() instanceof MethodDeclaration) {
			String Param_Type, Param_Var;
			for (Node Child_Node : node.getParentNode().getChildrenNodes()) {
				if (Child_Node instanceof Parameter) {
					Param_Type = Child_Node.toString().split(" ")[0];
					Param_Var = Child_Node.toString().split(" ")[1];
					//if (CheckType(Param_Type))
					Variable_List.put(Param_Var, Param_Type);
				}
			}
		}
		
		c_Content.get(Class_id).AddBlock(Variable_List);
	    Block_id++;
			
	}
	
	private void visitVariable(ClassOrInterfaceDeclaration node, Object obj) {
		
		Map<String, String> Field_Types = new HashMap<String, String>();
		List<String> Split_Type = new ArrayList<String>();
	    
		Class_id++;
		node.setUserData(Common.ClassId, Class_id);
		
		ArrayList<FieldDeclaration> c_Type = new ArrayList<>();
		
		if (node.getFields() != null) {
			c_Type.addAll(node.getFields());
		}
		for (int i = 0; i < c_Type.size(); i++) {
			String type = c_Type.get(i).getElementType().toString();
			for (int p = 0 ; p < c_Type.get(i).getVariables().size(); p++) {
				String var = c_Type.get(i).getVariables().get(p).toString().split("=")[0].trim();
				Field_Types.put(var, type);
			}
		}
		c_Content.add(new ClassContent(Field_Types));
	}
	
    private void visitMethod(MethodDeclaration node) {
        LeavesCollectorVisitor leavesCollectorVisitor = new LeavesCollectorVisitor();
        leavesCollectorVisitor.visitDepthFirst(node);
        ArrayList<Node> leaves = leavesCollectorVisitor.getLeaves();

        String normalizedMethodName = Common.normalizeName(node.getName(), Common.BlankWord);
        ArrayList<String> splitNameParts = Common.splitToSubtokens(node.getName());
        String splitName = normalizedMethodName;
        if (splitNameParts.size() > 0) {
            splitName = String.join(Common.internalSeparator, splitNameParts);
        }

        if (node.getBody() != null) {
            long methodLength = getMethodLength(node.getBody().toString());
            if (m_CommandLineValues.MaxCodeLength > 0) {
                if (methodLength >= m_CommandLineValues.MinCodeLength && methodLength <= m_CommandLineValues.MaxCodeLength) {
                    m_Methods.add(new MethodContent(leaves, splitName, getClassId(node)));
                }
            } else {
                m_Methods.add(new MethodContent(leaves, splitName, getClassId(node)));
            }
        }
    }

	private int getClassId(Node node) {
		int index = -1;
		Node current = node;
		while (current != null) {
			if (current instanceof ClassOrInterfaceDeclaration) {
				index = current.getUserData(Common.ClassId);
				return index;
			}
			current = current.getParentNode();
		}
		return index;
	}

    private long getMethodLength(String code) {
        String cleanCode = code.replaceAll("\r\n", "\n").replaceAll("\t", " ");
        if (cleanCode.startsWith("{\n"))
            cleanCode = cleanCode.substring(3).trim();
        if (cleanCode.endsWith("\n}"))
            cleanCode = cleanCode.substring(0, cleanCode.length() - 2).trim();
        if (cleanCode.length() == 0) {
            return 0;
        }
        return Arrays.stream(cleanCode.split("\n"))
                .filter(line -> (line.trim() != "{" && line.trim() != "}" && line.trim() != ""))
                .filter(line -> !line.trim().startsWith("/") && !line.trim().startsWith("*")).count();
    }

    public MethodContent getMethodContents() {
        return m_Methods.get(0);
    }
    

	public ArrayList<ClassContent> getClassContents() {
		return c_Content;
	}
}
