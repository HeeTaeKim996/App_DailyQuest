package com.example.dailyquest.Utils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RunnableDelegate
{
    public final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Runnable listener)
    {
        if(listener != null && listeners.contains(listener) == false)
        {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener)
    {
        listeners.remove(listener);
    }

    public void invoke()
    {
        for(Runnable listener : listeners)
        {
            listener.run();
        }
    }

}
