package anom;

import heuristic.Greedy;
import java.util.Arrays;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import tuples.Tuple;
import tuples.AnonymityVector;
import java.util.Random;

public class Main {
    final class MyResult {
    private final int[] f;
    private final int[] c;

    public MyResult(int[] first, int[] second) {
        this.f = first;
        this.c = second;
    }

    public int[] getFirst() {
        return f;
    }

    public int[] getSecond() {
        return c;
    }
}
    
    //choco, restricciones léxico y pesos
    public static int [] anom(int n, int Q, int []f, int R, int[] c ) {	
	int max1=0;
        for(int i=0; i<Q;i++)
            if(f[i]>max1)
                max1=f[i];//frec maxima
        int max2=0;
        for(int i=0; i<R;i++)
            if(c[i]>max2)
                max2=c[i];//recuso maximo  
        int max=max2;
        if (max1<max2)
                max=max1;
        int p=0;
        for(int i=0; i<Q;i++)
           p=p+f[i]; //poblacion total
     
        int []v= new int[p];//vector de anonimicidad TAMAÑO MIN DE LOS MAX. Ric;hard
 
        Solver solver = new Solver("anom");
            
        IntVar[] a = new IntVar[Q * R];//matriz plana
        int minLocal; 
        for (int i = 0; i < Q; i++) {
            for (int j = 0; j < R; j++) {
                
                if(c[j]>f[i]) 
                    minLocal=f[i];
                else minLocal=c[j];
                a[i * R + j] = VariableFactory.enumerated("a" + i + "_" + j, 0, minLocal, solver);
            }
         }

        
        IntVar[] fila = null;
        IntVar[] columna = null;
        //C1
         for (int i = 0; i < Q; i++) {
             fila = new IntVar[R];
            for (int j = 0; j < R; j++) {
                fila[j]=a[i*R+j];
            }
            
           // IntVar sum=VariableFactory.enumerated(qf[i+1]+"", qf[i+1], qf[i+1], solver);//TRUCO?
            IntVar sum=VariableFactory.fixed(f[i],solver);
            solver.post(IntConstraintFactory.sum(fila,sum));
         }
         //C2
         for (int i = 0; i < R; i++) {
             columna = new IntVar[Q];
            for (int j = 0; j < Q; j++) {
                
                columna[j]=a[i+j*R];
            }
          //  IntVar sum1=VariableFactory.enumerated(rc[i+1]+"", rc[i+1], rc[i+1], solver);
            IntVar sum1=VariableFactory.fixed(c[i],solver);
            solver.post(IntConstraintFactory.sum(columna,"<=",sum1));
         }
         
         int []weight = new int[p];
         IntVar []vchoco=new IntVar[p];
         IntVar []weightByValue=new IntVar[p];
         for (int i=0; i<p; i++) {
         	vchoco[i] = VariableFactory.enumerated("v" + (i+1), 0, p/(i+1), solver);
         	weightByValue[i] = VariableFactory.enumerated("wbyvalue"+(i+1),0,p,solver);
         }
         for (int i=0; i<p; i++) weight[i] = i+1;
         for (int i=0; i<p; i++) 
         	solver.post(IntConstraintFactory.times(vchoco[i],i+1,weightByValue[i]));
         
         // C3
         solver.post(IntConstraintFactory.global_cardinality(a,weight,vchoco,false));

         // C4: vchoco es un vector de anonimato válido
         IntVar ps = VariableFactory.fixed(p, solver);        
         solver.post(IntConstraintFactory.sum(weightByValue, ps));

         System.out.println("Al find solution!");
         int l = vchoco.length + a.length;
         IntVar[] vl = new IntVar[l];
         for (int i=0; i<vchoco.length; i++)
        	 vl[i]=vchoco[i];
         for (int i=vchoco.length; i<l; i++)
        	 vl[i]=a[i-vchoco.length];
         

         solver.set(ISF.custom(ISF.lexico_var_selector(), ISF.min_value_selector(), vl));
         if(solver.findSolution()){
             for (int i = 0; i < Q; i++) {
                 for (int j = 0; j < R; j++) {
                     System.out.print(a[i * R + j].getValue()+ " ");
                 }
                 System.out.println("");
              }
             	 
           for (int i=0; i<p; i++)
        	   v[i] = vchoco[i].getValue();
         } else System.out.println("No solution ");
         
         return v;
	}
	
//METODOS PARA EL VORAZ	
	// busca hueco pero no modifica
	// devuelve el recurso, o -1 si no encuentra hueco
    public static int huecoindividual(int cantidad, int R,  int[] c ) {
	int position=-1;
	int difference=0;
	for(int j=0; j<R; j++) {
            if (c[j]>=cantidad) {
                if (position==-1) {
                    position=j;
                    difference = c[j]-cantidad;
                } else {
                    if (c[j]-cantidad<difference) {
			difference = c[j]-cantidad;
                        position = j;
                        }
		}
            }
        }
	return position;
    }
    
