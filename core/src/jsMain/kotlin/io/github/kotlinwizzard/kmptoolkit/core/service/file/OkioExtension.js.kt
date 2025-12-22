package io.github.kotlinwizzard.kmptoolkit.core.service.file

import okio.FileSystem
import okio.fakefilesystem.FakeFileSystem


private val fakeFs = FakeFileSystem()
actual val FileSystem.Companion.SYSTEM: FileSystem
    get() =  fakeFs