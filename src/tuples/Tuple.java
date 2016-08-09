package tuples;

public class Tuple {

	// compares two vectors, a with length la and b with length lb
	public static boolean compare(int a[], int la, int b[]) {
		int lb = b.length;
		boolean result = la == lb;
		int i = 0;
		while (result && i < la) {
			result = a[i] == b[i];
			i++;
		}
		return result;
	}

	// number of tuples of length m that can represent a population of
	// n as an anonymous tuple
	public static long s(int n, int m) {
		long res = 0L;
		if (n == 0)
			res = 0L;
		else if (m == 1)
			res = 1L;
		else {
			// z el es max de x e y z = x>y ? x : y;
			res = n % m == 0 ? 1L : 0L;
			for (int i = 0; i <= (n / m); i++)
				res += s(n - i * m, m - 1);
		}
		return res;
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

	public static double distance(int n, int base[], int anom1[], int anom2[]) {
		int[] a = new int[n];
		double resultado = -1.0;
		if (!correct(n, base)) {
			System.out.println("Error, first array non-valid!");
			resultado = -1.0;
		} else if (!correct(n, anom1)) {
			System.out.println("Error, second array non-valid");
			resultado = -1.0;
		} else if (!correct(n, anom2)) {
			System.out.println("Error, third array non-valid");
			resultado = -1.0;
		} else {
			SearchResult sBase = new SearchResult(); 
			SearchResult sAnom1 = new SearchResult();
			SearchResult sAnom2 = new SearchResult(); 

			sBase = pos(n, n, sBase, 0, a, base);
			sAnom1 = pos(n, n, sAnom1, 0, a, anom1);
			sAnom2 = pos(n, n, sAnom2, 0, a, anom2);
                        System.out.println("SBASE: "+sBase.count);
                        System.out.println("sAnom1: "+sAnom1.count);
                        System.out.println("sAnom2: "+sAnom2.count);
			long total = s(n, n);
			double dist1 = (sAnom1.count*1.0)/ (sBase.count-1L);
			double dist2 = (sAnom2.count*1.0)/ (sBase.count-1L);
			resultado = dist2 - dist1;
                       // System.out.println("DIST1: "+dist1);
                        //System.out.println("DIST2: "+dist2);
                        
		}
		return resultado;
	}

	private static SearchResult pos(int n, int remain, SearchResult iresult, int pos, int a[], int start[]) {

		int weight = pos + 1;
		int newRemain;

		// becomes true when start is found and again false when end is found
		if (!iresult.found && remain >= weight) {
			int maxv = remain / weight;
			// try all the possible values at this position
			for (int i = maxv; i >= 0 && !iresult.found; i--) {
				a[pos] = i;
				newRemain = remain - i * weight;
				if (newRemain == 0) // a new anom.vector has been found
					iresult.count++;

				if (!compare(a, pos + 1, start) && pos != n - 1) 
						iresult = pos(n, newRemain, iresult, pos + 1, a, start);
					
				else
					 iresult.found = true;
			}
		}
		return iresult;
	}

}
