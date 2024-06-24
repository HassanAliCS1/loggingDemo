package com.filter.demo.testerPractice;

import java.lang.reflect.Array;

public record AyahRecord(
        int number,
        String audio,
        Array audioSecondary,
        String text,
        Edition edition,
        Surah surah,
        int numberInSurah,
        int juz,
        int manzil,
        int page,
        int ruku,
        int hizbQuarter,
        Boolean sajda
) {
}