    public static boolean buscaHueco( int trozos, int i, int []f, int R,  int[] c, int [][]a){
	boolean found = true;
	int tamanno = f[i]/trozos;
	int []trozo = new int[trozos];
	int []position = new int[trozos];
	for (int j=0; j<trozos; j++)
            trozo[j] = tamanno;
        trozo[0]+=f[i] % trozos;
        for (int j=0; j<trozos && found; j++) {
            position[j] = huecoindividual(trozo[j], R,  c );
            if (position[j]==-1){
                found=false;
                for (int z=0; z<j; z++)
                    c[z] += trozo[z];	
            } else {
                c[position[j]] -= trozo[j];   			
            }
        }	
        if (found)
            for (int j=0; j<trozos; j++) {
                a[i][position[j]] = trozo[j]; 
            }	
        return found;
    }
	//MÉTODO DEVORADOR
    public static int [] anom2(int n, int Q, int []f, int R, int[] c ) {
        int []v = new int[n];
        int [][]a = new int[Q][R];
        for(int i=0; i<Q; i++) {
            for (int j=0; j<R; j++) {
                 a[i][j]=0;				
            }
        }
        for(int i=0; i<Q; i++) {
            // buscar el hueco que mejor se ajuste
            boolean b = true;
            for (int j=1; j<=f[i]; j++) {
                b = buscaHueco(j,i,f,R,c,a);
                if (b)
                    break;
            }
        }
        for (int i=0; i<n; i++) 
                v[i]=0;
        for (int i=0; i<n; i++) {
            for (int j=0;j<Q;j++)
                for (int k=0;k<R;k++) 
                    if (a[j][k]==i+1)
                        v[i]++;
        }
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
	
    
    
    public static int[] anomRandom(int n, int Q, int []f, int R, int[] c ){
        int[] recursos=Arrays.copyOf(c, R);
        int aux;
        int[]v=new int[n+1];
        for (int k=0;k<n+1;k++)
                    v[k]=0;
        Random r =new Random();
        for(int i=0;i<Q;i++){
            int cont=f[i];
            int[] qi=new int[R];
            for (int k=0;k<R;k++)
                    qi[k]=0;
            while(cont!=0){
                aux=r.nextInt(R);
                while(recursos[aux]==0)
                    aux=(aux+1) % (R);
                recursos[aux]--;
                qi[aux]++;
                cont--;       
            }
            for (int k=0;k<R;k++)
                    v[qi[k]]++;
            
        }
        for (int k=0;k<n;k++)
                    v[k]=v[k+1];
        v[n]=0;
        
        return v;
        
    }
	/**
	 * Pretty print of the anonymity vector in Java array format
	 * @param v
	 */
    public static void printV(int []v) {
        int end;
        for (end=v.length-1; end>=0 && v[end]==0; end--);
            if (end>-1) System.out.print("{");
        for (int i=0; i<end;i++)
            System.out.print(v[i]+(i==end-1?"} ":","));
        System.out.println("");
    }
    public static void test1() {
        int n = 30;
        int Q = 11; 
        int []f = {1,2,3,2,2,4,4,4,1,3,4};
        int R = 10;
        int[] c = {4,4,3,4,3,3,3,4,4,3};


        int[] v = anom(n,Q,f,R,c);

        for (int i=0; i<v.length;i++)
                 System.out.print(v[i]+" ");
          System.out.println("");

        int []constraint ={ 9, 4, 3, 1}; 
        int []random ={24,3};
        int []base =  {2,3,2,4};
        System.out.println(Arrays.toString(v));
        System.out.println(Arrays.toString(constraint));
        System.out.println(Tuple.distance(n, base, random, constraint));
                // 0.9977827050997783
    }

    public static void test2() {
            int n = 30;
            int Q = 12; 
            int []f = {2,2,2,4,2,3,3,4,3,2,1,2};
            int R = 10;
            int[] c = {3,3,3,4,4,3,3,4,4,3};

/*		
            int[] v = anom(n,Q,f,R,c);

            for (int i=0; i<v.length;i++)
                     System.out.print(v[i]+" ");
    System.out.println("");

    int []constraint ={ 1, 6, 3, 2}; 
    int []random ={30};
            int []base =  {1,6,3,2};
            System.out.println(Tuple.distance(n, base, random, constraint));
            // 1
    */	

            int [] v = anom2(n,Q,f,R,c);

            for (int i=0; i<v.length;i++)
                     System.out.print(v[i]+" ");
            System.out.println("");

    }

    public static void test3() {
            int n = 30;
            int Q = 11; 
            int []f = {4,4,1,5,1,1,5,3,2,1,3}; // 4 1 2 2 2
            int R = 10;
            int[] c = {4,4,3,3,4,4,3,4,3,4};


            int[] v = anom2(n,Q,f,R,c);

            for (int i=0; i<v.length;i++)
                     System.out.print(v[i]+" ");
            System.out.println("");
/*
    int []constraint ={ 2, 3, 2, 4}; 
    int []random ={30};
            int []base =  {2,3,3,2};
            System.out.println(Tuple.distance(n, base, random, constraint));
            */
            // 1
    }

    public static void test4() {
        int n = 1000;
        int Q = 18; 
        int []f = {56,58,44,48,56,58,54,52,53,56,60,63,44,52,59,69,57,61};
        int R = 50;
        int []c = {20,22,21,20,21,20,20,22,22,22,21,21,22,22,21,21,22,22,20,21,20,22,20,20,20,21,22,22,22,22,21,22,21,22,22,20,20,20,20,22,22,21,21,22,20,22,21,20,22,21};
        int[] v = anom2(n,Q,f,R,c);
        printV(v);
        v = anom(n,Q,f,R,c);
        printV(v);
    }


    public static void test5() {
            int n = 10000;
            int Q = 18; 
            int []f = {504,526,551,549,574,582,530,574,587,565,565,545,577,563,572,551,530,555};
            int R = 500;
            int []c = {21,20,22,20,22,21,22,21,22,22,21,20,21,21,22,22,20,20,20,22,22,20,20,22,20,21,22,22,22,20,21,21,21,21,22,21,22,21,22,22,20,21,20,21,22,20,21,20,20,21,20,20,22,21,22,22,22,22,22,22,20,21,21,22,22,20,20,22,21,20,22,22,22,22,20,21,20,22,22,20,21,22,21,20,21,21,20,21,20,20,20,21,21,21,20,21,22,21,20,20,22,20,20,21,20,20,20,20,20,22,21,21,22,22,21,21,20,21,22,21,21,22,22,20,21,20,21,20,22,22,21,21,22,21,22,20,22,20,20,22,22,22,21,22,21,22,20,21,21,22,20,20,22,20,20,21,20,22,22,20,21,21,21,21,20,21,21,22,21,22,22,21,21,21,21,20,21,22,22,22,22,22,20,21,22,21,20,20,21,20,20,20,21,22,21,21,21,20,21,22,20,21,21,22,22,20,22,20,22,21,20,21,21,21,20,20,22,20,20,20,20,21,20,22,20,22,20,21,20,21,21,20,20,20,20,22,20,22,20,22,21,20,21,22,21,21,22,21,21,20,22,22,21,20,21,22,20,22,21,20,21,20,21,21,20,21,21,20,21,21,20,22,21,21,21,20,20,20,22,22,21,21,21,20,21,20,20,21,20,21,22,21,21,20,20,21,22,21,22,20,22,20,22,21,22,20,21,22,21,20,21,20,22,20,20,21,21,20,22,21,22,22,20,20,20,20,22,22,21,22,22,21,22,22,22,22,22,21,22,21,21,21,21,21,22,22,22,20,22,22,22,22,21,22,22,21,20,22,20,22,21,20,20,21,22,22,20,22,21,22,20,22,21,22,20,20,21,20,20,21,22,20,20,20,20,20,21,21,22,22,20,20,22,20,21,22,20,22,21,21,22,22,21,20,21,22,20,20,22,21,22,21,22,20,22,22,21,20,20,20,20,21,21,20,22,20,22,20,22,20,22,22,20,20,22,22,20,20,22,20,22,22,22,21,20,22,21,21,22,22,21,20,20,22,20,22,20,22,22,22,20,21,22,20,22,22,22,20,22,22,21,22,21,21,21,21,20,22,20,20,21,21,20,22,21,20,22,21,20,21,21,20,20,21,20,20,20,22,21,20};
            int[] v = anom2(n,Q,f,R,c);
            printV(v);
            v = anom2(n,Q,f,R,c);
            printV(v);

    }
    
    //metodo dado un n genera q,f   times indica cuantas veces repite el proceso
    //opt y voraz devuelven la media de todas los resultados obtenidos en times veces
    public static void test(int n,int times, double opt,double voraz,int minq, int maxq, int minf, int maxf){
           //int n=5;
            double o=0;
            double v=0;
            double ov=0;
            for(int i =0; i< times;i++){
        
                Main m = new Main();
                MyResult a;
                /*for (int i=0; i<100;i++){
                    System.out.println("i:"+i+" ");
                    a = m.generateQF(100,5,5,true);
                }*/
                 //for (int i=0; i<100;i++)
                    a = m.generateQF(n,minq,maxq,minf,maxf,true); // a true utiliza los rangos min max
                int[] f=a.getFirst();
                int[] c=a.getSecond();
                Appointment apo = new Appointment(f, c);

                int[] vBase=getV(n,f);
                int[] vRandom=anomRandom(n,f.length,f,c.length,c);
                int[] vOptimo;
                if(n<=25)
                    vOptimo= apo.chocoLex();
                else   vOptimo= null;
                int[] vVoraz = anom2(n,f.length,f,c.length,c); 


                double x=0;
                double y=0;
                double z=0;

                vBase=cutArray(vBase);
                vRandom=cutArray(vRandom);
                if(n<=25)
                vOptimo=cutArray(vOptimo);
                vVoraz=cutArray(vVoraz);

              System.out.print("VBASE :  ");
              System.out.println(Arrays.toString(vBase)); 
              System.out.print("vRandom :  ");
              System.out.println(Arrays.toString(vRandom)); 
              System.out.print("vOptimo :  ");
              System.out.println(Arrays.toString(vOptimo)); 
              System.out.print("vVoraz :  ");
              System.out.println(Arrays.toString(vVoraz)); 
              if(n<=25){
              System.out.print(" Distancia (VBASE,VRANDOM,VOPTIMO) : ");
              x=Tuple.distance(n, vBase, vRandom, vOptimo);
              System.out.println(x);
              }
              System.out.print(" Distancia (VBASE,VRANDOM,VVORAZ) : ");
              y=Tuple.distance(n, vBase, vRandom, vVoraz);
              System.out.println(y);
              
              if(n<=25){
              System.out.print(" Distancia (VBASE,VVORAZ,VOPTIMO) : ");
              z=Tuple.distance(n, vBase, vVoraz, vOptimo);
              if (z<0)
                  System.out.println("NEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEGATIVO");
              System.out.println(z);
              }
              if(n>25){
              o=0;
              v+=y;
              ov=0;
              }else
              {
              ov+=z;    
              o+=x;
              v+=y;
              }
          
            }
            
            voraz=v/times;
            opt=o/times;
            double ovtotal= ov/times;
           System.out.print("Valor voraz :  ");
           System.out.println(voraz); 
           System.out.print("Valor optimo :  ");
           System.out.println(opt); 
           System.out.print("Valor OPTIMO/VORAZ :  ");
           System.out.println(ovtotal); 
    }
    
    public static void testHeuristico(int n,int times, double opt,double voraz,int minq, int maxq, int minf, int maxf){
           //int n=5;
            double o=0;
            double v=0;
            double ov=0;
            for(int i =0; i< times;i++){
        
                Main m = new Main();
                MyResult a;
                /*for (int i=0; i<100;i++){
                    System.out.println("i:"+i+" ");
                    a = m.generateQF(100,5,5,true);
                }*/
                 //for (int i=0; i<100;i++)
                    a = m.generateQF(n,minq,maxq,minf,maxf,true); // a true utiliza los rangos min max
                int[] f=a.getFirst();
                int[] c=a.getSecond();
                Appointment apo = new Appointment(f, c);

                int[] vBase=getV(n,f);
                int[] vRandom=anomRandom(n,f.length,f,c.length,c);
                int[] vOptimo;
                if(n<=25)
                    vOptimo= apo.chocoLex();
                else   vOptimo= null;
                Greedy g=new Greedy();
                int[] vHeuristico = g.generate(n,f.length,f,c.length,c);
                        //anom2(n,f.length,f,c.length,c); 


                double x=0;
                double y=0;
                double z=0;

                vBase=cutArray(vBase);
                vRandom=cutArray(vRandom);
                if(n<=25)
                vOptimo=cutArray(vOptimo);
                vHeuristico=cutArray(vHeuristico);

              System.out.print("VBASE :  ");
              System.out.println(Arrays.toString(vBase)); 
              System.out.print("vRandom :  ");
              System.out.println(Arrays.toString(vRandom)); 
              System.out.print("vOptimo :  ");
              System.out.println(Arrays.toString(vOptimo)); 
              System.out.print("vHeuristico :  ");
              System.out.println(Arrays.toString(vHeuristico)); 
              if(n<=25){
              System.out.print(" Distancia (VBASE,VRANDOM,VOPTIMO) : ");
              x=Tuple.distance(n, vBase, vRandom, vOptimo);
              System.out.println(x);
              }
              System.out.print(" Distancia (VBASE,VRANDOM,VHEURISTICO) : ");
              y=Tuple.distance(n, vBase, vRandom, vHeuristico);
              System.out.println(y);
              
              if(n<=25){
              System.out.print(" Distancia (VBASE,VHEURISTICO,VOPTIMO) : ");
              z=Tuple.distance(n, vBase, vHeuristico, vOptimo);
              if (z<0)
                  System.out.println("NEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEGATIVO");
              System.out.println(z);
              }
              if(n>25){
              o=0;
              v+=y;
              ov=0;
              }else
              {
              ov+=z;    
              o+=x;
              v+=y;
              }
          
            }
            
            voraz=v/times;
            opt=o/times;
            double ovtotal= ov/times;
           System.out.print("Valor heuristico :  ");
           System.out.println(voraz); 
           System.out.print("Valor optimo :  ");
           System.out.println(opt); 
           System.out.print("Valor OPTIMO/HEURISTICO :  ");
           System.out.println(ovtotal); 
    }
    
    
    public static void testK(int n,int times, double opt,double voraz,int minq, int maxq, int minf, int maxf){
           //int n=5;
            double o=0;
            double v=0;
            double r=0;
            for(int i =0; i< times;i++){
        
                Main m = new Main();
                MyResult a;
                /*for (int i=0; i<100;i++){
                    System.out.println("i:"+i+" ");
                    a = m.generateQF(100,5,5,true);
                }*/
                 //for (int i=0; i<100;i++)
                    a = m.generateQF(n,minq,maxq,minf,maxf,true); // a true utiliza los rangos min max
                int[] f=a.getFirst();
                int[] c=a.getSecond();
                Appointment apo = new Appointment(f, c);

                int[] vBase=getV(n,f);
                int[] vRandom=anomRandom(n,f.length,f,c.length,c);
                int[] vOptimo;
                if(n<=25)
                    vOptimo= apo.chocoLex();
                else   vOptimo= null;
                int[] vVoraz = anom2(n,f.length,f,c.length,c); 


                double x=0;
                double y=0;
                double z=0;

                vBase=cutArray(vBase);
                vRandom=cutArray(vRandom);
                if(n<=25)
                vOptimo=cutArray(vOptimo);
                vVoraz=cutArray(vVoraz);

              System.out.print("VBASE :  ");
              System.out.println(Arrays.toString(vBase)); 
              System.out.print("vRandom :  ");
              System.out.println(Arrays.toString(vRandom)); 
              System.out.print("vOptimo :  ");
              System.out.println(Arrays.toString(vOptimo)); 
              System.out.print("vVoraz :  ");
              System.out.println(Arrays.toString(vVoraz)); 
              if(n<=25){
              System.out.print(" KOPTIMO: ");
              x=getK(vOptimo);
              System.out.println(x);
              }
              System.out.print(" KRANDOM ");
              y=getK(vRandom);
              System.out.println(y);
              System.out.print(" KVORAZ ");
              z=getK(vVoraz);
              System.out.println(z);
              
              if(n>25){
              o=0;
              r+=y;
              v+=z;
              }else
              {
              v+=z;    
              o+=x;
              r+=y;
              }
          
            }
            
            voraz=v/times;
            opt=o/times;
            double ran= r/times;
           System.out.print("KRANDOM :  ");
           System.out.println(ran); 
           System.out.print("Koptimo :  ");
           System.out.println(opt); 
           System.out.print("Kvoraz:  ");
           System.out.println(voraz); 
    }
    public static void testKHeuristic(int n,int times, double opt,double voraz,int minq, int maxq, int minf, int maxf){
           //int n=5;
            double o=0;
            double v=0;
            double r=0;
            for(int i =0; i< times;i++){
        
                Main m = new Main();
                MyResult a;
                /*for (int i=0; i<100;i++){
                    System.out.println("i:"+i+" ");
                    a = m.generateQF(100,5,5,true);
                }*/
                 //for (int i=0; i<100;i++)
                    a = m.generateQF(n,minq,maxq,minf,maxf,true); // a true utiliza los rangos min max
                int[] f=a.getFirst();
                int[] c=a.getSecond();
                Appointment apo = new Appointment(f, c);

                int[] vBase=getV(n,f);
                int[] vRandom=anomRandom(n,f.length,f,c.length,c);
                int[] vOptimo;
                if(n<=25)
                    vOptimo= apo.chocoLex();
                else   vOptimo= null;
                Greedy g=new Greedy();
                int[] vHeuristico = g.generate(n,f.length,f,c.length,c);


                double x=0;
                double y=0;
                double z=0;

                vBase=cutArray(vBase);
                vRandom=cutArray(vRandom);
                if(n<=25)
                vOptimo=cutArray(vOptimo);
                vHeuristico=cutArray(vHeuristico);

              System.out.print("VBASE :  ");
              System.out.println(Arrays.toString(vBase)); 
              System.out.print("vRandom :  ");
              System.out.println(Arrays.toString(vRandom)); 
              System.out.print("vOptimo :  ");
              System.out.println(Arrays.toString(vOptimo)); 
              System.out.print("vHeuristico :  ");
              System.out.println(Arrays.toString(vHeuristico)); 
              if(n<=25){
              System.out.print(" KOPTIMO: ");
              x=getK(vOptimo);
              System.out.println(x);
              }
              System.out.print(" KRANDOM ");
              y=getK(vRandom);
              System.out.println(y);
              System.out.print(" KHeuristico ");
              z=getK(vHeuristico);
              System.out.println(z);
              
              if(n>25){
              o=0;
              r+=y;
              v+=z;
              }else
              {
              v+=z;    
              o+=x;
              r+=y;
              }
          
            }
            
            voraz=v/times;
            opt=o/times;
            double ran= r/times;
           System.out.print("KRANDOM :  ");
           System.out.println(ran); 
           System.out.print("Koptimo :  ");
           System.out.println(opt); 
           System.out.print("Kheuristico:  ");
           System.out.println(voraz); 
    }
    
    
    
    public static int getK(int[] v)
    {
        int min=-1;
        int i=0;
        while(i<v.length && min==-1){
            if (v[i]!=0)
                min=i+1;
            i++;
        }
        if (min!=-1)
            return min;
        else return -1;
            
        
    }
    
    public static void testEjemplos(){
         //test1();
        // compare3(20, 1, 20, 20, 4, 5, 5,1000);
        // i n q q1 q2 r r1 r2 it
        //compare(3, 20, 1, 20, 20, 4, 5, 5,1000);
        //compare(1, 20, 2, 10, 10, 4, 5, 5,1000);
        //compare(3, 20, 5, 4, 4, 4, 5, 5, 1000);
        //compare(3, 20, 4, 5, 5, 4, 5, 5,1000);
        //compare(3, 20, 10, 2, 2, 4, 5, 5,10);
        //compare(3, 20, 12, 1, 2, 4, 5, 5,10);
        //compare(3, 20, 17, 1, 2, 4, 5, 5,1);
        //compare(3, 20, 20, 1, 1, 4, 5, 5,1);
        //compare(1, 40, 10, 4, 4, 8, 5, 5,1);
        // comparing chocoLex and ChocoIterative
        // test_n20(1000); // 0.33 sin search
        // test_n30(10); // 0.33 sin search
        // test_n40(1); // 0.48 con search
        // test_n50(10); // 0.31
        //compare(4, 3, 2, 1, 2, 2, 1, 2,1);
        //exampleChocoJacop2();
        // test_n30_2(10);
        // test_n40(100); // 0.48 con search
        // test_n40_2(100); // 0.48 con search
        // AnonymityVector.test();
           
//        
         
//            
//          
//          System.out.print(" Distancia (VBASE,VRANDOM,VOPTIMO) : ");
//          System.out.println(Tuple.distance(n, vBase, vRandom, vOptimo));
//          System.out.print(" Distancia (VBASE,VRANDOM,VVORAZ) : ");
//          System.out.println(Tuple.distance(n, vBase, vRandom, vVoraz));

         
            /*long r1= AnonymityVector.s(100, 100);
            long r2= AnonymityVector.s1(100, 100);
            System.out.println("R1: " + r1);
            System.out.println("R2: " + r2);
        */
    }
    public  static void main(String []args) {
            double res1=0;
            double res2=0;
           // testHeuristico(5,100,res1,res2,0,2,0,2);
           // testHeuristico(10,100,res1,res2,0,3,0,3);
           // testHeuristico(25,100,res1,res2,3,7,3,7);
            //testHeuristico(50,100,res1,res2,5,8,5,8);
           // testHeuristico(5,100,res1,res2,0,2,0,2);
            
            //test(100,100,res1,res2,0,20,10,20);
            testKHeuristic(10000,100,res1,res2,0,200,500,1000); // hay  que mejorar los rangos de los cuasis y de los recursos
            // para que se acerquen más a un caso real, sino se pueden desviar los resultados
            //testKHeuristic(1000,100,res1,res2,0,300,100,200);
            //testKHeuristic(100,100,res1,res2,0,30,10,20);
            //testKHeuristic(25,100,res1,res2,0,7,5,10);
            //testKHeuristic(10,100,res1,res2,0,3,3,7);
            //testKHeuristic(5,100,res1,res2,0,2,1,3);
            /*
            int[] v1={2,8};
            int[] v2={5,5};
            
             int[] vVoraz = anom2(5,v1.length,v1,v2.length,v2); 
             System.out.print("vVoraz :  ");
             System.out.println(Arrays.toString(vVoraz)); 
             */
            /*
            int[] v1={5};
            int[] v2={0,5};
            
             int[] vRandom = anomRandom(5,v1.length,v1,v2.length,v2); 
             System.out.print("vRandom :  ");
             System.out.println(Arrays.toString(vRandom)); 
             */
            return;
            
        //test1();
    }
    //elimina los ceros por la derecha
    public static int[] cutArray(int[] v)
    {
        int cont=v.length;
        boolean encontrado=false;
        while(cont>0 && !encontrado)
        {   cont--;
            if(v[cont]!=0)
               encontrado=true;
        }
        int[] res=new int[cont+1];
        for(int i=0;i<cont+1;i++)
            res[i]=v[i];
        return res;
    }
    public static int[] getV(int n, int[]q){
        int[] a= new int [n];
        for (int i =0;i<q.length;i++){
           if(q[i]-1 >= 0)
                a[q[i]-1]++;
        }
        
        return a;
    }

    //generate q and f random length    n population  topq and topf considered if range is true
    public   MyResult generateQF(int n,int minq, int maxq, int minf, int maxf, boolean range){
        int[] q;
        int[] f;
        int aux;
        int[] a=new int[n];
        int i=0;
        int contador=0;
        Random r = new Random();
        while(contador!=n){
            if(i==n-1){
                a[i]=n-contador;
                contador=contador+a[i];
                i++;
            }else{
                if(!range)
                     aux=r.nextInt(n);// m+1
                else aux=minq+r.nextInt(maxq-minq+1);
                if(contador + aux > n){
                    a[i]=n-contador;
                    contador=contador+a[i];
                } else{
                    a[i]=aux;
                    contador=contador+aux;
                }
                i++;  }
        }
        q=new int[i];
        for (int x=0; x<i;x++){
            q[x]=a[x];
        }
        i=0;
        contador=0;
        a=new int[n];
        while(contador<n){
           if(i==n-1){
                a[i]=n-contador;
                contador=contador+a[i];
                 i++;  
               
            } else{
                if(!range)
                    aux=r.nextInt(n);// m+1
                else aux=minf+r.nextInt(maxf-minf+1);
                a[i]=aux;
                contador=contador+aux;
                i++;  
            }
        } 
        f=new int[i];
        for (int x=0; x<i;x++){
            f[x]=a[x];
        }
        
          System.out.println("");
          System.out.print("q:  ");
          System.out.println(Arrays.toString(q));
          System.out.print("f:  ");
          System.out.println(Arrays.toString(f));
          
        return new MyResult(q,f);
    }
    
    
    

    public static void test6() {
            int n = 10;
            int[] f = { 4, 6 };
            int R = 3;
            int[] c = { 3, 3, 4 };

            Appointment apo = new Appointment(f, c);
            int[] v1 = apo.chocoLex();
            apo.printV(v1);
            int[] v2 = apo.chocoIterative();
            apo.printV(v2);
            int[] constraint = { 2, 3, 2, 4 };
            int[] random = { 24, 3 };
            int[] base = { 2, 3, 2, 4 };
            System.out.println(AnonymityVector.distance(n, base, random, constraint));
            // 0.9977827050997783
    }

    public static void test7() {
            int n = 30;
            int Q = 11;
            int[] f = { 1, 2, 3, 2, 2, 4, 4, 4, 1, 3, 4 };
            int R = 10;
            int[] c = { 4, 4, 3, 4, 3, 3, 3, 4, 4, 3 };

            Appointment apo = new Appointment(f, c);
            int[] v1 = apo.chocoLex();
            apo.printV(v1);
            int[] v2 = apo.chocoIterative();

            apo.printV(v2);
            /*
             * int[] constraint = { 2, 3, 2, 4 }; int[] random = { 24, 3 }; int[]
             * base = { 2, 3, 2, 4 }; System.out.println(Tuple.distance(n, base,
             * random, constraint)); // 0.9977827050997783
             *
             */
    }

    public static void test_n20(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(20, 1, 20, 20, 4, 5, 5);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);

                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n30(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(30, 8, 1, 30, 6, 4, 6);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);

                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
                    if (!AnonymityVector.compare(v1, v2))
                            System.out.println("Valores diferentes");

            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n30_2(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(30, 8, 1, 30, 6, 4, 6);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex2();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);

                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
                    if (!AnonymityVector.compare(v1, v2))
                            System.out.println("Valores diferentes");

            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n20_2(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(20, 1, 20, 20, 4, 5, 5);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex2();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);

                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);

