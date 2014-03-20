/*
   Copyright (c) 2014 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.linkedin.sbt.restli


import sbt._
import sbt.Keys._

/**
 * Convenience trait that includes all Restli functionality
 */
trait All extends Pegasus with Restspec
trait RestliTypeProject {
  val project : Project
}


trait Restli {
  val JavaFileGlobExpr = "*.java"
  val ClassFileGlobExpr = "*.class"
  val PdscFileGlobExpr = "*.pdsc"

  val RestspecJsonFileGlobExpr = "*.restspec.json"
  val SnapshotJsonFileGlobExpr = "*.snapshot.json"

  /**
   * Generates settings that place the artifact generated by `packagingTaskKey` in the specified `ivyConfig`,
   * while also suffixing the artifact name with "-" and the `ivyConfig`.
   */
  def restliArtifactSettings(packagingTaskKey : TaskKey[File])(ivyConfig : String) = {
    val config = Configurations.config(ivyConfig)

    Seq(
        (artifact in packagingTaskKey) <<= (artifact in packagingTaskKey) { artifact =>
          artifact.copy(name = artifact.name + "-" + ivyConfig,
                        configurations = artifact.configurations ++ Seq(config))
        },
        ivyConfigurations += config
    )
  }

  /**
   * Finds descendants of `dir` matching `globExpr` and maps them to paths relative to `dir`.
   */
  def mappings(dir : File, globExpr : String): Seq[(File, String)] = {
    val filter = GlobFilter(globExpr)
    Seq(dir).flatMap(d => Path.allSubpaths(d).filter{ case (f, id) => filter.accept(f) } )
  }

  /**
   * Returns an indication of whether `sourceFiles` and their modify dates differ from what is recorded in `cacheFile`,
   * plus a function that can be called to write `sourceFiles` and their modify dates to `cacheFile`.
   */
  def prepareCacheUpdate(cacheFile: File, sourceFiles: Seq[File],
                         streams: std.TaskStreams[_]): (Boolean, () => Unit) = {
    val fileToModifiedMap = sourceFiles.map(f => f -> FileInfo.lastModified(f)).toMap

    val (_, previousFileToModifiedMap) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
    val relation = Seq.fill(sourceFiles.size)(file(".")) zip sourceFiles //we only care about the source files here

    streams.log.debug(fileToModifiedMap.size + " <- current VS previous ->" + previousFileToModifiedMap.size)
    val anyFilesChanged = !cacheFile.exists || (previousFileToModifiedMap != fileToModifiedMap)
    def updateCache() {
      Sync.writeInfo(cacheFile, Relation.empty[File, File] ++ relation.toMap,
                     sourceFiles.map(f => f -> FileInfo.lastModified(f)).toMap)(FileInfo.lastModified.format)
    }
    (anyFilesChanged, updateCache)
  }
}