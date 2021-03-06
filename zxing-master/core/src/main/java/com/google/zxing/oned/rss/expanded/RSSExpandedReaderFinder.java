package com.google.zxing.oned.rss.expanded;


import com.google.zxing.oned.rss.FinderPattern;
import com.google.zxing.common.BitArray;
import java.util.List;
import com.google.zxing.NotFoundException;

public class RSSExpandedReaderFinder {
	private final int[] startEnd = new int[2];

	public FinderPattern patternFinderBuilder(BitArray row, List<ExpandedPair> previousPairs, int rowNumber,
			boolean isOddPattern, boolean thisStartFromEven, RSSExpandedReader rSSExpandedReader)
			throws NotFoundException {
		FinderPattern pattern;
		boolean keepFinding = true;
		int forcedOffset = -1;
		do {
			this.findNextPair(row, previousPairs, forcedOffset, thisStartFromEven, rSSExpandedReader);
			pattern = row.parseFoundFinderPattern(rowNumber, isOddPattern, rSSExpandedReader, startEnd);
			if (pattern == null) {
				forcedOffset = getNextSecondBar(row, this.startEnd[0]);
			} else {
				keepFinding = false;
			}
		} while (keepFinding);
		return pattern;
	}

	public void findNextPair(BitArray row, List<ExpandedPair> previousPairs, int forcedOffset,
			boolean thisStartFromEven, RSSExpandedReader rSSExpandedReader) throws NotFoundException {
		int[] counters = rSSExpandedReader.getDecodeFinderCounters();
		counters[0] = 0;
		counters[1] = 0;
		counters[2] = 0;
		counters[3] = 0;
		int width = row.getSize();
		int rowOffset;
		if (forcedOffset >= 0) {
			rowOffset = forcedOffset;
		} else if (previousPairs.isEmpty()) {
			rowOffset = 0;
		} else {
			ExpandedPair lastPair = previousPairs.get(previousPairs.size() - 1);
			rowOffset = lastPair.getFinderPattern().getStartEnd()[1];
		}
		boolean searchingEvenPair = previousPairs.size() % 2 != 0;
		if (thisStartFromEven) {
			searchingEvenPair = !searchingEvenPair;
		}
		boolean isWhite = false;
		while (rowOffset < width) {
			isWhite = !row.get(rowOffset);
			if (!isWhite) {
				break;
			}
			rowOffset++;
		}
		int counterPosition = 0;
		int patternStart = rowOffset;
		for (int x = rowOffset; x < width; x++) {
			if (row.get(x) != isWhite) {
				counters[counterPosition]++;
			} else {
				if (counterPosition == 3) {
					if (searchingEvenPair) {
						RSSExpandedReader.reverseCounters(counters);
					}
					if (RSSExpandedReader.isFinderPattern(counters)) {
						this.startEnd[0] = patternStart;
						this.startEnd[1] = x;
						return;
					}
					if (searchingEvenPair) {
						RSSExpandedReader.reverseCounters(counters);
					}
					patternStart += counters[0] + counters[1];
					counters[0] = counters[2];
					counters[1] = counters[3];
					counters[2] = 0;
					counters[3] = 0;
					counterPosition--;
				} else {
					counterPosition++;
				}
				counters[counterPosition] = 1;
				isWhite = !isWhite;
			}
		}
		throw NotFoundException.getNotFoundInstance();
	}

	public static int getNextSecondBar(BitArray row, int initialPos) {
		int currentPos;
		if (row.get(initialPos)) {
			currentPos = row.getNextUnset(initialPos);
			currentPos = row.getNextSet(currentPos);
		} else {
			currentPos = row.getNextSet(initialPos);
			currentPos = row.getNextUnset(currentPos);
		}
		return currentPos;
	}
}