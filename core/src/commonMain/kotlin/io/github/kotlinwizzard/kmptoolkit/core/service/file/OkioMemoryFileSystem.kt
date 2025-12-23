package io.github.kotlinwizzard.kmptoolkit.core.service.file

import io.github.kotlinwizzard.kmptoolkit.core.extensions.millis
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.Buffer
import okio.FileHandle
import okio.FileMetadata
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.Timeout
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class OkioMemoryFileSystem @OptIn(ExperimentalTime::class) constructor(
    var maxCacheDurationMillis: Long,
    var maxBytes: Long,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
): FileSystem() {

    private data class Entry(
        val bytes: ByteArray,
        val storedAtMillis: Long
    ) {
        val size: Long get() = bytes.size.toLong()


        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry) return false
            if (storedAtMillis != other.storedAtMillis) return false
            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            var result = storedAtMillis.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }

    private val lock = SynchronizedObject()

    private val files: MutableMap<Path, Entry> = linkedMapOf()
    private val directories: MutableSet<Path> = hashSetOf()

    private var totalBytes: Long = 0L

    override fun canonicalize(path: Path): Path = path

    override fun metadataOrNull(path: Path): FileMetadata? = synchronized(lock) {
        pruneExpiredLocked()

        if (directories.contains(path)) {
            return@synchronized FileMetadata(
                isRegularFile = false,
                isDirectory = true,
                size = null
            )
        }

        val e = files[path] ?: return@synchronized null
        FileMetadata(
            isRegularFile = true,
            isDirectory = false,
            size = e.size
        )
    }

    override fun list(dir: Path): List<Path> = synchronized(lock) {
        pruneExpiredLocked()

        val prefix = if (dir.toString().endsWith("/")) dir.toString() else dir.toString() + "/"

        val out = linkedSetOf<Path>()

        fun addChild(full: Path) {
            val s = full.toString()
            if (!s.startsWith(prefix)) return
            val rest = s.removePrefix(prefix)
            val child = rest.substringBefore("/")
            if (child.isNotEmpty()) {
                out.add((prefix + child).toPathCompat())
            }
        }

        directories.forEach { addChild(it) }
        files.keys.forEach { addChild(it) }

        out.toList()
    }

    override fun listOrNull(dir: Path): List<Path>? = try {
        list(dir)
    } catch (_: Throwable) {
        null
    }

    override fun openReadOnly(file: Path): FileHandle {
        throw UnsupportedOperationException("openReadOnly not supported. Use source(file).")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        throw UnsupportedOperationException("openReadWrite not supported. Use sink(file) or appendingSink(file).")
    }

    override fun source(file: Path): Source = synchronized(lock) {
        pruneExpiredLocked()

        val e = files[file] ?: throw FileNotFoundException("No such file: $file")
        Buffer().write(e.bytes).asSource()
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        val buffer = Buffer()

        return object : Sink {
            private var closed = false

            override fun timeout(): Timeout = Timeout.NONE

            override fun write(source: Buffer, byteCount: Long) {
                check(!closed) { "closed" }
                buffer.write(source, byteCount)
            }

            override fun flush() {
                check(!closed) { "closed" }
            }

            override fun close() {
                if (closed) return
                closed = true

                val bytes = buffer.readByteArray()
                commitBytes(file = file, bytes = bytes, mustCreate = mustCreate, mustExist = false, append = false)
            }
        }
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        val buffer = Buffer()

        return object : Sink {
            private var closed = false

            override fun timeout(): Timeout = Timeout.NONE

            override fun write(source: Buffer, byteCount: Long) {
                check(!closed) { "closed" }
                buffer.write(source, byteCount)
            }

            override fun flush() {
                check(!closed) { "closed" }
            }

            override fun close() {
                if (closed) return
                closed = true

                val bytesToAppend = buffer.readByteArray()
                commitBytes(file = file, bytes = bytesToAppend, mustCreate = false, mustExist = mustExist, append = true)
            }
        }
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) = synchronized(lock) {
        pruneExpiredLocked()

        if (directories.contains(dir)) {
            if (mustCreate) throw IOException("Directory already exists: $dir")
            return@synchronized
        }
        directories.add(dir)
    }

    override fun atomicMove(source: Path, target: Path) = synchronized(lock) {
        pruneExpiredLocked()

        if (directories.remove(source)) {
            directories.add(target)
            return@synchronized
        }

        val e = files.remove(source) ?: throw FileNotFoundException("No such file: $source")
        files[target]?.let { existing ->
            totalBytes -= existing.size
        }
        files[target] = e
    }

    override fun delete(path: Path, mustExist: Boolean) = synchronized(lock) {
        pruneExpiredLocked()

        if (directories.remove(path)) return@synchronized

        val removed = files.remove(path)
        if (removed != null) {
            totalBytes -= removed.size
            return@synchronized
        }

        if (mustExist) throw FileNotFoundException("No such file: $path")
    }

    override fun createSymlink(source: Path, target: Path) {
        throw UnsupportedOperationException("Symlinks not supported in memory filesystem.")
    }

    private fun commitBytes(
        file: Path,
        bytes: ByteArray,
        mustCreate: Boolean,
        mustExist: Boolean,
        append: Boolean
    ) = synchronized(lock) {
        pruneExpiredLocked()

        val now = nowMillis()

        if (bytes.size.toLong() > maxBytes) {
            throw IOException("File too large (${bytes.size} bytes) exceeds MAX_BYTES=$maxBytes")
        }

        val existing = files[file]
        if (mustCreate && existing != null) {
            throw IOException("File already exists: $file")
        }
        if (mustExist && existing == null) {
            throw FileNotFoundException("No such file: $file")
        }

        val newBytes = if (append && existing != null) {
            existing.bytes + bytes
        } else {
            bytes
        }

        if (newBytes.size.toLong() > maxBytes) {
            throw IOException("Resulting file too large (${newBytes.size} bytes) exceeds MAX_BYTES=$maxBytes")
        }

        val existingSize = existing?.size ?: 0L
        val newSize = newBytes.size.toLong()

        val required = (totalBytes - existingSize) + newSize
        if (required > maxBytes) {
            evictOldestUntilFitsLocked(requiredBytes = required)
        }

        val requiredAfter = (totalBytes - existingSize) + newSize
        if (requiredAfter > maxBytes) {
            throw IOException("Not enough space after eviction. required=$requiredAfter maxBytes=$maxBytes")
        }

        if (existing != null) {
            totalBytes -= existingSize
        }

        files[file] = Entry(bytes = newBytes, storedAtMillis = now)
        totalBytes += newSize
    }

    private fun pruneExpiredLocked() {
        if (maxCacheDurationMillis <= 0L) return

        val now = nowMillis()
        val it = files.entries.iterator()
        while (it.hasNext()) {
            val (path, entry) = it.next()
            val age = now - entry.storedAtMillis
            if (age >= maxCacheDurationMillis) {
                totalBytes -= entry.size
                it.remove()
            }
        }
    }

    private fun evictOldestUntilFitsLocked(requiredBytes: Long) {
        if (requiredBytes <= maxBytes) return
        val victims = files.entries
            .sortedBy { it.value.storedAtMillis }

        var bytesToFree = requiredBytes - maxBytes
        if (bytesToFree <= 0L) return

        for ((path, entry) in victims) {
            files.remove(path)
            totalBytes -= entry.size
            bytesToFree -= entry.size
            if (bytesToFree <= 0L) return
        }
    }

    companion object {
          fun Create(maxCacheDurationMinutes:Int, maxBytesMB: Int) = OkioMemoryFileSystem(
              maxCacheDurationMillis =maxCacheDurationMinutes.toDuration(DurationUnit.MINUTES).inWholeMilliseconds,
              maxBytes = maxBytesMB * 1024L * 1024L
          )
    }
}

private fun String.toPathCompat(): Path =  this.toPath()
private fun Buffer.asSource(): Source = this