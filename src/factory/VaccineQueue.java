package factory;

import java.util.LinkedList;
import java.util.Queue;

public class VaccineQueue {
	// vaccine queue
	Queue<Vaccine> lockedQueue = new LinkedList<Vaccine>();
	// variable that keeps track if the demon is writing in queue or not
	boolean isRobotCreatingVaccine = false;

	/**
	 * adds a vaccine to the queue
	 * 
	 * @param vaccine to be added in queue
	 */
	public void push(Vaccine item) {
		isRobotCreatingVaccine = true;
		synchronized (lockedQueue) {
			lockedQueue.add(item);
			System.out.println(
					"Production robot added in queue vaccine at position " + item.position.x + " " + item.position.y);
			isRobotCreatingVaccine = false;
			lockedQueue.notifyAll();
		}
	}

	/**
	 * Removes the vaccine from queue if none is writing, otherwise returns null
	 */
	public Vaccine pop() {
		Vaccine item = null;
		synchronized (lockedQueue) {
			while(isRobotCreatingVaccine) {
				try {
					lockedQueue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		item = lockedQueue.poll();
		if(item == null) {
			return item;
		}
		
		System.out.println(
				"Packing robot got the vaccine in queue at position " + item.position.x + " " + item.position.y);
		return item;
	}
}
