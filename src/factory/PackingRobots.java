package factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class PackingRobots extends Thread {
	// total number of facility
	int numberOfFacilities;
	// vaccine queue, common with production and packing robots
	public VaccineQueue vaccineQueue[];
	// packing robots identifier
	int id;
	// only 10 witches can send the vaccine to the server
	Semaphore semaphore = new Semaphore(10);
	// minimum sleeping time
	private final int minimumSleepingTime = 10;
	// maximum sleeping time
	private final int maximumSleepingTime = 30;

	/**
	 * @param id           robot identifier identifier
	 * @param vaccineQueue common for factories, production and packing robots
	 */
	PackingRobots(int id, VaccineQueue vaccineQueue[], int numberOfFacilities) {
		this.id = id;
		this.vaccineQueue = vaccineQueue;
		this.numberOfFacilities = numberOfFacilities;
	}

	/**
	 * Each robot packs the vaccine and randomly picks a free factory and reads
	 * vaccines from its queue when production robot are not writing. When they
	 * succeed they send the vaccines by TCP/IP to the server. they stop when the
	 * vaccines goal is achieved.
	 */
	public void run() {
		Socket socket = null;
		int serverPort = 6789;
		Random rand = new Random();
		while (Main.CURRENT_NO_OF_VACCINES < Main.NO_OF_VACCINES_TO_ACHIEVE) {
			int currentCoven = rand.nextInt(numberOfFacilities);
			int sleepingTime = rand.nextInt(maximumSleepingTime - minimumSleepingTime) + minimumSleepingTime;
			
			try {
				Thread.sleep(sleepingTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Vaccine potion = vaccineQueue[currentCoven].pop();
			if (potion != null) {
				try {
					socket = new Socket("localhost", serverPort);
					DataInputStream in = new DataInputStream(socket.getInputStream());
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					out.writeUTF("Packing robot sent one potion to Santa"); // UTF is a string encoding.
					String data = in.readUTF();
					System.out.println("Received: " + data);
					Main.CURRENT_NO_OF_VACCINES++;
					System.out.println(Main.CURRENT_NO_OF_VACCINES);
				} catch (UnknownHostException e) {
					System.out.println("Socket:" + e.getMessage());
				} catch (EOFException e) {
					System.out.println("EOF:" + e.getMessage());
				} catch (IOException e) {
					System.out.println("Readline:" + e.getMessage());
				} finally {
					if (socket != null)
						try {
							socket.close();
						} catch (IOException e) {
							System.out.println("Close:" + e.getMessage());
						}
				}
			}
			semaphore.release();
		}
		System.out.println("Packing robot " + id + " finished");
	}

}
