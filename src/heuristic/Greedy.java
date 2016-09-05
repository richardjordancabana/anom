package heuristic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Greedy extends Appointment {

	@Override
	public int[] generate(int n, int Q, int[] f, int R, int[] c) {
		
		int []v = new int[n];
		int [][]a = new int[Q][R];
		
		
		// First sort f in descending order
		// int [] to Integer[] in Java 8
		List<Integer>  li =  Arrays.stream( f ).boxed().collect( Collectors.toList() );


		// sort in descending order
		Collections.sort(li);
		Collections.reverse(li);
		// back to array
		Integer[] ff = li.toArray(new Integer[li.size()]);
		
		// for every quasi-id value
		boolean b = true;
		for(int i=0; b && i<Q; i++) {
			// find slots
			
			 b = findSlots(i,ff,c,a);
		}

		v = Tools.getV(a, n, Q, R);
		
		/*
		for (int i = 0; i < Q; i++) {
            for (int j = 0; j < R; j++) {
                System.out.print(a[i][j]+ " ");
            }
            System.out.println("");
		}
        */
		return v;

	}



	public static boolean findSlots(int ipos, Integer[]f, int[] c, int [][]a){
		boolean b=true;
		int q = f[ipos];
		int maxc = IntStream.range(0, c.length).map(i -> c[i]).max().getAsInt();


		boolean []selected = new boolean[c.length]; // characteristic function indicating if a resource has been selected
		int []iselected = new int[c.length]; // characteristic function indicating if a resource has been selected
		int nfound = 0; // number of elements found so far
        int sofar = 0;
		
        // Phase 1: get the maximum values that can cover q
        while (nfound < c.length && sofar < q ) {
			// look for the greater element in cc
			int max = -1;
			int imax  = -1;
			for (int j=0; j<c.length; j++) {
				if (c[j]>max && !selected[j]) {
					max = c[j];
					imax = j;
				}
			}
				
			if (imax==-1) {
				System.out.println("Error: max index -1");
				
			} else {
			sofar += c[imax];
			selected[imax] = true;
			iselected[nfound] = imax;
			nfound++;
			}
		}
			
		if (nfound==c.length && sofar < q) {
			System.out.println("Error: not possible to find slots for "+q+" values");
			b = false;
		} else {
			// decrease the capacity and write-down the appointment
			boolean []sel = new boolean[nfound]; // to mark the already selected resource indices
			for (int i=0; i<nfound && q > 0; i++) {
				// look for the smallest (non-zero)
				int imin = -1;
				int min = maxc+1;
				for (int j=0; j<nfound; j++) {
					if (c[iselected[j]]<min && c[iselected[j]]!=0 && !sel[j]) {
						min = c[iselected[j]];
						imin = j;
					}
				}
				// decrease the selected capacity and write-down the appointment
				
				// check if the capacity must be decreased to zero
				if (c[iselected[imin]]>q ) {
					// the minimum covers the remaining population...then it is better to take the greatest value
					// to get the greatest remaining capacity
					int max = 0;
					int imax  = -1;
					for (int j=0; j<nfound; j++) {
						if (c[iselected[j]]>max &&  !sel[j]) {
							max = c[iselected[j]];
							imax = j;
						}
					}
					if (imax==-1)
						System.out.println("Error geting the max. value that covers value "+q);
					else {
					c[iselected[imax]] -= q ;
					a[ipos][iselected[imax]] = q;
					q = 0;
					sel[imax] = true;
					}
				} else {
					sel[imin] = true;
					q -= c[iselected[imin]];
					a[ipos][iselected[imin]] = c[iselected[imin]];
					c[iselected[imin]] = 0;				
				}
			}
		}
		
		return b;
		
	}
}
