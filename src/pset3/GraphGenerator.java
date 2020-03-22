package pset3;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class GraphGenerator {
	public CFG createCFG(String className) throws ClassNotFoundException {
		CFG cfg = new CFG();
		JavaClass jc = Repository.lookupClass(className);
		ClassGen cg = new ClassGen(jc);
		ConstantPoolGen cpg = cg.getConstantPool();
		
		for (Method m: cg.getMethods()) {
			MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
			InstructionList il = mg.getInstructionList();
			InstructionHandle[] handles = il.getInstructionHandles();
			for (InstructionHandle ih: handles) {
				int position = ih.getPosition();
				cfg.addNode(position, m, jc);
				Instruction inst = ih.getInstruction();
				// your code goes here
				
				//Dummy Exit Node = -1
				if(inst instanceof ReturnInstruction) {
					cfg.addEdge(position, -1, m, jc);
				} else if(inst instanceof BranchInstruction) {
					BranchInstruction branchInstruction = (BranchInstruction) inst;
					int branchPosition = branchInstruction.getTarget().getPosition();
					cfg.addEdge(position, branchPosition, m, jc);
					int nextPosition = ih.getNext().getPosition();
					cfg.addEdge(position, nextPosition, m, jc);
				} else {
					int nextPosition = ih.getNext().getPosition();
					cfg.addEdge(position, nextPosition, m, jc);
				}
			}
		}
		return cfg;
	}
	public CFG createCFGWithMethodInvocation(String className) throws ClassNotFoundException {
		// your code goes here
		CFG cfg = new CFG();
		JavaClass jc = Repository.lookupClass(className);
		ClassGen cg = new ClassGen(jc);
		ConstantPoolGen cpg = cg.getConstantPool();
		
		for (Method m: cg.getMethods()) {
			MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
			InstructionList il = mg.getInstructionList();
			InstructionHandle[] handles = il.getInstructionHandles();
			for (InstructionHandle ih: handles) {
				int position = ih.getPosition();
				cfg.addNode(position, m, jc);
				Instruction inst = ih.getInstruction();
				
				if(inst instanceof ReturnInstruction) {
					cfg.addEdge(position, -1, m, jc);
				} else if(inst instanceof BranchInstruction) {
					BranchInstruction branchInstruction = (BranchInstruction) inst;
					int branchPosition = branchInstruction.getTarget().getPosition();
					cfg.addEdge(position, branchPosition, m, jc);
					int nextPosition = ih.getNext().getPosition();
					cfg.addEdge(position, nextPosition, m, jc);
				} else if(inst instanceof INVOKESTATIC){
					INVOKESTATIC invokeInstruction = (INVOKESTATIC) inst;
					JavaClass invokeJC = Repository.lookupClass(invokeInstruction.getClassName(cpg));
					Method invokeMethod = cg.containsMethod(invokeInstruction.getName(cpg), invokeInstruction.getSignature(cpg));
					cfg.addEdge(position, m, jc, 0, invokeMethod, invokeJC);
					cfg.addEdge(-1, invokeMethod, invokeJC, ih.getNext().getPosition(), m, jc);
				} else {
					int nextPosition = ih.getNext().getPosition();
					cfg.addEdge(position, nextPosition, m, jc);
				}
			}
		}
		return cfg;
	}

	public static void main(String[] a) throws ClassNotFoundException {
		GraphGenerator gg = new GraphGenerator();
		CFG cfg = gg.createCFG("pset3.C"); // example invocation of createCFG
		System.out.println(cfg.toString());
		System.out.println();
		
		CFG cfg2 = gg.createCFGWithMethodInvocation("pset3.D"); // example invocation of createCFGWithMethodInovcation
		System.out.println(cfg2.toString());
		System.out.println();
		
		System.out.println("Testing methods reachable from main:");
		System.out.println(cfg2.isReachable("main", "pset3.D", "main", "pset3.D"));		//main -> main; true
		System.out.println(cfg2.isReachable("main", "pset3.D", "foo", "pset3.D"));		//main -> foo; true
		System.out.println(cfg2.isReachable("main", "pset3.D", "bar", "pset3.D"));		//main -> bar; true
		System.out.println(cfg2.isReachable("main", "pset3.D", "<init>", "pset3.D"));	//main -> <init>; false
		System.out.println();
		
		System.out.println("Testing methods reachable from <init>:");
		System.out.println(cfg2.isReachable("<init>", "pset3.D", "<init>", "pset3.D"));	//<init> -> <init>; true
		System.out.println(cfg2.isReachable("<init>", "pset3.D", "main", "pset3.D"));	//<init> -> main; false
		System.out.println(cfg2.isReachable("<init>", "pset3.D", "foo", "pset3.D"));	//<init> -> foo; false
		System.out.println(cfg2.isReachable("<init>", "pset3.D", "bar", "pset3.D"));	//<init> -> bar; false
		System.out.println();
		
		System.out.println("Testing methods reachable from foo:");
		System.out.println(cfg2.isReachable("foo", "pset3.D", "foo", "pset3.D"));		//foo -> foo; true
		System.out.println(cfg2.isReachable("foo", "pset3.D", "main", "pset3.D"));		//foo -> main; false
		System.out.println(cfg2.isReachable("foo", "pset3.D", "bar", "pset3.D"));		//foo -> bar; true
		System.out.println(cfg2.isReachable("foo", "pset3.D", "<init>", "pset3.D"));	//foo -> <init>; false	
		System.out.println();
		
		System.out.println("Testing methods reachable from bar:");
		System.out.println(cfg2.isReachable("bar", "pset3.D", "bar", "pset3.D"));		//bar -> bar; true
		System.out.println(cfg2.isReachable("bar", "pset3.D", "main", "pset3.D"));		//bar -> main; false
		System.out.println(cfg2.isReachable("bar", "pset3.D", "foo", "pset3.D"));		//bar -> foo; false
		System.out.println(cfg2.isReachable("bar", "pset3.D", "<init>", "pset3.D"));	//bar -> <init>; false
		System.out.println();
		
		System.out.println("Edge cases:");
		System.out.println(cfg2.isReachable("main", "pset3.D", "boss", "pset3.D"));		//methodTo doesn't exist; false
		System.out.println(cfg2.isReachable("boss", "pset3.D", "main", "pset3.D"));		//methodFrom doesn't exist; false
		System.out.println(cfg2.isReachable("boss", "pset3.D", "boss", "pset3.D"));		//methods don't exist; false
		System.out.println();
	}
}