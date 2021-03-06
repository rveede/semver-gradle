/*
 * SemverVersionSpec.kt
 *
 * Copyright (c) 2018-2019, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.gradle.semver

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import java.nio.file.Files
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("unused")
object UtilsSpec : Spek({
    describe("a config and version") {
        val version by memoized { Version() }
        val config by memoized { SemverConfig() }
        val configFile = File("test.properties")
        lateinit var props: Properties

        before {
            config.properties = configFile.name
        }

        describe("save properties") {
            it("should save properties") {
                Utils.saveProperties(config, version)
                assertTrue(configFile.exists())
            }
            it("load the properties") {
                props = Properties().apply {
                    Files.newInputStream(configFile.toPath()).use { nis ->
                        load(nis)
                    }
                    configFile.delete()
                }
            }
        }
        describe("validate the properties file") {
            it("version should be the same") {
                assertEquals(props.getProperty(config.majorKey), version.major, "Major")
                assertEquals(props.getProperty(config.minorKey), version.minor, "Minor")
                assertEquals(props.getProperty(config.patchKey), version.patch, "Patch")
                assertEquals(props.getProperty(config.preReleaseKey), version.preRelease, "PreRelease")
                assertNull(props.getProperty(config.preReleasePrefixKey), "PreRelease Prefix")
                assertEquals(props.getProperty(config.buildMetaKey), version.buildMeta, "Build Meta")
                assertNull(props.getProperty(config.buildMetaPrefixKey), "Build Meta Prefix")
                assertNull(props.getProperty(config.separatorKey), "Separator")
            }
        }
        describe("setting system properties") {
            val newVersion = arrayOf(
                Pair(config.majorKey, "2"),
                Pair(config.minorKey, "1"),
                Pair(config.patchKey, "1"),
                Pair(config.preReleaseKey, "beta"),
                Pair(config.buildMetaKey, "007"))
            it("should have none of our properties") {
                assertTrue(Utils.isNotSystemProperty(setOf(config.majorKey, config.minorKey, config.patchKey, config.preReleaseKey,
                    config.buildMetaKey)))
            }
            it("version should match system properties") {
                newVersion.forEach {
                    System.getProperties().setProperty(it.first, it.second)
                    assertEquals(Utils.loadProperty(props, it.first, ""), it.second)
                }
            }
        }
    }
})
