package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

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
		wordObj = new LinkedList<Integer>();
		listenerQueue = new LinkedList<KThread>();
		speakerQueue = new LinkedList<KThread>();
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
		Lib.debug(dbgCom, "Adding speaker");
		speakerQueue.addLast(KThread.currentThread());
		
		//Add the speakers word to the queue
		Lib.debug(dbgCom, "Speaker adding word");
		wordObj.addLast(word); //make word object to pass to listener

		// if there is no listener, speaker waits
		if (listenerQueue.size() == 0) {
			Lib.debug(dbgCom, "speakerLock sleeping");
			speakerLock.sleep();
		} else {
			//listener is ready, then speaker prepares word
			Lib.debug(dbgCom, "SpeakerLock waking");
			listenerLock.wake();
		}

		listenerLock.wakeAll();
		lock.release();
		return;
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		lock.acquire();
		int message = 0;
		Lib.debug(dbgCom, "Adding listener");
		listenerQueue.addLast(KThread.currentThread());
		// if there is no speaker, listener waits
		///...Lib.debug(dbgCom, "Waking speakerLock");
		///...speakerLock.wakeAll();
		if (speakerQueue.size() == 0) {
			Lib.debug(dbgCom, "Sleeping listnerLock");
			listenerLock.sleep();
		} else {
			Lib.debug(dbgCom, "Waking speakerLock");
			speakerLock.wakeAll();
		}
		//if speaker is ready, then listener gets word from speaker
		listenerQueue.removeFirst();
		listenerLock.wake();
		Lib.debug(dbgCom, "Listener recieving word");
		message = wordObj.remove();
		listener--;
		if(listenerQueue.size() > 0 && wordObj.size() > 0) listenerLock.wakeAll();

		lock.release();
		return message;
	}
	private static final char dbgCom = 'c';
	private Lock lock;
	private Condition2 speakerLock;
	private Condition2 listenerLock;
	private int listener;
	private int speaker;
	private LinkedList<Integer> wordObj;
	private LinkedList<KThread> listenerQueue;
	private LinkedList<KThread> speakerQueue;


	//*** Testing ***
    private static class CommunicatorSendTest implements Runnable {
		private String name;
		private Communicator communicator; 
		private int word;
		CommunicatorSendTest(String name, Communicator communicator, int word) {
		    this.name=name;
		    this.communicator=communicator;
		    this.word=word;
		}
		
		public void run() {
		    System.out.println("*** " + name + ": Before call to speak with " + word);
		    communicator.speak(word);
		    System.out.println("*** " + name + ": After call to speak with " + word);
		}
    }
    private static class CommunicatorListenTest implements Runnable {
		private String name;
		private Communicator communicator; 
		CommunicatorListenTest(String name, Communicator communicator) {
		    this.name=name;
		    this.communicator=communicator;
		}
		
		public void run() {
		    System.out.println("*** " + name + ": Before call to listen.");
		    int word=communicator.listen();
		    System.out.println("*** " + name + ": Received " + word);
		}
    }
    public static void selfTest() {
		// Communicator Tests
		Communicator communicator = new Communicator();
		System.out.println("\nTesting Communicator...\n");
		//new KThread(new CommunicatorListenTest("one",communicator)).fork();
		//new KThread(new CommunicatorListenTest("two",communicator)).fork();
		new KThread(new CommunicatorSendTest("one",communicator,10)).fork();
		new KThread(new CommunicatorSendTest("two",communicator,20)).fork();
		new KThread(new CommunicatorListenTest("one",communicator)).fork();
		new KThread(new CommunicatorListenTest("two",communicator)).fork();
    	
    }
}
