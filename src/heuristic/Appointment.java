package heuristic;
/**
 * Appointment generation
 * 
 * @author rafael
 *
 */
public abstract class Appointment {
	/**
	 * Method that generates appointments
	 * 
	 * @param n:
	 *            population size
	 * @param Q:
	 *            number of values that the quasi-id takes
	 * @param f:
	 *            frequency of each quasi-id value
	 * @param R:
	 *            number of resources
	 * @param c:
	 *            capacity of each resource
	 * @return anonymity vector
	 */
	public abstract int[] generate(int n, int Q, int[] f, int R, int[] c);

	/*public int[] generate(Population p) {
		return generate(p.getN(), p.getQ(), p.getF(), p.getR(), p.getC());
	}
*/
}
