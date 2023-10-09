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

import rudiments.*
import parasite.*
import anticipation.*

import scala.collection.mutable as scm

extension (value: LazyList[Bytes])
  def slurp(): Bytes =
    val bld: scm.ArrayBuilder[Byte] = scm.ArrayBuilder.ofByte()
    value.foreach { bs => bld.addAll(bs.mutable(using Unsafe)) }
    
    bld.result().immutable(using Unsafe)

extension (obj: LazyList.type)
  def multiplex[ElemType](streams: LazyList[ElemType]*)(using Monitor): LazyList[ElemType] =
    multiplexer(streams*).stream
  
  def multiplexer[ElemType](streams: LazyList[ElemType]*)(using Monitor): Multiplexer[Any, ElemType] =
    val multiplexer = Multiplexer[Any, ElemType]()
    streams.zipWithIndex.map(_.swap).foreach(multiplexer.add)
    multiplexer
  
  def pulsar[DurationType: GenericDuration](duration: DurationType)(using Monitor): LazyList[Unit] =
    val startTime: Long = System.currentTimeMillis
    
    def recur(iteration: Int): LazyList[Unit] =
      try
        sleepUntil(startTime + duration.milliseconds*iteration)
        () #:: pulsar(duration)
      catch case err: CancelError => LazyList()
    
    recur(0)