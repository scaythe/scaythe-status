package com.scaythe.status;

import static com.scaythe.status.ScaytheStatusApp.logError;

import com.scaythe.status.write.ModuleData;
import com.scaythe.status.write.StatusWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WriteManager implements SmartLifecycle {
  private final StatusWriter writer;
  private final BlockingQueue<List<ModuleData>> combinedDataQueue;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private boolean started = false;

  public WriteManager(ModuleManager moduleManager, StatusWriter writer) {
    combinedDataQueue = moduleManager.combinedDataQueue();
    this.writer = writer;
  }

  private void writeCombinedData() {
    writer.writeHeader();

    List<ModuleData> lastData = List.of();

    while (!Thread.interrupted()) {
      try {
        List<ModuleData> data = combinedDataQueue.take();
        log.atDebug().log("got combined data: {}", data);
        if (!data.equals(lastData)) {
          writer.write(data);
          lastData = data;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public synchronized void start() {
    executor.submit(logError(this::writeCombinedData));
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
