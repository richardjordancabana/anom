package anom;

import java.util.Arrays;
import java.util.Random;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


/**
 * Generating appointments
 *
 */
public class Appointment {

	/**
	 * total Q number of quasi-ids f frequency of each quasi. Array of Q
	 * elements R number of resources c capacity of each resource
	 */
	int n;
	int Q;
	int[] f;
	int R;
	int[] c;

	/**
	 * @param f
	 *            frequency of each quasi. Array of Q elements
	 * @param c
	 *            capacity of each resource
	 */
	public Appointment(int[] f, int[] c) {
		this.Q = f.length;
		this.R = c.length;
		n = 0;
		for (int i = 0; i < Q; i++)
			n += f[i]; // poblacion total
		this.f = new int[f.length];
		this.c = new int[c.length];
		System.arraycopy(f, 0, this.f, 0, f.length);
		System.arraycopy(c, 0, this.c, 0, c.length);
	}

	/**
	 * Generates: - resources capacity given the parameters and using random
	 * values (attr. c) - Vector of quasiids (f) Precond: cmax*R<n Postconds: -
	 * sum(c)>=n. c.length==R - sum(f)=n. f.length==Q
	 * 
	 * @param n:
	 *            total population
	 * @param Q:
	 *            Number of quasis
	 * @param qmin:
	 *            Minimum frequency for any quasiid
	 * @param qmax:
	 *            Max. frequency for any quasi
	 * @param R:
	 *            Number of resources
	 * @param cmin:
	 *            Minimum capacity
	 * @param cmax:
	 *            Max. capacity.
	 */
	public Appointment(int n, int Q, int qmin, int qmax, int R, int cmin, int cmax) {
		this.n = n;
		this.Q = Q;
		this.R = R;
		// create the vectors
		f = generateRandom(Q, n, qmin, qmax, true);
		c = generateRandom(R, n, cmin, cmax, false);
		if (f == null || c == null)
			System.out.println("Erroneous appointment parameters ");

	}

	/**
	 * Generates a new vector v of size l such that: - either sum(v)==n (if
	 * equal=true) or sum(v)>=n (if equal=false) - min<=v[i]<=max
	 * 
	 * @param l
	 *            size of the vector
	 * @param n
	 *            total population
	 * @param min
	 *            min value for each vector component
	 * @param max
	 *            max. value for each vector component
	 * @param equal:
	 *            true if the sum of the produced vector must be exactly n.
	 *            false indicates that it can be also >= n
	 * @return the new vector, or null if l*max < n
	 */
	private int[] generateRandom(int l, int n, int min, int max, boolean equal) {
		int[] vector = new int[l];
		if (l * max < n)
			vector = null;
		else {
			Random rnd = new Random();
			// first attempt
			for (int i = 0; i < l; i++)
				vector[i] = (int) (rnd.nextDouble() * (max - min + 1) + min);
			// get the sum and compare with n
			int sum = 0;
			for (int i = 0; i < l; i++)
				sum += vector[i];
			// if too few; pick up elements with value < max and increment
			while (sum < n) {
				boolean found = false;
				for (int i = 0; i < l && !found; i++)
					if (vector[i] < max) {
						found = true;
						sum++;
						vector[i]++;
					}
			}
			// too many
			while (sum > n && equal) {
				boolean found = false;
				for (int i = 0; i < l && !found; i++)
					if (vector[i] > min) {
						found = true;
						sum--;
						vector[i]--;
					}
			}
		}
		return vector;

	}

	@Override
	public String toString() {
		String r = "N: " + n + "\nQ:" + Q + "\nf={";
		for (int i = 0; i < Q; i++)
			r += f[i] + (i == Q - 1 ? "}\n" : ",");
		r += "R: " + R + "\nc={";
		for (int i = 0; i < R; i++)
			r += c[i] + (i == R - 1 ? "}\n" : ",");

		return r;
	}

