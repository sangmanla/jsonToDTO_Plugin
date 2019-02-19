package sangman.jsontodto;

public class Util {

	public static Util getInstance() {
		return UtilHolder.instance;
	}

	private static class UtilHolder {
		final static Util instance = new Util();
	}

	public String getCapital(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
}
