package factory;

import java.util.Random;

public class Main {
	// maximum and minimum number of factories
	static final int MINIMUM_NO_OF_FACTORIES = 2;
	static final int MAXIMUM_NO_OF_FACTORIES = 5;
	// the size of the factory
	static final int MINIMUM_FACTORY_SIZE = 100;
	static final int MAXIMUM_FACTORY_SIZE = 500;
	// number of packing robots
	static final int NO_OF_PACKING_ROBOTS = 30;
	// number of vaccines
	static final int NO_OF_VACCINES_TO_ACHIEVE = 1000;
	static volatile int CURRENT_NO_OF_VACCINES = 0;

	public static void main(String[] args) {

		Random rand = new Random();
		int numberOfFactories = rand.nextInt(MAXIMUM_NO_OF_FACTORIES - MINIMUM_NO_OF_FACTORIES)
				+ MINIMUM_NO_OF_FACTORIES;
		System.out.println("Number of packing robots: " + NO_OF_PACKING_ROBOTS);
		System.out.println("Number of factories: " + numberOfFactories);

		Factory[] factory = new Factory[numberOfFactories];
		Thread[] packingRobots = new Thread[NO_OF_PACKING_ROBOTS];
		VaccineQueue[] vaccineQueues = new VaccineQueue[numberOfFactories];

		ProductionRobotSpawner productionRobotSpawner = new ProductionRobotSpawner(factory, numberOfFactories);

		for (int i = 0; i < numberOfFactories; i++) {
			vaccineQueues[i] = new VaccineQueue();
		}
		for (int i = 0; i < numberOfFactories; i++) {
			int factoryDimension = rand.nextInt(MAXIMUM_FACTORY_SIZE - MINIMUM_FACTORY_SIZE) + MINIMUM_FACTORY_SIZE;
			factory[i] = new Factory(i, factoryDimension, vaccineQueues[i]);
			factory[i].start();
		}
		for (int i = 0; i < NO_OF_PACKING_ROBOTS; i++) {
			packingRobots[i] = new PackingRobots(i, vaccineQueues, numberOfFactories);
			packingRobots[i].start();
		}
		productionRobotSpawner.start();

		for (int i = 0; i < numberOfFactories; i++) {
			try {
				factory[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Could not join factory " + i);
			}
		}
		try {
			productionRobotSpawner.join();
		} catch (InterruptedException e) {
			System.out.println("Could not join production robot spawner");
			e.printStackTrace();
		}
		for (int i = 0; i < NO_OF_PACKING_ROBOTS; i++) {
			try {
				packingRobots[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Could not join packing robots " + i);
			}
		}
		System.out.println("Application finished");
	}
}
