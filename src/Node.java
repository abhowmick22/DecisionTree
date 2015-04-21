import java.util.List;

/* A node of the decision tree */
public class Node {
	public String splitOn;											// Stores which attribute to split on at a particular node
	public String label;											// Stores the class label for leaf nodes. For nodes that are not leaf nodes, it stores the value of the attribute of the parent's' split 
	public boolean isLeaf;											// boolean flag for leaf nodes
	public List<String> childrenValues;							// Stores the values of the children attributes
	public List<Node> children;	
}
