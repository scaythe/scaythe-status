package com.scaythe.status.module;

import java.util.ArrayDeque;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LimitedQueue<E> extends ArrayDeque<E> {

  private final int limit;

  @Override
  public boolean add(E o) {
    super.add(o);

    while (size() > limit) {
      super.remove();
    }

    return true;
  }
}
