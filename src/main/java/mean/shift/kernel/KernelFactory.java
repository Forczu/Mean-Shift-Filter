 	package mean.shift.kernel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class KernelFactory {


	private static Map<String, Method> callbackMap = new HashMap<String, Method>();

	static {
		try {
			Register(GaussianKernel.getName(), GaussianKernel.class.getMethod("getInstance", (Class<?>[])null));
			Register(RectangularKernel.getName(), RectangularKernel.class.getMethod("getInstance", (Class<?>[])null));
			Register(EpanechnikovKernel.getName(), EpanechnikovKernel.class.getMethod("getInstance", (Class<?>[])null));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	private static void Register(String type, Method method) {
		callbackMap.put(type, method);
	}

	public static Kernel getKernel(String type) {
		try {
			return (Kernel) callbackMap.get(type).invoke(null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
