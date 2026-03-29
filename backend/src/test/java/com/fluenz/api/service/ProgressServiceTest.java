package com.fluenz.api.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressServiceTest {

    @Test
    void calculateGainedMinutesReturnsZeroForNonPositiveDurations() {
        assertEquals(0, ProgressService.calculateGainedMinutes(0));
        assertEquals(0, ProgressService.calculateGainedMinutes(-15));
    }

    @Test
    void calculateGainedMinutesRoundsUpToOneMinuteBelowSixtySeconds() {
        assertEquals(1, ProgressService.calculateGainedMinutes(1));
        assertEquals(1, ProgressService.calculateGainedMinutes(59));
    }

    @Test
    void calculateGainedMinutesRoundsUpPartialMinutes() {
        assertEquals(1, ProgressService.calculateGainedMinutes(60));
        assertEquals(2, ProgressService.calculateGainedMinutes(61));
        assertEquals(9, ProgressService.calculateGainedMinutes(540));
    }

    @Test
    void calculateGainedMinutesCapsAtTenMinutesPerChunk() {
        assertEquals(10, ProgressService.calculateGainedMinutes(600));
        assertEquals(10, ProgressService.calculateGainedMinutes(601));
        assertEquals(10, ProgressService.calculateGainedMinutes(3600));
    }
}
