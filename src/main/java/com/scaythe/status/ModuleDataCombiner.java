package com.scaythe.status;

import com.scaythe.status.write.ModuleData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ModuleDataCombiner {
  private final AtomicReference<List<ModuleData>> lastCombinedOutput =
      new AtomicReference<>(List.of());
  private final AtomicInteger nextRegistrationIndex = new AtomicInteger();

  private final BlockingQueue<List<ModuleData>> combinedDataQueue;

  public ModuleDataCombiner(BlockingQueue<List<ModuleData>> combinedDataQueue) {
    this.combinedDataQueue = combinedDataQueue;
  }

  public Consumer<ModuleData> getNextModuleOutput() {
    lastCombinedOutput.updateAndGet(
        lastOutput -> {
          List<ModuleData> newOutput = new ArrayList<>(lastOutput);
          newOutput.add(ModuleData.of("", ""));
          return List.copyOf(newOutput);
        });

    int index = nextRegistrationIndex.getAndIncrement();
    return moduleData -> {
      List<ModuleData> combinedData =
          lastCombinedOutput.updateAndGet(
              lastOutput -> {
                List<ModuleData> newOutput = new ArrayList<>(lastOutput);
                newOutput.set(index, moduleData);
                return List.copyOf(newOutput);
              });
      combinedDataQueue.add(combinedData);
    };
  }
}
