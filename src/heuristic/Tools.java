package heuristic;

import java.math.BigInteger;
import java.util.Arrays;
import tuples.*;

/**
 * Different methods to deal with anonymity vectors
 * 
 * @author rafael
 *
 */
public class Tools {

	// compares two vectors, right zeros are not taken into account
	public static boolean compare(int a[], int b[]) {
		int lb = getLength(b);
		int la = getLength(a);
		boolean result = la == lb;
		int i = 0;
		while (result && i < la) {
			result = a[i] == b[i];
			i++;
		}
		return result;
	}

	/**
	 * 
	 * @param a
	 *            anonymity vector
	 * @return Length >=0, without considering right-hand side zeroes
	 */
	public static int getLength(int a[]) {
		int l = a.length;
		while (l > 0 && a[l - 1] == 0)
			l--;
		return l;
	}

	/**
	 * @param v
	 *            anonymity vector
	 * @return population represented by the vector:
	 *         v[0]*1+....v[v.length-1]*v.length
	 */
	public static int getN(int v[]) {
		int l = getLength(v);
		int r = 0;
		for (int i = 0; i < l; i++)
			r += v[i] * (i + 1);
		return r;
	}

	/**
	 * checks if a is a correct anonymity vector for population n
	 * 
	 * @param n
	 *            total population
	 * @param an
	 *            anonymity vector
	 * @return true if sum(i=0...a.length)(i*a[i])=n, false otherwise
	 */
	public static boolean correct(int n, int[] a) {
		int la = a.length;
		int suma = 0;
		for (int i = 0; i < la; i++)
			suma += a[i] * (i + 1);
		return suma == n;
	}

	/**
	 * Pretty print of the anonymity vector in Java array format
	 * 
	 * @param v
	 */
	public static void printV(int[] v) {
		int end;
		for (end = v.length - 1; end >= 0 && v[end] == 0; end--)
			;
		if (end > -1)
			System.out.print("{");
		end++;
		for (int i = 0; i < end; i++)
			System.out.print(v[i] + (i == end - 1 ? "} " : ","));
		System.out.println("");

	}

	public static BigInteger improvement(BigInteger v1, BigInteger v2, 
			                             BigInteger base, int factor) {
		BigInteger result = v1.subtract(v2).multiply(new BigInteger((1000)+"")).divide(base);
		return result;
	}
	/**
	 * @return anonymity vector based on the frequency vector
	 */
	public static int[] base(int n, int f[]) {
		int[] v = new int[n];
		Arrays.fill(v, 0); // innecesario, pero una buena práctica
		for (int i = 0; i < f.length; i++)
			v[f[i] - 1]++;

		return v;
	}

	/**
	 * Position of anonymity vector start
	 * 
	 * @param n
	 *            population size
	 * @param remain
	 *            number of elements
	 * @param iresult
	 *            counts the number of elements
	 * @param pos
	 * @param a
	 * @param start
	 * @param show
	 * @return
	 */
	private static SearchResult pos(int n, int remain, SearchResult iresult, int pos, int a[], int start[],
			boolean show, boolean sum, int b[]) {

		int weight = pos + 1;
		int newRemain;

		// becomes true when start is found and again false when end is found
		if (!iresult.found && remain >= weight) {
			int maxv = remain / weight;
			// try all the possible values at this position
			for (int i = maxv; i >= 0 && !iresult.found; i--) {
				a[pos] = i;
				newRemain = remain - i * weight;
				if (newRemain == 0) {// a new anom.vector has been found
					iresult.count++;
					if (show) {
						// System.out.print("Found the "+iresult.count+"th
						// vector:");
						for (int k = 0; k <= pos; k++)
							System.out.print(a[k] + " ");

						System.out.println("");
					}
					// checking the number of non-zero elements at each position
					if (sum)
						for (int k = 0; k < b.length; k++)
							if (a[k] != 0)
								b[k]++;

				}

				for (int k = pos + 1; k < a.length; k++)
					a[k] = 0;
				if (!compare(a, start) && pos != n - 1)
					iresult = pos(n, newRemain, iresult, pos + 1, a, start, show, sum, b);
				else
					iresult.found = true;
			}
		}
		return iresult;
	}

	public static void enumerate(int n) {
		int last[] = new int[n];
		int a[] = new int[n];
		for (int i = 0; i < n - 1; i++)
			last[i] = 0;
		last[n - 1] = 1;
		SearchResult sBase = new SearchResult();
		pos(n, n, sBase, 0, a, last, true, false, null);
	}

	/**
	 * Greater anonymity vector of size n
	 * 
	 * @param n
	 *            population
	 * @return the vector (0,...,1)
	 */
	public static int[] last(int n) {
		int last[] = new int[n];
		for (int i = 0; i < n - 1; i++)
			last[i] = 0;
		last[n - 1] = 1;
		return last;
	}

	/**
	 * Medium size anonymity vector of size n
	 * 
	 * @param n
	 *            population
	 * @return the vector (0,...,2)
	 */
	public static int[] medium(int n) {
		final int m = n / 2;
		int medium[] = new int[m];
		for (int i = 0; i < medium.length - 1; i++)
			medium[i] = 0;
		medium[m - 1] = 2;
		if (2 * medium.length < n)
			medium[0] = 1;
		for (int i = 0; i < medium.length; i++)
			System.out.print(medium[i] + " ");
		System.out.println();
		return medium;
	}

	/**
	 * Least anonymity vector of size n
	 * 
	 * @param n
	 *            population
	 * @return vector (n)
	 */
	public static int[] first(int n) {
		int first[] = new int[1];
		first[0] = n;
		return first;
	}

	public static void checkPosition(int n) {
		SearchResult sLast = new SearchResult();
		// SearchResult sMedium = new SearchResult();
		// SearchResult sFirst = new SearchResult();
		int last[] = last(n);
		// int medium[] = medium(n);
		// int first[] = first(n);
		int a[] = new int[n];
		int b[] = new int[n];
		SearchResult rLast = pos(n, n, sLast, 0, a, last, true, false, b);
		System.out.println();
		// a = new int[n];
		// SearchResult rMedium = pos(n, n, sMedium, 0, a, medium, true);
		// a = new int[n];
		// SearchResult rFirst = pos(n, n, sFirst, 0, a, first, false);
		// System.out.println(rFirst.count);
		// System.out.println(rMedium.count);
		// System.out.println(rLast.count);
	}
	
	public static int[] getV(int a[][], int n, int Q, int R) {
		int v[] = new int[n];
		for (int i=0;i<Q; i++)
			for (int j=0;j<R; j++)
				if (a[i][j]!=0)
				   v[a[i][j]-1]++;
		return v;
	}
}
