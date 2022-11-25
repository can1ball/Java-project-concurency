package factory;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class ProductionRobotSpawner extends Thread {
	// minimum sleeping time
	private final int minimumSleepingTime = 500;
	// maximum sleeping time
	private final int maximumSleepingTime = 1000;
	// array of existing facilities where production robots may be spawned
	public Factory[] factory;
	// current number of factories
	int noOfFactories;

	/**
	 * Constructor. Initializes fields
	 */
	ProductionRobotSpawner(Factory[] factory, int noOfFactories) {
		this.factory = factory;
		this.noOfFactories = noOfFactories;
	}

	/**
	 * The threads sleeps for a random amount of time, then calls a method in the
	 * facility to spawn robots there. The thread stops this routine when the goal
	 * is achieved.
	 */
	public void run() {
		Random rand = new Random();
		int sleepingTime;
		while (ProductionRobot.CurrentNumberOfVaccines < Main.NO_OF_VACCINES_TO_ACHIEVE) {
			sleepingTime = rand.nextInt(maximumSleepingTime - minimumSleepingTime) + minimumSleepingTime;
			try {
				Thread.sleep(sleepingTime);
			} catch (InterruptedException e) {
				System.out.println("Robot spawner can't sleep");
				e.printStackTrace();
			}

			for (int i = 0; i < noOfFactories; i++) {
				ReentrantLock factoryLock = factory[i].getFactoryLock();
				factoryLock.lock();
				factory[i].addProductionRobotToFactory();
				factoryLock.unlock();
			}
		}
		System.out.println("Robot spawner finished");
	}
}
