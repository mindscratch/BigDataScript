package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessages;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.Scope;

/**
 * Boolean expression
 * 
 * @author pcingola
 */
public class ExpressionLogic extends ExpressionBinary {

	public ExpressionLogic(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;

		super.returnType(scope);
		returnType = Type.get(PrimitiveType.BOOL);

		return returnType;

	}

	@Override
	public void typeCheckNotNull(Scope scope, CompilerMessages compilerMessages) {
		// Can we transform 'left' into an int?
		left.checkCanCastBool(compilerMessages);
		right.checkCanCastBool(compilerMessages);
	}

}
