package pset3;

import java.util.*;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

public class CFG {
	Set<Node> nodes = new HashSet<Node>();
	Map<Node, Set<Node>> edges = new HashMap<Node, Set<Node>>();
	
	public static class Node {
		int position;
		Method method;
		JavaClass clazz;
		
		Node(int p, Method m, JavaClass c) {
			position = p;
			method = m;
			clazz = c;
		}
		
		public Method getMethod() {
			return method;
		}
		
		public JavaClass getClazz() {
			return clazz;
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof Node)) return false;
			Node n = (Node)o;
			return (position == n.position) && method.equals(n.method) && clazz.equals(n.clazz);
		}
		
		public int hashCode() {
			return position + method.hashCode() + clazz.hashCode();
		}
		
		public String toString() {
			return clazz.getClassName() + '.' + method.getName() + method.getSignature() + ": " + position;
		}
	}
	
	public void addNode(int p, Method m, JavaClass c) {
		addNode(new Node(p, m, c));
	}
	
	private void addNode(Node n) {
		nodes.add(n);
		Set<Node> nbrs = edges.get(n);
		if (nbrs == null) {
			nbrs = new HashSet<Node>();
			edges.put(n, nbrs);
		}
	}

		public void addEdge(int p1, Method m1, JavaClass c1, int p2, Method m2, JavaClass c2) {
			Node n1 = new Node(p1, m1, c1);
			Node n2 = new Node(p2, m2, c2);
			addNode(n1);
			addNode(n2);
			Set<Node> nbrs = edges.get(n1);
			nbrs.add(n2);
			edges.put(n1, nbrs);
		}

		public void addEdge(int p1, int p2, Method m, JavaClass c) {
			addEdge(p1, m, c, p2, m, c);
		}

		public String toString() {
			return nodes.size() + " nodes\n" + "nodes: " + nodes + '\n' + "edges: " + edges;
		}

		public boolean isReachable(String methodFrom, String clazzFrom, String methodTo, String clazzTo) {
			// you will implement this method in Question 2.2
			
			boolean exists = false;
			Node start = new Node(0, null, null);
			for(Node n: nodes) {
				if((n.position == 0) && n.getMethod().getName().equals(methodFrom) && n.getClazz().getClassName().equals(clazzFrom)) {
					start = n;
					exists = true;
					break;
				}
			}
			
			//check if method exists
			if(!exists) {
				return false;
			}
			
			//if node to reach is itself
			if(methodFrom.equals(methodTo) && clazzFrom.equals(clazzTo)) {
				return true;
			}
			
			Map<Node, Node> visited = new HashMap<>();	// store visited nodes in <child, parent> pairs
	        Queue<Node> queue = new LinkedList<>();
	        visited.put(start, null);
	        queue.add(start);
	        while(!queue.isEmpty()) {
	        	Node node = queue.remove();
	        	if(node.position == -1 && node.getMethod().getName().equals(methodFrom) && node.getClazz().getClassName().equals(clazzFrom)) {
	        		node = queue.peek();
	        		if(node != null) {
	        			node = queue.remove();
	        		} else {
	        			break;
	        		}
	        	}
	        	if(node.getMethod().getName().equals(methodTo) && node.getClazz().getClassName().equals(clazzTo)) {
	        		if(visited.get(node).position != -1) {
	        			return true;
	        		} else {
	        			node = queue.peek();
	        			if(node != null) {
	        				node = queue.remove();
	        			} else {
	        				break;
	        			}
	        		}
	        	}
	        	for(Node neighbor: edges.get(node)) {
	        		if(!visited.containsKey(neighbor)) {
	        			visited.put(neighbor, node);
	        			queue.add(neighbor);
	        		}
	        	}
	        	
	        }
			return false;
		}
	}