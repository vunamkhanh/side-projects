import java.math.*;
import java.util.*;
import java.io.*;

/* Input command with the following parameters -n -k -c -l -n2 -k2 -c2 input */

public class Project
{
    // Define cache parameters and user input interface
    private int setSize = 0; // Number of blocks in a set
    private int setNo = 0; // Number of sets in the cache
    private int blockSize = 64; // Size of each block in byte
    private int cacheSize = 0; //Size of the cache in KB
    private int victimSize = 0; // Number of entries in the victim cache
    private int hasBuffer = 0; // Input 1 for a stream buffer with 1 entry
    private File inputFile = null; // Trace file to be read
    private boolean gotInput = false;;

    private int log2 (int x) {
		return (int)(Math.log(x)/Math.log(2));
	}

    public void parameters (String[] args) {
		int index = 0;
		boolean gotInput = false;

		while(index < args.length) {
			if(args[index].equalsIgnoreCase("-n")) {
				setNo = Integer.parseInt(args[index+1]);
				index = index+2;
			}
			else if(args[index].equalsIgnoreCase("-k")) {
				setSize = Integer.parseInt(args[index+1]);
				index = index+2;
			}
			else if(args[index].equalsIgnoreCase("-c")) {
                cacheSize = Integer.parseInt(args[index+1]);
                index = index+2;
			}
			else if(args[index].equalsIgnoreCase("-l")) {
                blockSize = Integer.parseInt(args[index+1]);
                index = index+2;
			}
			else if(args[index].equalsIgnoreCase("-v")) {
                victimSize = Integer.parseInt(args[index+1]);
                index = index+2;
			}
			else if(args[index].equalsIgnoreCase("-b")) {
                hasBuffer = Integer.parseInt(args[index+1]);
                index = index+2;
			}
			else if(args[index].endsWith(".txt")) {
				try {
					inputFile = new File(args[index]);
					gotInput = true;
					index++;
	   			}
	   			catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
                System.err.println("unknown argument: " + args[index]);
				System.exit(1);
			}
		}
		if (cacheSize == 0) {
            System.out.println("cache size missing");
            System.exit(1);
		}
		else if(setSize == 0) {
		    setSize = (cacheSize * 1024 / blockSize) / setNo;
		}
		else if (setNo == 0) {
            setNo = (cacheSize * 1024 / blockSize) / setSize;
		}

		if(!gotInput) {
			System.exit(1);
		}
	}

    // Find a random number within a range
    /*public int random (int min, int max) {
      Random r = new Random();
      int rand = r.nextInt(max-min+1) + min;
      return rand;
    }*/

    // Run the simulation
    public void test() {
        try {
            // Values for result analysis
            int hitCount = 0;
            int missCount = 0;
            int accessCount = 0;
            double missRatio = 0;
            double amat = 0;

            // Initialize the cache structure, 2 array elements are for set index and the tag
            // Set Index will go from 0 to setNo - 1, store set index in cache[id][0], and
            // memory tag in the other elements
            long[][] cache = new long[setNo][setSize+1];
            for (int id = 0; id < setNo; id++) {
                cache[id][0] = id;
            }

            // Initialize the victim cache
            long[] victim = new long[victimSize];

            // Initialize the stream buffer
            long buffer = 0;

            // Temporary value for LRU evaluations and size of arrays
            long temp = 0;
            int l = 0;

            // Read from input file
            // All hit and miss counts will occur within this loop
            Scanner file = new Scanner(inputFile);
            while(file.hasNext())
            {
               // Getting the required information from every line
               String line = file.nextLine();
               String[] tmp = line.split(" ");
               int ins = Integer.parseInt(tmp[0]);
               long addr = Long.decode(tmp[1]);

               // Remove set index and block offset from address to get the tag
               long tag = addr >> ( log2(blockSize) + log2(setNo) );
               // Delete the tag and block offset from address to get set index
               long setId = (addr - (tag << ( log2(blockSize) + log2(setNo) ))) >> log2(blockSize);

               // Start hit and miss counting
               if ((ins==0) | (ins==1) | (ins==2)) {
                    accessCount++;
                    outerloop:
                    for (int i = 0; i < setNo; i++) {
                        if (setId == cache[i][0]) {
                            // Search the whole set for the tag
                            for (int j = 1; j < setSize + 1; j++){
                                if (tag == cache[i][j]){
                                    hitCount++;
                                    // LRU scheme for eviction, newer tags are placed in higher indexes
                                    temp = cache[i][j];
                                    l = cache[i].length;
                                    for (int k = j; k < (l-1); k++) {
                                        cache[i][k] = cache[i][k+1];
                                    }
                                    cache[i][l-1] = temp;
                                    break outerloop;
                                }
                            }

                            // Victim cache
                            if (victimSize != 0) {
                                for (int j = 0; j < victimSize; j++){
                                    if (victim[j] == tag) {
                                        hitCount++;

                                        // Swap a block in the cache with the one in the victim cache
                                        victim[j] = cache[i][1];
                                        cache[i][1] = tag;

                                        //LRU scheme for victim cache on replacement
                                        temp = victim[j];
                                        l = victim.length;
                                        for (int k = j; k < (l-1); k++) {
                                            victim[k] = victim[k+1];
                                        }
                                        victim[l-1] = temp;

                                        // LRU scheme for the cache once the block is swapped
                                        temp = cache[i][1];
                                        l = cache[i].length;
                                        for (int k = 1; k < (l-1); k++) {
                                            cache[i][k] = cache[i][k+1];
                                        }
                                        cache[i][l-1] = temp;
                                        break outerloop;
                                    }
                                }
                                victim[1] = tag;
                                        // LRU scheme for victim cache on miss
                                        temp = victim[1];
                                        l = victim.length;
                                        for(int k = 1; k < (l-1); k++) {
                                            victim[k] = victim[k+1];
                                        }
                                        victim[l-1] = temp;
                            }

                            // Stream buffer
                            if (hasBuffer == 1){
                                if (tag == buffer) {
                                    hitCount++;
                                    break outerloop;
                                }
                                else{
                                    buffer = tag+1;
                                }
                            }

                            missCount++;

                            // Evict the least recently used block in the front
                            cache[i][1] = tag;

                            // LRU scheme for eviction, new tags are placed in higher indexes
                            temp = cache[i][1];
                            l = cache[i].length;
                            for (int k = 1; k < (l-1); k++) {
                                cache[i][k] = cache[i][k+1];
                            }
                            cache[i][l-1] = temp;
                        }
                    }
               }
            }
            file.close();

            // Print out results
            System.out.println("Cache size is " + cacheSize);
            System.out.println("Set size is " + setSize);
            System.out.println("Number of sets is " + setNo);
            if (victimSize != 0){
                System.out.println("Victim cache size is " + victimSize);
            }
            if (hasBuffer == 1) {
                System.out.print("Buffer cache size is 1");
            }
            System.out.println();

            missRatio = missCount * 1.0 / accessCount;
            amat = 2 + missRatio * 20;

            System.out.println("Access count is " + accessCount);
            System.out.println("Miss count is " + missCount);
            System.out.println("Hit count is " + hitCount);
            System.out.println("Miss rate is " + missRatio);
            System.out.println("AMAT is " + amat + "ns");
            System.out.println();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Project c = new Project();
        c.parameters(args);
        c.test();
    }

}
