/*
    Turbulence, version [unreleased]. Copyright 2023 Jon Pretty, Propensive OÜ.

    The primary distribution site is: https://propensive.com/

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
    file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions
    and limitations under the License.
*/

package turbulence

import java.util.concurrent as juc

object Funnel:
  private object Termination

class Funnel[ItemType]():
  private val queue: juc.LinkedBlockingQueue[ItemType | Funnel.Termination.type] = juc.LinkedBlockingQueue()
  
  def put(item: ItemType): Unit = queue.put(item)
  def stop(): Unit = queue.put(Funnel.Termination)
  def stream: LazyList[ItemType] = LazyList.continually(queue.take().nn).takeWhile(_ != Funnel.Termination)

class Gun() extends Funnel[Unit]():
  def fire(): Unit = put(())