package com.google.zxing.qrcode.detector;

import java.io.Serializable;
import java.util.Comparator;

/**
   * <p>Orders by {@link FinderPattern#getEstimatedModuleSize()}</p>
   */
  final class EstimatedModuleComparator implements Comparator<FinderPattern>, Serializable {
    @Override
    public int compare(FinderPattern center1, FinderPattern center2) {
      return Float.compare(center1.getEstimatedModuleSize(), center2.getEstimatedModuleSize());
    }
  }