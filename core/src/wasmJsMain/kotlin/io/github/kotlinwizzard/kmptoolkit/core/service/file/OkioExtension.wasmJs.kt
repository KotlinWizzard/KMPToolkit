package io.github.kotlinwizzard.kmptoolkit.core.service.file

import okio.FileSystem
import okio.fakefilesystem.FakeFileSystem

private val wasmFs = FakeFileSystem()
actual val FileSystem.Companion.SYSTEM: FileSystem
    get() =  wasmFs