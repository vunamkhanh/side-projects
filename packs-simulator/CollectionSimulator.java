import java.io.*;
import java.util.*;

// This method will read specifications of a collection from a text file
// and run simulations until the collection is completed, or there is sufficient
// crafting material to finish the rest of the collection
//
// User can manually change matrix values through user input


public class CollectionSimulator{
	
	// File: Collection_Specs.txt
	// Array for card specifications, structure is as follows (probability is relative):
	// 
	// Game name + expansion
	//            | 0.probability | 1.crafting cost | 2.disenchant value | 3.unique cards | 4.play set |
	// 0.common   |               |                 |                    |                |            |
	// 1.rare     |               |                 |                    |                |            |
	// 2.epic     |               |                 |                    |                |            |
	// 3.legendary|               |                 |                    |                |            |
	// 4.misc.    | 0.pack size   | 0               | 0                  | 0              | 0          |
	//
	// * Golden cards are not counted
	// * Only for self-contained sets
	// * Assume empty initial collection
	String name;
	int[][] specs = new int[5][5];
	
	// ArrayList to determine card rarity
	// Rarity number repeated according to appearance probability
	// 0 = common, 1 = rare, 2 = epic, 3 = legendary
	ArrayList<Integer> rarityList = new ArrayList<Integer>();
	
	// Arrays to determine the unique card: one for each rarity
	// Each card is represented by the position in the array
	// Everytime a new card appears, the count in its array position
	// will increase until the play set threshold
	int[] commonCards;
	int[] rareCards;
	int[] epicCards;
	int[] legendaryCards;
	
	// Remaining crafting cost
	int remainingCost = 0;
	
