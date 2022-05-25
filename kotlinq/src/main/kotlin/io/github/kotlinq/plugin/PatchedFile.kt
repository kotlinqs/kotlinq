package io.github.kotlinq.plugin

import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileSystem
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.path.Path
import kotlin.io.path.readText

abstract class PatchedFile(private val file: VirtualFile): VirtualFile() {

    private val patchedBody: String by lazy { patchBody(Path(file.path).readText()) }

    protected abstract fun patchBody(original: String): String

    init {
        file.copyCopyableDataTo(this)
    }

    override fun getName(): String {
        return file.name
    }

    override fun getFileSystem(): VirtualFileSystem {
        return file.fileSystem
    }

    override fun getPath(): String {
        return file.path
    }

    override fun isWritable(): Boolean {
        return file.isWritable
    }

    override fun isDirectory(): Boolean {
        return file.isDirectory
    }

    override fun isValid(): Boolean {
        return file.isValid
    }

    override fun getParent(): VirtualFile {
        return file.parent
    }

    override fun getChildren(): Array<VirtualFile> {
        return file.children
    }

    override fun getOutputStream(p0: Any?, p1: Long, p2: Long): OutputStream {
        return file.getOutputStream(p0, p1, p2)
    }

    override fun contentsToByteArray(): ByteArray {
        return patchedBody.toByteArray()
    }

    override fun contentsToByteArray(cacheContent: Boolean): ByteArray {
        return patchedBody.toByteArray()
    }

    override fun getTimeStamp(): Long {
        return file.timeStamp
    }

    override fun getLength(): Long {
        return patchedBody.length.toLong()
    }

    override fun refresh(p0: Boolean, p1: Boolean, p2: Runnable?) {
        file.refresh(p0, p1, p2)
    }

    override fun getInputStream(): InputStream {
        return patchedBody.byteInputStream()
    }

}