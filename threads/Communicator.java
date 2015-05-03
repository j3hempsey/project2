package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		this.lock = new Lock();
		this.speakerLock = new Condition2(lock);
		this.listenerLock = new Condition2(lock);
		this.wordReady = false;
		this.listener = 0;
		this.speaker = 0;
}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word the integer to transfer.
	 */
	public void speak(int word) {
		lock.acquire();
		speaker++;
		// if there is no listener, speaker waits
		while (listener == 0) {
			speakerLock.sleep();
		}
		//if listener is ready, then speaker prepares word
		if (listener == 1) {
			speakerLock.wake();
			wordReady = true;
			this.wordObj = word; //make word object to pass to listener
		}
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		lock.acquire();
		listener++;
		int message = 0;
		// if there is no speaker, listener waits
		while (speaker == 0) {
			listenerLock.sleep();
		}
		//if speaker is ready, then listener gets word from speaker
		if (speaker == 1) {
			listenerLock.wake();
			message = wordObj.intValue();
		}
		listener--;
		lock.release();
		return message;
	}
	
	private Lock lock;
	private Condition2 speakerLock;
	private Condition2 listenerLock;
	private boolean wordReady;
	private int listener;
	private int speaker;
	private Integer wordObj;
}