	/**
	 * @return anonymity vector based on the frequency vector
	 */
	int[] base() {
		int[] v = new int[n];
		Arrays.fill(v, 0); // innecesario, pero una buena práctica
		for (int i = 0; i < f.length; i++)
			v[f[i] - 1]++;

		return v;
	}

	/**
	 * Pretty print of the anonymity vector in Java array format
	 * 
	 * @param v
	 */
	public void printV(int[] v) {
		int end;
		for (end = v.length - 1; end >= 0 && v[end] == 0; end--)
			;
		if (end > -1)
			System.out.print("{");
		for (int i = 0; i <= end; i++)
			System.out.print(v[i] + (i == end ? "} " : ","));
		System.out.println("");
	}

	// Richard's getKMin
	int[] chocoIterative() {

		int[] v = new int[n];// vector de anonimicidad TAMAÑO MIN DE LOS MAX.
								// Ric;hard

		int[][] total = null;

		for (int i = 0; i < n; i++)
			v[i] = 0;
		int suma = 0;
		// Rafa: esto sobra
		// for (int i = 0; i < n; i++)
		// suma = suma + v[i] * i;

		IntVar[] vchoco = null;
		IntVar k;
		int l = 1; // nivel
		IntVar[] a;
		// IntVar[] cuenta;
		int kvalor = 0;
		int minLocal = 0;

		while (suma != n) {

			Solver solver = new Solver("Minimize K");
			a = new IntVar[Q * R];// matriz plana
			for (int i = 0; i < Q; i++) {
				for (int j = 0; j < R; j++) {
					minLocal = Math.min(f[i], c[j]);

					a[i * R + j] = VariableFactory.bounded("a" + i + "_" + j, 0, minLocal, solver);
				}
			}

			// restricciones
			IntVar[] fila = null;
			IntVar[] columna = null;
			// C1
			for (int i = 0; i < Q; i++) {
				fila = new IntVar[R];
				for (int j = 0; j < R; j++) {
					fila[j] = a[i * R + j];
				}

				// IntVar sum=VariableFactory.enumerated(qf[i+1]+"", qf[i+1],
				// qf[i+1], solver);//TRUCO?
				IntVar sum = VariableFactory.fixed(f[i], solver);
				solver.post(IntConstraintFactory.sum(fila, sum));
			}
			// C2
			for (int i = 0; i < R; i++) {
				columna = new IntVar[Q];
				for (int j = 0; j < Q; j++) {

					columna[j] = a[i + j * R];
				}
				// IntVar sum1=VariableFactory.enumerated(rc[i+1]+"", rc[i+1],
				// rc[i+1], solver);
				IntVar sum1 = VariableFactory.fixed(c[i], solver);
				solver.post(IntConstraintFactory.sum(columna, "<=", sum1));
			}

			// C3
			if (l != 1) {// cambios probar
				vchoco = VariableFactory.enumeratedArray("vchoco", l - 1, 0, n, solver);// rafa:
																						// cambio
																						// l
																						// por
																						// l-1
				// cambiar max=poblacion

				// copiar los valores ya establecidos de
				for (int i = 0; i < l - 1; i++)
					solver.post(IntConstraintFactory.arithm(vchoco[i], "=", v[i]));

			}
			for (int i = 0; i < l - 1; i++)
				solver.post(IntConstraintFactory.count(i + 1, a, vchoco[i]));
			// C4
			// max poblacion/l+1
			k = VariableFactory.enumerated("k", 0, (n - suma) / l, solver);
			solver.post(IntConstraintFactory.count(l, a, k));
			// solver.post(IntConstraintFactory.arithm(vchoco[l], "=", k));
			// minimizar k

			if (true) {
				IntVar[] av = new IntVar[Q * R + 1];
				for (int i = 0; i < Q * R; i++)
					av[i + 1] = a[i];
				av[0] = k;
				solver.set(ISF.custom(ISF.lexico_var_selector(), ISF.min_value_selector(), av));
			}

			solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, k);

			if (solver.findSolution()) {

				// Chatterbox.printStatistics(solver);
				// Chatterbox.printSolutions(solver);
				kvalor = k.getValue();

				v[l - 1] = kvalor;
				l++;
				suma = 0;
				// printV(v);

				for (int i = 0; i < l - 1; i++)
					suma = suma + v[i] * (i + 1);

				if (suma == n) {
					total = new int[Q][R];

					for (int i = 0; i < Q; i++) {
						for (int j = 0; j < R; j++) {
							total[i][j] = a[i * R + j].getValue();
						}
					}

				}
			}

		}

