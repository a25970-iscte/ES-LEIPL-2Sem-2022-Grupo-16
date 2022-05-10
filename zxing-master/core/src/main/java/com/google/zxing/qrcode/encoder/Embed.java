package com.google.zxing.qrcode.encoder;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.Version;

public class Embed {

    private Embed() {
        throw new IllegalStateException("Utility class");
    }

    // Embed position adjustment patterns if need be.
    static void maybeEmbedPositionAdjustmentPatterns(Version version, ByteMatrix matrix) {
        if (version.getVersionNumber() < 2) { // The patterns appear if version >= 2
            return;
        }
        int index = version.getVersionNumber() - 1;
        int[] coordinates = MatrixUtil.POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index];
        for (int y : coordinates) {
            if (y >= 0) {
                for (int x : coordinates) {
                    if (x >= 0 && MatrixUtil.isEmpty(matrix.get(x, y))) {
                        // If the cell is unset, we embed the position adjustment pattern here.
                        // -2 is necessary since the x/y coordinates point to the center of the pattern,
                        // not the
                        // left top corner.
                        Embed.embedPositionAdjustmentPattern(x - 2, y - 2, matrix);
                    }
                }
            }
        }
    }

    // Embed position detection patterns and surrounding vertical/horizontal
    // separators.
    static void embedPositionDetectionPatternsAndSeparators(ByteMatrix matrix) throws WriterException {
        // Embed three big squares at corners.
        int pdpWidth = MatrixUtil.POSITION_DETECTION_PATTERN[0].length;
        // Left top corner.
        Embed.embedPositionDetectionPattern(0, 0, matrix);
        // Right top corner.
        Embed.embedPositionDetectionPattern(matrix.getWidth() - pdpWidth, 0, matrix);
        // Left bottom corner.
        Embed.embedPositionDetectionPattern(0, matrix.getWidth() - pdpWidth, matrix);

        // Embed horizontal separation patterns around the squares.
        int hspWidth = 8;
        // Left top corner.
        Embed.embedHorizontalSeparationPattern(0, hspWidth - 1, matrix);
        // Right top corner.
        Embed.embedHorizontalSeparationPattern(matrix.getWidth() - hspWidth,
                hspWidth - 1, matrix);
        // Left bottom corner.
        Embed.embedHorizontalSeparationPattern(0, matrix.getWidth() - hspWidth, matrix);

        // Embed vertical separation patterns around the squares.
        int vspSize = 7;
        // Left top corner.
        Embed.embedVerticalSeparationPattern(vspSize, 0, matrix);
        // Right top corner.
        Embed.embedVerticalSeparationPattern(matrix.getHeight() - vspSize - 1, 0, matrix);
        // Left bottom corner.
        Embed.embedVerticalSeparationPattern(vspSize, matrix.getHeight() - vspSize,
                matrix);
    }

    static void embedPositionDetectionPattern(int xStart, int yStart, ByteMatrix matrix) {
        for (int y = 0; y < 7; ++y) {
            int[] patternY = MatrixUtil.POSITION_DETECTION_PATTERN[y];
            for (int x = 0; x < 7; ++x) {
                matrix.set(xStart + x, yStart + y, patternY[x]);
            }
        }
    }

    static void embedPositionAdjustmentPattern(int xStart, int yStart, ByteMatrix matrix) {
        for (int y = 0; y < 5; ++y) {
            int[] patternY = MatrixUtil.POSITION_ADJUSTMENT_PATTERN[y];
            for (int x = 0; x < 5; ++x) {
                matrix.set(xStart + x, yStart + y, patternY[x]);
            }
        }
    }

    static void embedVerticalSeparationPattern(int xStart,
            int yStart,
            ByteMatrix matrix) throws WriterException {
        for (int y = 0; y < 7; ++y) {
            if (!MatrixUtil.isEmpty(matrix.get(xStart, yStart + y))) {
                throw new WriterException();
            }
            matrix.set(xStart, yStart + y, 0);
        }
    }

    static void embedHorizontalSeparationPattern(int xStart,
            int yStart,
            ByteMatrix matrix) throws WriterException {
        for (int x = 0; x < 8; ++x) {
            if (!MatrixUtil.isEmpty(matrix.get(xStart + x, yStart))) {
                throw new WriterException();
            }
            matrix.set(xStart + x, yStart, 0);
        }
    }

    // Embed the lonely dark dot at left bottom corner. JISX0510:2004 (p.46)
    static void embedDarkDotAtLeftBottomCorner(ByteMatrix matrix) throws WriterException {
        if (matrix.get(8, matrix.getHeight() - 8) == 0) {
            throw new WriterException();
        }
        matrix.set(8, matrix.getHeight() - 8, 1);
    }

    static void embedTimingPatterns(ByteMatrix matrix) {
        // -8 is for skipping position detection patterns (size 7), and two
        // horizontal/vertical
        // separation patterns (size 1). Thus, 8 = 7 + 1.
        for (int i = 8; i < matrix.getWidth() - 8; ++i) {
            int bit = (i + 1) % 2;
            // Horizontal line.
            if (MatrixUtil.isEmpty(matrix.get(i, 6))) {
                matrix.set(i, 6, bit);
            }
            // Vertical line.
            if (MatrixUtil.isEmpty(matrix.get(6, i))) {
                matrix.set(6, i, bit);
            }
        }
    }

}
