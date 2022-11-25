package factory;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Factory extends Thread {
	// factory identifier
	int id;
	// factory size
	public int factoryDimensions;
	// lock the factory
	private ReentrantLock factoryLock = new ReentrantLock();
	// vaccine queue common with production and packing robots
	VaccineQueue vaccineQueue;
	// maximum number of production robots that can be spawned in a factory
	public int maxProductionRobots;
	// vector of robots in factory
	Vector<ProductionRobot> pRobots;
	// current number of production robots robots in factory
	public int currentNumberOfRobots = 0;
	// hash map of robots and their position in the factory
	HashMap<String, ProductionRobot> robotsPositions = new HashMap<String, ProductionRobot>();

	/**
	 * @param id               factory identifier
	 * @param factoryDimension factory size
	 * @param vaccineQueue     queue common with production and packing robots
	 */
	Factory(int id, int factoryDimension, VaccineQueue vaccineQueue) {
		this.id = id;
		this.factoryDimensions = factoryDimension;
		this.vaccineQueue = vaccineQueue;
		maxProductionRobots = factoryDimension / 2;
		pRobots = new Vector<ProductionRobot>(maxProductionRobots);
		System.out.println("Factory " + id + " with maximum no of production robots: " + maxProductionRobots);
	}

	/**
	 * Send the lock to other classes so they can lock this factory
	 */
	public ReentrantLock getFactoryLock() {
		return factoryLock;
	}

	/**
	 * Method checks if there is enough space to add another robot. If so, it
	 * generates a random position until it gets a valid one and places the robot in
	 * that position.
	 */
	public synchronized void addProductionRobotToFactory() {
		if (currentNumberOfRobots < maxProductionRobots) {
			Random rand = new Random();
			int x = rand.nextInt(factoryDimensions);
			int y = rand.nextInt(factoryDimensions);
			while (robotsPositions.get(x + " " + y) != null) {
				x = rand.nextInt(factoryDimensions);
				y = rand.nextInt(factoryDimensions);
			}
			Position pos = new Position(x, y);
			pRobots.add(currentNumberOfRobots,
					new ProductionRobot(pos, factoryDimensions, robotsPositions, vaccineQueue, id, this));
			robotsPositions.put((pos.x + " " + pos.y), pRobots.get(currentNumberOfRobots));
			pRobots.get(currentNumberOfRobots).start();
			currentNumberOfRobots++;
			System.out.println("Added production robot to facility " + this.id + " at position " + pos.x + " " + pos.y
					+ " current count: " + currentNumberOfRobots);
		}
	}

	/**
	 * Every few seconds, the factory asks the robots, their position
	 */
	public void run() {
		while (ProductionRobot.CurrentNumberOfVaccines < Main.NO_OF_VACCINES_TO_ACHIEVE) {
			factoryLock.lock();
			for (int i = 0; i < currentNumberOfRobots; i++) {
				pRobots.get(i).getPosition();
			}
			factoryLock.unlock();
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Factory " + id + " finished");
	}
}