		return v;

	}

	/**
	 * 
	 * 
	 * @return Anonymity vector for the current problem
	 */
	public int[] chocoLex() {

		int[] v = new int[n];// vector de anonimicidad TAMAÑO MIN DE LOS MAX.
								// Ric;hard

		Solver solver = new Solver("anom");

		IntVar[] a = new IntVar[Q * R];// matriz plana
		int minLocal;
		for (int i = 0; i < Q; i++) {
			for (int j = 0; j < R; j++) {
				minLocal = Math.min(f[i], c[j]);

				a[i * R + j] = VariableFactory.enumerated("a" + i + "_" + j, 0, minLocal, solver);
			}
		}

		IntVar[] fila = null;
		IntVar[] columna = null;
		// C1
		for (int i = 0; i < Q; i++) {
			fila = new IntVar[R];
			for (int j = 0; j < R; j++) {
				fila[j] = a[i * R + j];
			}

			// IntVar sum=VariableFactory.enumerated(qf[i+1]+"", qf[i+1],
			// qf[i+1], solver);//TRUCO?
			IntVar sum = VariableFactory.fixed(f[i], solver);
			solver.post(IntConstraintFactory.sum(fila, sum));
		}
		// C2
		for (int i = 0; i < R; i++) {
			columna = new IntVar[Q];
			for (int j = 0; j < Q; j++) {

				columna[j] = a[i + j * R];
			}
			// IntVar sum1=VariableFactory.enumerated(rc[i+1]+"", rc[i+1],
			// rc[i+1], solver);
			IntVar sum1 = VariableFactory.fixed(c[i], solver);
			solver.post(IntConstraintFactory.sum(columna, "<=", sum1));
		}

		// C3

		int[] weight = new int[n];
		IntVar[] vchoco = new IntVar[n];
		IntVar[] weightByValue = new IntVar[n];
		for (int i = 0; i < n; i++) {
			vchoco[i] = VariableFactory.enumerated("v" + (i + 1), 0, n / (i + 1), solver);
			weightByValue[i] = VariableFactory.enumerated("wbyvalue" + (i + 1), 0, n, solver);
		}
		for (int i = 0; i < n; i++)
			weight[i] = i + 1;
		for (int i = 0; i < n; i++)
			solver.post(IntConstraintFactory.times(vchoco[i], i + 1, weightByValue[i]));

		solver.post(IntConstraintFactory.global_cardinality(a, weight, vchoco, false));

		// C4: vchoco is a valid anonymity vector
		IntVar ps = VariableFactory.fixed(n, solver);
		solver.post(IntConstraintFactory.sum(weightByValue, ps));

		// System.out.println("Al find solution!");
		int l = vchoco.length + a.length;
		IntVar[] vl = new IntVar[l];
		for (int i = 0; i < vchoco.length; i++)
			vl[i] = vchoco[i];
		for (int i = vchoco.length; i < l; i++)
			vl[i] = a[i - vchoco.length];

		solver.set(ISF.custom(ISF.lexico_var_selector(), ISF.min_value_selector(), vl));
		if (solver.findSolution()) {
			/*
			 * for (int i = 0; i < Q; i++) { for (int j = 0; j < R; j++) {
			 * System.out.print(a[i * R + j].getValue() + " "); }
			 * System.out.println(""); }
			 */

			for (int i = 0; i < n; i++)
				v[i] = vchoco[i].getValue();
		} else
			System.out.println("No solution ");

		return v;
	}

	/**
	 * 
	 * 
	 * @return Anonymity vector for the current problem
	 */
	public int[] chocoLex2() {

		int[] v = new int[n];// vector de anonimicidad TAMAÑO MIN DE LOS MAX.
								// Ric;hard

		Solver solver = new Solver("anom");
		int[] upper = new int[n]; // upper[i]: upper bound of v[i]
		for (int i = 0; i < n; i++) {
			upper[i] = 0;
		}

		IntVar[] a = new IntVar[Q * R];// matriz plana
		int minLocal;
		for (int i = 0; i < Q; i++) {
			for (int j = 0; j < R; j++) {
				minLocal = Math.min(f[i], c[j]);

				a[i * R + j] = VariableFactory.enumerated("a" + i + "_" + j, 0, minLocal, solver);
				// the values 1,2,...nimLocal have another possible member
				for (int k = 0; k < minLocal; k++)
					upper[k]++;
			}
		}

		IntVar[] fila = null;
		IntVar[] columna = null;
		// C1
		for (int i = 0; i < Q; i++) {
			fila = new IntVar[R];
			for (int j = 0; j < R; j++) {
				fila[j] = a[i * R + j];
			}

			// IntVar sum=VariableFactory.enumerated(qf[i+1]+"", qf[i+1],
			// qf[i+1], solver);//TRUCO?
			IntVar sum = VariableFactory.fixed(f[i], solver);
			solver.post(IntConstraintFactory.sum(fila, sum));
		}
		// C2
		for (int i = 0; i < R; i++) {
			columna = new IntVar[Q];
			for (int j = 0; j < Q; j++) {

				columna[j] = a[i + j * R];
			}
			// IntVar sum1=VariableFactory.enumerated(rc[i+1]+"", rc[i+1],
			// rc[i+1], solver);
			IntVar sum1 = VariableFactory.fixed(c[i], solver);
			solver.post(IntConstraintFactory.sum(columna, "<=", sum1));
		}

		// C3
		// real maximum for each value
		for (int i = 0; i < n; i++)
			upper[i] = Math.min(n / (i + 1), upper[i]);
		// size of the v (plus 1)
		int size = n - 1;
		while (upper[size] == 0)
			size--;
		size++;

		int[] weight = new int[size];
		IntVar[] vchoco = new IntVar[size];
		IntVar[] weightByValue = new IntVar[size];
		for (int i = 0; i < size; i++) {
			vchoco[i] = VariableFactory.enumerated("v" + (i + 1), 0, upper[i], solver);
			weightByValue[i] = VariableFactory.enumerated("wbyvalue" + (i + 1), 0, n, solver);
		}
		for (int i = 0; i < size; i++)
			weight[i] = i + 1;
		for (int i = 0; i < size; i++)
			solver.post(IntConstraintFactory.times(vchoco[i], i + 1, weightByValue[i]));

		solver.post(IntConstraintFactory.global_cardinality(a, weight, vchoco, false));

		// C4: vchoco is a valid anonymity vector
		IntVar ps = VariableFactory.fixed(n, solver);
		solver.post(IntConstraintFactory.sum(weightByValue, ps));

		// join all te variables
		int l = vchoco.length + a.length;
		IntVar[] vl = new IntVar[l];
		for (int i = 0; i < vchoco.length; i++)
			vl[i] = vchoco[i];
		for (int i = vchoco.length; i < l; i++)
			vl[i] = a[i - vchoco.length];

		solver.set(ISF.custom(ISF.lexico_var_selector(), ISF.min_value_selector(), vl));
		if (solver.findSolution()) {
			/*
			 * for (int i = 0; i < Q; i++) { for (int j = 0; j < R; j++) {
			 * System.out.print(a[i * R + j].getValue() + " "); }
			 * System.out.println(""); }
			 */

			for (int i = 0; i < n; i++)
				v[i] = i < size ? vchoco[i].getValue() : 0;
		} else
			System.out.println("No solution ");

		return v;
	}

}
