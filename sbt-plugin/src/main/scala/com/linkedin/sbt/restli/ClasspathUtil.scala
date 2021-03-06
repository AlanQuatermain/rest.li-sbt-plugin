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


object ClasspathUtil {

  /**
   * inspired by play.api.util.Threads method with same name.
   * added finally to be exception safe
   *
   * @param classloader
   * @param res
   * @tparam T
   * @return
   */
  def withContextClassLoader[T](classloader: ClassLoader)(res: => T): T = {
    val thread = Thread.currentThread
    val oldLoader = thread.getContextClassLoader
    thread.setContextClassLoader(classloader)
    try {
      res
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }

  /**
   * Create a ClassLoader from a collection of classpath URLs.
   *
   * @param classpath
   * @param parentClassLoader
   * @return
   */
  def classLoaderFromClasspath(classpath: Seq[String], parentClassLoader: ClassLoader = this.getClass.getClassLoader): ClassLoader = {
    val classUrls = classpath.map(path => (new java.io.File(path)).toURI.toURL).toArray
    new java.net.URLClassLoader(classUrls, parentClassLoader)
  }
}