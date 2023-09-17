package com.scaythe.status;

import static com.scaythe.status.ScaytheStatusApp.logError;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.input.EventReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Flogger
public class ReadManager implements SmartLifecycle {
  private final ModuleManager moduleManager;
  private final EventReader reader;

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final BlockingQueue<ClickEvent> queue = new SynchronousQueue<>();
  private boolean started = false;

  private void dispatchEvents() {
    while (!Thread.interrupted()) {
      try {
        moduleManager.dispatchEvent(queue.take());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public synchronized void start() {
    executor.submit(logError(() -> reader.read(queue::add)));
    executor.submit(logError(this::dispatchEvents));
    started = true;
  }

  @Override
  public synchronized void stop() {
    executor.shutdownNow();
  }

  @Override
  public synchronized boolean isRunning() {
    return started && !executor.isTerminated();
  }
}
