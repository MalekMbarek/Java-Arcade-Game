//Observable.java
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Observable {
    // CopyOnWriteArrayList : plusieurs threads (les missiles) notifient en même temps
    private List<Observer> observers = new CopyOnWriteArrayList<>();

    public void add(Observer obs) {
        observers.add(obs);
    }

    public void notifyObserver() {
        for (Observer o : observers) o.update(this);
    }
}
