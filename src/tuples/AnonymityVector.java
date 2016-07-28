package tuples;

public class AnonymityVector { 
	// compares two vectors, right zeros are not taken into account
	public static boolean compare(int a[],int b[]) {
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
	 * @param a anonymity vector
	 * @return Length >=0, without considering right-hand side zeroes
	 */
	private static int getLength(int a[]) {
		int l = a.length;
		while(l>0 && a[l-1]==0) 
			l--;
		return l;
		
	}
	

	// number of tuples of length m that can represent a population of
	// size n as an anonymous tuple
	/**
	 * @param n  total population >=0
	 * @param m   vector size >= 1
	 * @return total number of anonymity vectors a that can contain n, with size(a)<=m
	 */
	public static long s(int n, int m) {
		long res = 0L;
		if (n == 0)
			res =  0L; // no anonymity vectors for population= 0
		else if (m == 1)
			res = 1L; // value n at the first position; just one possibility
		else {			
			res = n % m == 0 ? 1L : 0L;
			for (int i = 0; i <= (n / m); i++)
				res += s(n - i * m, m - 1); // the second parameter always decreases
		}
		return res;
	}

	
	/**
	 * Number of anonymity vectors for a given population
	 * @param n, must be n>0
	 * @return
	 */
	public static long nAnom(int n) { 
		long s = s(n,n);			
		return s;
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
	 * Distance among anonymity vectors
	 * @param n population
	 * @param base base anonymity vector, before adding appointment 
	 * @param anom1 First anonymity vector with appointment
	 * @param anom2 Second anonymity vector with appointment
	 * @return
	 */
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

			sBase = pos(n, n, sBase, 0, a, base,false);
			sAnom1 = pos(n, n, sAnom1, 0, a, anom1,false);
			sAnom2 = pos(n, n, sAnom2, 0, a, anom2,false);
			long total = s(n, n);
			double dist1 = (sAnom1.count * 1.0) / (sBase.count - 1L);
			double dist2 = (sAnom2.count * 1.0) / (sBase.count - 1L);
			resultado = dist2 - dist1;
		}
		return resultado;
	}

	private static SearchResult pos(int n, int remain, SearchResult iresult, int pos, int a[], int start[],boolean show) {

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
                                            for (int k=0; k<=pos; k++) 				
                                                    System.out.print(a[k]+" ");
                                            System.out.println("");
					}
				}
				for (int k=pos+1; k<a.length; k++) a[k] =0;
				if (!compare(a,  start) && pos != n - 1)
					iresult = pos(n, newRemain, iresult, pos + 1, a, start, show);
				else
					iresult.found = true;
			}
		}
		return iresult;
	}
	
	public static void enumerate(int n) {
		int last[] = new int[n];
		int a[] = new int[n];
		for (int i=0; i<n-1; i++)
			last[i]  = 0;
		last[n-1]=1; 
		SearchResult sBase = new SearchResult();
		pos(n, n, sBase, 0, a, last,true);
	}
	
	public static void test() {
	 final int n=4;
		SearchResult sBase = new SearchResult();
		SearchResult sAnom1 = new SearchResult();
		SearchResult sAnom2 = new SearchResult();

		int []a = new int[n];
		int []base = {0,2,0,0};
		int []anom1 = {1,0,1,0};
		int []anom2 = {2,1,0,0};
		
		sBase = pos(n, n, sBase, 0, a, base,false);
		sAnom1 = pos(n, n, sAnom1, 0, a, anom1,false);
		sAnom2 = pos(n, n, sAnom2, 0, a, anom2,false);
		long total = s(n, n);
		double dist1 = (sAnom1.count * 1.0) / (sBase.count - 1L);
		double dist2 = (sAnom2.count * 1.0) / (sBase.count - 1L);
		double resultado = dist2 - dist1;
        System.out.println(resultado);

	}

  public static void main(String []args){
	//for (int i=0; i<1000; i++)
         //   System.out.println("i: "+ i+ "-> "+ AnonymityVector.nAnom(i));
        //enumerate(10);
      test();
  }
}
