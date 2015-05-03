package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		waitQueue = new ArrayList<KThread>();
		timeQueue = new ArrayList<Long>();
		
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		
		// lock the system
		boolean int_status = Machine.interrupt().disable();
		
		// for each of the items in the queue
		for (int i = 0; i < waitQueue.size(); i++) {
			// is it time to wake up?
			if (Machine.timer().getTime() >= timeQueue.get(i)) {
				// wake up that thread
				waitQueue.get(i).ready();
				// remove from wait queue
				waitQueue.remove(i);
				timeQueue.remove(i);
			}
		}
		
		// unlock the system
		Machine.interrupt().restore(int_status);
		
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;
		

		// lock the system
		boolean int_status = Machine.interrupt().disable();
		// add items that want to be waited onto the queue
		waitQueue.add(KThread.currentThread());
		// and the time that wants to be woken at
		timeQueue.add(wakeTime);
		
		// make that thread blocked
		KThread.sleep();
		
		// unlock the system
		Machine.interrupt().restore(int_status);
		
		
	}
	
	private ArrayList<KThread> waitQueue;
	private ArrayList<Long> timeQueue;
	
}
