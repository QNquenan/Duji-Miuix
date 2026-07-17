package com.quenan.duji.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseNotesRepositoryTest {
    @Test
    fun comparesVersionNamesByNumericSegments() {
        assertTrue(ReleaseNotesRepository.isVersionNewer("v0.10.14", "v0.10.13"))
        assertTrue(ReleaseNotesRepository.isVersionNewer("v1.0.0", "v0.99.99"))
        assertFalse(ReleaseNotesRepository.isVersionNewer("v0.9.0", "v0.10.13"))
        assertFalse(ReleaseNotesRepository.isVersionNewer("v0.10.13", "v0.10.13"))
    }
}
