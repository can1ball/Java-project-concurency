package factory;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProductionRobot extends Thread {
	// current number of vaccines created by the robots
	public static volatile int CurrentNumberOfVaccines = 0;
	// a lock used to ensure exclusive access to the number of vaccines variable
	public static Lock vaccineLock = new ReentrantLock();
	// a lock used to ensure exclusive access to the robots positions in the map map
	public static Lock moveLock = new ReentrantLock();
	// position on x and y coordinated of a robot in the matrix
	private Position position;
	// queue for vaccines; common with a factory, production and packing
	public VaccineQueue potionQueue;
	// dimension of the factory the robot belongs to
	public int factorySize;
	// map that contains robots positions
	HashMap<String, ProductionRobot> robotMapPosition;
	// identifier of the factory the robot belongs to
	public int factoryId;
	ReentrantLock factoryLock = new ReentrantLock();

	/**
	 * @param position         coordinates x and y of a robot in the factory
	 * @param facilitySize     factory size
	 * @param robotMapPosition Map that has the coordinates of all the robots in the
	 *                         factory
	 * @param vaccineQueue     queue where robots write vaccines
	 * @param factoryId        identifier
	 */
	ProductionRobot(Position position, int facilitySize, HashMap<String, ProductionRobot> robotMapPosition,
			VaccineQueue vaccineQueue, int factoryId, Factory factory) {
		this.position = position;
		this.factorySize = facilitySize;
		this.robotMapPosition = robotMapPosition;
		this.potionQueue = vaccineQueue;
		this.factoryId = factoryId;
		factoryLock = factory.getFactoryLock();
	}

	public Position getPosition() {
		return this.position;
	}

	public void run() {
		while ((ProductionRobot.CurrentNumberOfVaccines < Main.NO_OF_VACCINES_TO_ACHIEVE)) {
			moveRobot();
		}
		System.out.println("Production robot in facility " + factoryId + " finished");
	}

	/**
	 * Check if the demon can move in a direction and if so move, otherwise sleep
	 */
	private void moveRobot() {
		try {
			factoryLock.lock();
			if (canMoveRight()) {
				moveRight();
			} else if (canMoveUp()) {
				moveUp();
			} else if (canMoveDown()) {
				moveDown();
			} else if (canMoveLeft()) {
				moveLeft();
			} else {
				surrounded();
			}
		} finally {
			factoryLock.unlock();
		}
	}

	/**
	 * Checks if the robot can move right, there isn't another robot there and the
	 * robot is still within the factory boundaries
	 * 
	 * @return true if he can move, false otherwise
	 */
	private boolean canMoveRight() {
		if (position.x + 1 < factorySize)
			if (robotMapPosition.get((position.x + 1) + " " + position.y) == null) {
				return true;
			}
		return false;
	}

	/**
	 * Update position and call methods to increment number of vaccines and rest
	 */
	private void moveRight() {
		removeOldPosition();
		System.out.println("Production robot in facility " + factoryId + " moved right from " + this.position.x + " "
				+ this.position.y);
		this.position.x += 1;
		addNewPosition();
		createVaccine(); // create a vaccine at the new position
		restAfterCreatingVaccine();
	}

	/**
	 * Checks if the robot can move up, there isn't another robot there and the
	 * robot is still within the facility boundaries
	 * 
	 * @return true if he can move, false otherwise
	 */
	private boolean canMoveUp() {
		if (position.y - 1 > 0)
			if (robotMapPosition.get(position.x + " " + (position.y - 1)) == null) {
				return true;
			}
		return false;
	}

	/**
	 * Update position and call methods to increment number of vaccines and rest
	 */
	private void moveUp() {
		removeOldPosition();
		System.out.println("Production robot in facility " + factoryId + " moved up from " + this.position.x + " "
				+ this.position.y);
		this.position.y -= 1;
		addNewPosition();
		createVaccine();
		restAfterCreatingVaccine();
	}

	/**
	 * Checks if the robot can move down, there isn't another robot there and the
	 * robot is still within the facility boundaries
	 * 
	 * @return true if he can move, false otherwise
	 */
	private boolean canMoveDown() {
		if (position.y + 1 < (factorySize))
			if (robotMapPosition.get(position.x + " " + (position.y + 1)) == null) {
				return true;
			}
		return false;
	}

	/**
	 * Update position and call methods to increment number of vaccine and rest
	 */
	private void moveDown() {
		removeOldPosition();
		System.out.println("Production robot in facility " + factoryId + " moved down from " + this.position.x + " "
				+ this.position.y);
		this.position.y += 1;
		addNewPosition();
		createVaccine();
		restAfterCreatingVaccine();
	}

	/**
	 * Checks if the robot can move left, there isn't another robot there and the
	 * robot is still within the factory boundaries
	 * 
	 * @return true if he can move, false otherwise
	 */
	private boolean canMoveLeft() {
		if (position.x - 1 > 0)
			if (robotMapPosition.get((position.x - 1) + " " + position.y) == null) {
				return true;
			}
		return false;
	}

	/**
	 * Update position and call methods to increment number of vaccines and rest
	 */
	private void moveLeft() {
		removeOldPosition();
		System.out.println("Production robot in facility " + factoryId + " moved left from " + this.position.x + " "
				+ this.position.y);
		this.position.x -= 1;
		addNewPosition();
		createVaccine();
		restAfterCreatingVaccine();
	}

	/**
	 * Generate a random number and sleep thread
	 */
	private void surrounded() {
		Random rand = new Random();
		int max_sleep_time = 50;
		int min_sleep_time = 10;
		int sleep_time = rand.nextInt(max_sleep_time - min_sleep_time) + min_sleep_time;
		try {
			System.out.println("Production robot in facility " + factoryId + " at position " + this.position.x + " "
					+ this.position.y + " is surrounded and is resting");
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			System.out.println("Robot could not rest although it's surrounded");
			e.printStackTrace();
		}
	}

	private void restAfterCreatingVaccine() {
		try {
			System.out.println("Production robot in facility " + factoryId
					+ " created vaccine and is resting, current vaccines count: "
					+ ProductionRobot.CurrentNumberOfVaccines);
			Thread.sleep(30);
		} catch (InterruptedException e) {
			System.out.println("Robot could not sleep after creating vaccine ");
			e.printStackTrace();
		}
	}

	/**
	 * Increment number of vaccines
	 */
	private void createVaccine() {
		vaccineLock.lock();
		potionQueue.push(new Vaccine(new Position(position.x, position.y)));
		ProductionRobot.CurrentNumberOfVaccines++;
		vaccineLock.unlock();
	}

	/**
	 * Update robot position in map
	 */
	private void addNewPosition() {
		moveLock.lock();
		robotMapPosition.put(position.x + " " + position.y, this);
		moveLock.unlock();
	}

	/**
	 * Remove from map the position from where the robot moved
	 */
	private void removeOldPosition() {
		moveLock.lock();
		robotMapPosition.remove(position.x + " " + position.y);
		moveLock.unlock();
	}
}