	// Current crafting bank
	int craftBank = 0;
	
	
	public static void main(String[] args){
		
		CollectionSimulator CS = new CollectionSimulator();
		
		// Run the simulator
		CS.run();
		
		// Output the completed collections for testing
		CS.printCollection();
	}
	
	
	// Running the program
	void run(){
		
		// User options for custom initial collection or wholesale completion
		int option = initialize();
		System.out.println();
		
		boolean check = true;
		
		while (check){
			
			// Custom collection
			if (option == 1){
				averageCustom();
				check = false;
			}
			
			// Full collection from empty sets
			else if (option == 2){
				averageFull();
				check = false;
			}
			
			// No appropriate option
			else{
				System.out.println("No appropriate option chosen");
				System.out.println("Please re-input 1 or 2: ");
				option = readInt();
				System.out.println();
			}
		}
				
	}
	
	
	// Initialize all the parameters of the simulator
	int initialize(){
		
		// Collect the statistics of a collection from a txt file according
		// to the specified format
		readText();
		
		// Initialize the lists and arrays used to simulate pack opening
		initializeProb();
		
		// Display the name of the game and the expansion
		System.out.println();
		System.out.println("Current collection: ");
		System.out.println(name);
		System.out.println();
		
		// Initialize the intial total cost
		remainingCost = totalCraft();
		System.out.println("Total crafting cost of all cards is: ");
		System.out.println(remainingCost);
		System.out.println();
		
		// Option for a custom collection or a full collection
		System.out.println("Would you like to run a custom collection? (1 = y; 2 = n): ");
		int option = readInt();
		
		return option;
		
	}
	
	
	// Read a double input from the user
	// Return the user input
	int readInt(){
		
		Scanner stdin = new Scanner(System.in);
				
		int tmp = 0;
		
		try{
			
			tmp = stdin.nextInt();
			while (tmp < 0){
				System.out.println("Please input a positive number: ");
				stdin = new Scanner(System.in);
				tmp = stdin.nextInt();
			}
			
		} catch (Exception e){
			System.out.println();
			System.out.println("Please input a number: ");
			stdin = new Scanner(System.in);
		}
		
		return tmp;
		
	}

	
	// Read inputs from a text file
	// Only affect an static array
	void readText(){
		
		try{
			
			Scanner scan = new Scanner(new File("Collection_Specs.txt"));
			
			// Scan the name of the game and the expansion
			name = scan.nextLine();

			int i = 0;
				
			// Scan the specs of the collection
			while (scan.hasNext()) {
				String tmp = scan.nextLine();
				String[] tmpLine = tmp.split(" ");
				
				for (int j = 0; j < tmpLine.length; j++){
					specs[i][j] = Integer.parseInt(tmpLine[j]);
				}
				i++;
			}
			
			scan.close();
			
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
	}

	
	// Initialize the probability lists for pack opening
	// Only affect static array lists and arrays
	void initializeProb(){
		
		// Initialize card rarities
		for (int i = 0; i < specs.length; i++){
			for (int j = 0; j < specs[i][0]; j++){
				rarityList.add(i);
			}
		}
		
		// Initialize array sizes
		commonCards = new int[specs[0][3]];
		rareCards = new int[specs[1][3]];
		epicCards = new int[specs[2][3]];
		legendaryCards = new int[specs[3][3]];
		
		
		// Initialize card lists
		// Common
		for (int i = 0; i < commonCards.length; i++){
			commonCards[i] = 0;
		}
		
		// Rare
		for (int i = 0; i < rareCards.length; i++){
			rareCards[i] = 0;
		}
		
		// Epic
		for (int i = 0; i < epicCards.length; i++){
			epicCards[i] = 0;
		}
		
		// Legendary
		for (int i = 0; i < legendaryCards.length; i++){
			legendaryCards[i] = 0;
		}
		
	}


	// Calculate total collection crafting cost
	int totalCraft(){
		
		int total = 	specs[0][1]*specs[0][3]*specs[0][4]
					+	specs[1][1]*specs[1][3]*specs[1][4]
					+	specs[2][1]*specs[2][3]*specs[2][4]
					+ 	specs[3][1]*specs[3][3]*specs[3][4];
		
		return total;
		
	}
	
	
	// Custom collection - test how many packs are left
	void customCollection(int common, int rare, int epic, int legendary, int bank){
		
		// Fill up the collection up until the custom amount
		int tmp = 0;
		for (int i = 0; i < common; i++){
			if (commonCards[tmp] >= specs[0][4]){
				tmp++;
			}
			commonCards[tmp]++;
		}
		tmp = 0;
		
		for (int i = 0; i < rare; i++){
			if (rareCards[tmp] >= specs[1][4]){
				tmp++;
			}
			rareCards[tmp]++;
		}
		tmp = 0;
		
		for (int i = 0; i < epic; i++){
			if (epicCards[tmp] >= specs[2][4]){
				tmp++;
			}
			epicCards[tmp]++;
		}
		tmp = 0;
		
		for (int i = 0; i < legendary; i++){
			if (legendaryCards[tmp] >= specs[3][4]){
				tmp++;
			}
			legendaryCards[tmp]++;
		}
		tmp = 0;
		
		// Set the crafting bank to the current amount
		craftBank = bank;
		
	}
	
	
	// Averaged runs for the custom collection
	void averageCustom(){
		
		// Input options for the user
		System.out.println("Owned common cards: ");
		int common = readInt();
		System.out.println("Owned rare cards: ");
		int rare = readInt();
		System.out.println("Owned epic cards: ");
		int epic = readInt();
		System.out.println("Owned legendary cards: ");
		int legendary = readInt();
		System.out.println("Current crafting material amount: ");
		int bank = readInt();
		System.out.println("Please input the number of runs to take: ");
		int runs = readInt();
		System.out.println();
		
		// Calculate the remaining crafting cost, put into a temporary variable that will not be reset
		int remCost = totalCraft()	- common*specs[0][1]
										- rare*specs[1][1]
										- epic*specs[2][1]
										- legendary*specs[3][1];
		
		System.out.println("Remaining crafting cost is: ");
		System.out.println(remCost);
		
		// Take custom runs and average the result
		double custom = 0;
		for (int i = 0; i < runs; i++){
			
			// Reset the parameters
			initializeProb();
			craftBank = 0;
			remainingCost = remCost;
			
			customCollection(common, rare, epic, legendary, bank);
			custom = custom + completion();
			
		}
		double result = custom/runs;
		
		
		
		System.out.println("Number of packs needed to complete current collection: ");
		System.out.println(result + " packs over " + runs + " runs");
		
	}
	
	
	// Pack opening simulator
	// Size of each pack will be given by the user
	// One card will be at least rare-legendary, common
	// drop rate is replaced by rare
	void packOpening(){
		
		// Size of each pack
		int size = specs[4][0];
		
		// Two random variables to determine rarity and card
		int tmp = 0;
		int tmpRarity = 0;
		Random r = new Random();
		
		for (int i = 0; i < size; i++){

			// Randomly choose card rarity
			tmp = r.nextInt(rarityList.size());
			tmpRarity = rarityList.get(tmp);
			
			// Last card of the pack will always be rare and above
			if ( (tmpRarity == 0) && (i == (size-1) ) ){
				tmpRarity = 1;
			}
			
			// Common cards
			if (tmpRarity == 0){
				
				// Randomly choose a card and add to collection
				tmp = r.nextInt(commonCards.length);
				
				// Add to collection and reduce total crafting cost if new
				if (commonCards[tmp] < specs[0][4]){
					commonCards[tmp]++;
					remainingCost = remainingCost - specs[0][1];
				}
				
				// Disenchant and add to total bank if play set is full
				else {
					craftBank = craftBank + specs[0][2];
				}
				
			}
			
			// Rare cards
			if (tmpRarity == 1){
			
				// Randomly choose a card and add to collection
				tmp = r.nextInt(rareCards.length);
				
				// Add to collection and reduce total crafting cost if new
				if (rareCards[tmp] < specs[1][4]){
					rareCards[tmp]++;
					remainingCost = remainingCost - specs[1][1];
				}
				
				// Disenchant and add to total bank if play set is full
				else {
					craftBank = craftBank + specs[1][2];
				}
				
			}
			
			// Epic cards
			if (tmpRarity == 2){
				
				// Randomly choose a card and add to collection
				tmp = r.nextInt(epicCards.length);
				
				// Add to collection and reduce total crafting cost if new
				if (epicCards[tmp] < specs[2][4]){
					epicCards[tmp]++;
					remainingCost = remainingCost - specs[2][1];
				}
				
				// Disenchant and add to total bank if play set is full
				else {
					craftBank = craftBank + specs[2][2];
				}
				
			}
			
			// Legendary cards
			if (tmpRarity == 3){
				
				// Randomly choose a card and add to collection
				tmp = r.nextInt(legendaryCards.length);
				
				// Add to collection and reduce total crafting cost if new
				if (legendaryCards[tmp] < specs[3][4]){
					legendaryCards[tmp]++;
					remainingCost = remainingCost - specs[3][1];
				}
				
				// Disenchant and add to total bank if play set is full
				else {
					craftBank = craftBank + specs[3][2];
				}
				
			}
					
		}
		
	}
			
	
	// Repeat the pack opening process until completion
	int completion(){
		
		int count = 0;
		
		while (craftBank < remainingCost){
			packOpening();
			count++;
		}
		
		return count;
	}


	// Take the average of multiple runs
	void averageFull(){
		
		// Number of runs to average over
		// Packs are opened until completion, total count is averaged
		// over several runs
		System.out.println("Please input the number of runs to take: ");
		int runs = readInt();
		System.out.println();
		
		double total = 0;
		
		for (int i = 0; i < runs; i++){
			
			// Reset the parameters
			initializeProb();
			remainingCost = totalCraft();
			craftBank = 0;
			
			total = total + completion();
		}
		
		double result = total/runs;
		
		// Print out results to cmd
		System.out.println("Number of packs needed for full collection: ");
		System.out.println(result + " packs over " + runs + " runs");
		
	}
	
	
	// Output to txt the full collection
	void printCollection(){
		
		try{
			
			PrintStream ps = new PrintStream("Collections.txt");
			
			ps.println("Completed common cards: ");
			for (int i = 0; i < commonCards.length; i++){
				ps.print(commonCards[i] + " ");
			}
			ps.println();
			
			ps.println("Completed rare cards: ");
			for (int i = 0; i < rareCards.length; i++){
				ps.print(rareCards[i] + " ");
			}
			ps.println();
			
			ps.println("Completed epic cards: ");
			for (int i = 0; i < epicCards.length; i++){
				ps.print(epicCards[i] + " ");
			}
			ps.println();
			
			ps.println("Compleled legendary cards: ");
			for (int i = 0; i < legendaryCards.length; i++){
				ps.print(legendaryCards[i] + " ");
			}
			ps.println();
			
			ps.close();
			
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
		
	}

}