                    if (!AnonymityVector.compare(v1, v2)) {
                            System.out.println("Valores diferentes");
                    }
            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n40(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(40, 4, 6, 12, 8, 4, 6);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);
                    System.out.print(".");
                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n40_2(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(40, 4, 6, 12, 8, 4, 6);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex2();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);
                    System.out.print(".");
                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
                    if (!AnonymityVector.compare(v1, v2))
                            System.out.println("Valores diferentes");

            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n50(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(50, 5, 8, 14, 10, 4, 6);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);
                    System.out.print("l");
                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();
                    System.out.print("i");
                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }

    public static void test_n60(int iterations) {
            long startTimev1;
            long endTimev1;
            long durationv1 = 0;
            long startTimev2;
            long endTimev2;
            long durationv2 = 0;

            for (int i = 0; i < iterations; i++) {
                    Appointment a = new Appointment(60, 5, 10, 14, 12, 4, 6);
                    startTimev1 = System.nanoTime();
                    int[] v1 = a.chocoLex();
                    endTimev1 = System.nanoTime();
                    durationv1 += (endTimev1 - startTimev1);
                    System.out.print(".");
                    startTimev2 = System.nanoTime();
                    int[] v2 = a.chocoIterative();

                    endTimev2 = System.nanoTime();
                    durationv2 += (endTimev2 - startTimev2);
            } // a.printV(v2);
                    // System.out.println(a);
            System.out.println("Time: " + ((double) durationv1) / ((double) durationv2));
            // System.out.println("Time: " + (durationv2 ));

    }



    public static void compare(int i, int n, int q, int qmin, int qmax, int r, int rmin, int rmax, int iterations) {
            long startTimev;
            long endTimev;
            long durationv = 0;
            if (i == 1) {
                    System.out.print("chocoLex2 1");
            } else if (i == 2) {
                    System.out.print("chocoLex 2");
            } else if (i == 3) {
                    System.out.print("chocoIterative 3");

            } else

                    System.out.print("param desconocido");

            for (int j = 0; j < iterations; j++) {
                    Appointment a = new Appointment(n, q, qmin, qmax, r, rmin, rmax);
                    //System.out.print(".");
                    /*
                     * Appointment a2 = new Appointment(n, q, qmin, qmax, r, rmin,
                     * rmax); Appointment a3 = new Appointment(n, q, qmin, qmax, r,
                     * rmin, rmax);
                     */

                    if (i == 1) {

                            startTimev = System.nanoTime();
                            int[] v = a.chocoLex2();
                            endTimev = System.nanoTime();
                            durationv += (endTimev - startTimev);
                    } else if (i == 2) {
                            startTimev = System.nanoTime();
                            int[] v = a.chocoLex();
                            endTimev = System.nanoTime();
                            durationv += (endTimev - startTimev);
                    } else if (i == 3) {
                            startTimev = System.nanoTime();
                            int[] v = a.chocoIterative();
                            endTimev = System.nanoTime();
                            durationv += (endTimev - startTimev);
                    } 
                    // System.out.print(".");
                    /*
                     * if (!Tuple.compare(v1, v2)) { System.out.println(
                     * "Valores diferentes"); } if (!Tuple.compare(v1, v3)) {
                     * System.out.println("Valores diferentes"); }
                     */
            } // a.printV(v2);
            System.out.println("");
            // System.out.println("Time 1/3: " + ((double) durationv1) / ((double)
            // durationv3));
            // System.out.println("Time 2/3: " + ((double) durationv2) / ((double)
            // durationv3));
            // System.out.println("Duration 1: "+((double) durationv1) /
            // iterations);
            System.out.println("Duration : " + durationv + " Ratio: " + ((double) durationv) / iterations);
            // System.out.println("Duration 3: "+((double) durationv3) /
            // iterations);
    }

 
           
    
}
