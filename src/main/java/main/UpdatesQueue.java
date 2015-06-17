package main;

import java.util.concurrent.ConcurrentLinkedQueue;

public class UpdatesQueue {
private ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<String>();
	
	public UpdatesQueue() {
	}

	public boolean isEmpty() {
		return (list.size() == 0);
	}

	public synchronized void enqueue(String item){
		list.add(item);
	}

	public synchronized String dequeue() {
		return list.poll();
	}

	public String peek() {
		return list.peek();
	}
}