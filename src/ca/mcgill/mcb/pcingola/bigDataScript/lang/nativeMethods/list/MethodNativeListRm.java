package ca.mcgill.mcb.pcingola.bigDataScript.lang.nativeMethods.list;

import java.util.List;

import ca.mcgill.mcb.pcingola.bigDataScript.lang.Parameters;
import ca.mcgill.mcb.pcingola.bigDataScript.lang.Type;
import ca.mcgill.mcb.pcingola.bigDataScript.lang.TypeList;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BdsThread;

/**
 * Rm: Delete all files in list
 * 
 * @author pcingola
 */
public class MethodNativeListRm extends MethodNativeList {

	public MethodNativeListRm(Type baseType) {
		super(baseType);
	}

	@Override
	protected void initMethod(Type baseType) {
		functionName = "rm";
		classType = TypeList.get(baseType);
		returnType = Type.VOID;

		String argNames[] = { "this" };
		Type argTypes[] = { classType };
		parameters = Parameters.get(argTypes, argNames);
		addNativeMethodToClassScope();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected Object runMethodNative(BdsThread bdsThread, Object objThis) {
		List list = (List) objThis;

		for (Object o : list)
			bdsThread.data(o.toString()).delete();

		return objThis;
	}
}
