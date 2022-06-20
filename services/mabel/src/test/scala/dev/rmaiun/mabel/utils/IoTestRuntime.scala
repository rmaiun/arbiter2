package dev.rmaiun.mabel.utils

import cats.effect.unsafe.IORuntime

trait IoTestRuntime {
  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global
}
