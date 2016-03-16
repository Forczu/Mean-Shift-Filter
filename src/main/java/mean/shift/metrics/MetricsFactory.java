package mean.shift.metrics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MetricsFactory {

	private static Map<String, Method> callbackMap = new HashMap<String, Method>();

	static {
		try {
			Register("Euklidesowa", EuclideanMetrics.class.getMethod("getInstance", (Class<?>[])null));
			Register("Manhattan", ManhattanMetrics.class.getMethod("getInstance", (Class<?>[])null));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	private static void Register(String type, Method method) {
		callbackMap.put(type, method);
	}

	public static Metrics getMetrics(String type) {
		try {
			return (Metrics) callbackMap.get(type).invoke(null);
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
