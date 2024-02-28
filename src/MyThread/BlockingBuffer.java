package myThread;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

public class BlockingBuffer {
    private int mCapacityOfQueue;
    private LinkedBlockingDeque mQueues;

    public BlockingBuffer(int mCapacityOfQueue) {
        this.mCapacityOfQueue = mCapacityOfQueue;
        this.mQueues = new LinkedBlockingDeque<>(mCapacityOfQueue);
    }

    public synchronized boolean isEmpty() {
        return mQueues.isEmpty();
    }

    public synchronized void clear() {
        mQueues.clear();
    }

    public synchronized int size() {
        return mQueues.size();
    }

    public <T> boolean push(T data) throws InterruptedException {
        if (mQueues.size() >= mCapacityOfQueue) {
            Object object = mQueues.takeFirst();
            object = null;
        }
        return mQueues.offerLast(data);
    }

    public <T> T pop() throws InterruptedException {
        if (mQueues.size() > 0) {
            return (T) mQueues.takeLast();
        }
        return null;
    }

    public <T> T takeFirst() throws InterruptedException {
        if (mQueues.size() > 0 ){
            return (T) mQueues.takeFirst();
        }
        return null;
    }

    public <T> T getLast() throws InterruptedException {
        if (mQueues.size() > 0){
            return (T) mQueues.takeLast();
        }
        return null;
    }

    public <T> T getFirst(int i) throws InterruptedException {
        if (mQueues.size() > 0){
            return (T) mQueues.getFirst();
        }
        return null;
    }

    public <T> int drainTo(Collection<T> c){
        if (mQueues.size() > 0){
            return mQueues.drainTo(c);
        }
        return -1;
    }
}
