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
	}
}